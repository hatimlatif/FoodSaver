package com.example.foodsaver.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.foodsaver.api.ApiClient;
import com.example.foodsaver.api.SupabaseApiService;
import com.example.foodsaver.data.AppDatabase;
import com.example.foodsaver.data.model.Panier;
import com.example.foodsaver.data.local.PanierDao;
import com.example.foodsaver.data.network.PanierRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PanierRepository {
    private PanierDao panierDao;
    private SupabaseApiService apiService;

    public PanierRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        panierDao = db.panierDao();
        // Initialize the cloud API engine
        apiService = ApiClient.getClient().create(SupabaseApiService.class);
    }

    /**
     * HYBRID SYNC: Fetches local data instantly, then triggers a background cloud refresh.
     */
    public LiveData<List<Panier>> getAllPaniers() {
        refreshPaniersFromCloud(); // Background sync
        return panierDao.getAllPaniers(); // Returns the local "Mirror"
    }

    public LiveData<List<Panier>> getPaniersByCommerce(int commerceId) {
        refreshPaniersFromCloud(); // Background sync
        return panierDao.getPaniersByCommerce(commerceId);
    }

    /**
     * CLOUD REFRESH: Downloads latest paniers from Supabase and overwrites the local Room DB.
     */
    private void refreshPaniersFromCloud() {
        apiService.getPaniersFromCloud().enqueue(new Callback<List<Panier>>() {
            @Override
            public void onResponse(Call<List<Panier>> call, Response<List<Panier>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 1. Récupérer la liste téléchargée
                    List<Panier> downloadedCommerces = response.body();

                    // 2. LA CORRECTION : Marquer tout comme "déjà synchronisé"
                    for (Panier p : downloadedCommerces) {
                        p.setIsSynced(1);
                    }

                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        // The OnConflictStrategy.REPLACE in the DAO ensures the local DB stays updated
                        panierDao.insertAllPaniers(response.body());
                    });
                }
            }

            @Override
            public void onFailure(Call<List<Panier>> call, Throwable t) {
                Log.e("SyncError", "Failed to sync paniers: " + t.getMessage());
            }
        });
    }

    /**
     * HYBRID INSERT: Saves to Room immediately (for speed) then pushes to Cloud.
     */
    public void insertPanier(Panier panier) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // 1. Sauvegarde locale (ID fantôme)
            panier.setIsSynced(0);
            long localId = panierDao.insertPanier(panier);
            panier.setId((int) localId);

            // 2. Envoi vers le Cloud
            PanierRequest request = new PanierRequest(panier);
            apiService.insertPanierCloud(request).enqueue(new Callback<List<Panier>>() {
                @Override
                public void onResponse(Call<List<Panier>> call, Response<List<Panier>> response) {
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        Panier realCloudPanier = response.body().get(0);
                        realCloudPanier.setIsSynced(1);

                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            panierDao.deletePanier(panier);
                            panierDao.insertPanier(realCloudPanier);
                        });
                    } else {
                        // NOUVEAU : Afficher l'erreur exacte de Supabase dans le Logcat
                        try {
                            android.util.Log.e("SupabaseError", "Erreur insertion: " + response.errorBody().string());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<Panier>> call, Throwable t) {
                    // NOUVEAU : Afficher l'erreur si le réseau plante complètement
                    android.util.Log.e("SupabaseError", "Crash réseau: " + t.getMessage());
                }
            });
        });
    }

    public void updatePanier(Panier panier) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            panier.setIsSynced(0); // Marque comme modifié non synchronisé
            panierDao.updatePanier(panier);

            String filter = "eq." + panier.getId();
            PanierRequest request = new PanierRequest(panier);

            apiService.updatePanierCloud(filter, request).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        panier.setIsSynced(1);
                        AppDatabase.databaseWriteExecutor.execute(() -> panierDao.updatePanier(panier));
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                }
            });
        });
    }

    public void deletePanier(Panier panier) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            panierDao.deletePanier(panier);

            String filter = "eq." + panier.getId();
            apiService.deletePanierCloud(filter).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                }
            });
        });
    }
}