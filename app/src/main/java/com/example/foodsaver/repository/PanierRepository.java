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
import com.example.foodsaver.utils.NotificationHelper;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PanierRepository {
    private PanierDao panierDao;
    private SupabaseApiService apiService;
    private Application application; // ADDED: Context for notifications

    public PanierRepository(Application application) {
        this.application = application;
        AppDatabase db = AppDatabase.getDatabase(application);
        panierDao = db.panierDao();
        apiService = ApiClient.getClient().create(SupabaseApiService.class);
    }

    public LiveData<List<Panier>> getAllPaniers() {
        refreshPaniersFromCloud();
        return panierDao.getAllPaniers();
    }

    public LiveData<List<Panier>> getPaniersByCommerce(int commerceId) {
        refreshPaniersFromCloud();
        return panierDao.getPaniersByCommerce(commerceId);
    }

    public void refreshPaniersFromCloud() {
        apiService.getPaniersFromCloud().enqueue(new Callback<List<Panier>>() {
            @Override
            public void onResponse(Call<List<Panier>> call, Response<List<Panier>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Panier> cloudPaniers = response.body();
                    Log.d("SyncDebug", "Received " + cloudPaniers.size() + " paniers from cloud");


                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        // 1. Get local IDs for comparison
                        List<Panier> locals = panierDao.getAllPaniersSync();
                        java.util.Set<Integer> localIds = new java.util.HashSet<>();
                        if (locals != null) {
                            for (Panier p : locals) localIds.add(p.getId());
                        }

                        // 2. Identify local paniers that should be deleted (missing from cloud)
                        // Only delete if they WERE synced (isSynced=1) to avoid deleting items being created
                        if (locals != null) {
                            java.util.Set<Integer> cloudIds = new java.util.HashSet<>();
                            for (Panier p : cloudPaniers) cloudIds.add(p.getId());

                            for (Panier local : locals) {
                                if (local.getIsSynced() == 1 && !cloudIds.contains(local.getId())) {
                                    panierDao.deletePanier(local);
                                }
                            }
                        }

                        // 3. PRO TRIGGER: Check for REALLY new paniers (by ID)
                        Panier trulyNew = null;
                        for (Panier p : cloudPaniers) {
                            if (!localIds.contains(p.getId())) {
                                trulyNew = p;
                                break;
                            }
                        }

                        if (trulyNew != null) {
                            NotificationHelper.showNewPanierNotification(application, trulyNew.getTitre());
                        }

                        // --- UPSERT LOGIC ---
                        for (Panier p : cloudPaniers) {
                            p.setIsSynced(1);
                        }
                        panierDao.insertAllPaniers(cloudPaniers);
                    });

                }
            }

            @Override
            public void onFailure(Call<List<Panier>> call, Throwable t) {
                Log.e("SyncError", "Failed to sync paniers: " + t.getMessage());
            }
        });
    }

    public void insertPanier(Panier panier) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            panier.setIsSynced(0);
            long localId = panierDao.insertPanier(panier);
            panier.setId((int) localId);

            PanierRequest request = new PanierRequest(panier);
            apiService.insertPanierCloud(request).enqueue(new Callback<List<Panier>>() {
                @Override
                public void onResponse(Call<List<Panier>> call, Response<List<Panier>> response) {
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        Panier realCloudPanier = response.body().get(0);
                        realCloudPanier.setIsSynced(1);

                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            // On supprime le "fantôme" (celui avec l'ID temporaire)
                            panierDao.deletePanier(panier);
                            // On insère le vrai (avec l'ID Supabase)
                            panierDao.insertPanier(realCloudPanier);
                        });

                    }
                }

                @Override
                public void onFailure(Call<List<Panier>> call, Throwable t) {
                }
            });
        });
    }

    public void updatePanier(Panier panier) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            panier.setIsSynced(0);
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
            // 1. Suppression locale
            panierDao.deletePanier(panier);

            // 2. Suppression Cloud
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

    public void decrementQuantity(int panierId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // 1. Décrémentation locale
            panierDao.decrementQuantity(panierId);

            // 2. Récupérer l'objet mis à jour pour synchro cloud
            Panier updatedPanier = panierDao.getPanierByIdSync(panierId);
            if (updatedPanier != null) {
                updatePanier(updatedPanier);
            }
        });
    }

    public void incrementQuantity(int panierId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // 1. Incrémentation locale
            panierDao.incrementQuantity(panierId);

            // 2. Récupérer l'objet mis à jour pour synchro cloud
            Panier updatedPanier = panierDao.getPanierByIdSync(panierId);
            if (updatedPanier != null) {
                updatePanier(updatedPanier);
            }
        });
    }
}
