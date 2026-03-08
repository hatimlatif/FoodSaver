package com.example.foodsaver.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.foodsaver.api.ApiClient;
import com.example.foodsaver.api.SupabaseApiService;
import com.example.foodsaver.data.AppDatabase;
import com.example.foodsaver.data.model.Commerce;
import com.example.foodsaver.data.local.CommerceDao;
import com.example.foodsaver.data.network.CommerceRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommerceRepository {
    private CommerceDao commerceDao;
    private SupabaseApiService apiService;

    public CommerceRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        commerceDao = db.commerceDao();
        apiService = ApiClient.getClient().create(SupabaseApiService.class);
    }

    public LiveData<List<Commerce>> getAllCommerces() {
        refreshCommerces(); // Trigger cloud sync
        return commerceDao.getAllCommerces();
    }

    public LiveData<List<Commerce>> getCommercesByCommercant(String commercantId) { // int -> String
        refreshCommerces(); // Trigger cloud sync
        return commerceDao.getCommercesByCommercant(commercantId);
    }

    private void refreshCommerces() {
        apiService.getCommercesFromCloud().enqueue(new Callback<List<Commerce>>() {
            @Override
            public void onResponse(Call<List<Commerce>> call, Response<List<Commerce>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 1. Récupérer la liste téléchargée
                    List<Commerce> downloadedCommerces = response.body();

                    // 2. LA CORRECTION : Marquer tout comme "déjà synchronisé"
                    for (Commerce c : downloadedCommerces) {
                        c.setIsSynced(1);
                    }

                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        commerceDao.insertAllCommerces(response.body());
                    });
                }
            }

            @Override
            public void onFailure(Call<List<Commerce>> call, Throwable t) {
            }
        });
    }

    public void insertCommerce(Commerce commerce) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // 1. Sauvegarde locale (ID fantôme)
            commerce.setIsSynced(0);
            long localId = commerceDao.insertCommerce(commerce);
            commerce.setId((int) localId);

            // 2. Envoi vers le Cloud
            CommerceRequest request = new CommerceRequest(commerce);
            apiService.insertCommerceCloud(request).enqueue(new Callback<List<Commerce>>() {
                @Override
                public void onResponse(Call<List<Commerce>> call, Response<List<Commerce>> response) {
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        // 3. On récupère le VRAI ID généré par Supabase
                        Commerce realCloudCommerce = response.body().get(0);
                        realCloudCommerce.setIsSynced(1);

                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            // 4. On remplace le fantôme par le vrai !
                            commerceDao.deleteCommerce(commerce);
                            commerceDao.insertCommerce(realCloudCommerce);
                        });
                    } else {
                        try {
                            android.util.Log.e("SupabaseError", "Erreur Commerce: " + response.errorBody().string());
                        } catch (Exception e) {
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<Commerce>> call, Throwable t) {
                }
            });
        });
    }

    public void deleteCommerceAdmin(Commerce commerce) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // 1. Suppression locale
            commerceDao.deleteCommerce(commerce);

            // 2. Suppression Cloud (le "eq." est requis par l'API Supabase PostgREST)
            apiService.deleteCommerceCloud("eq." + commerce.getId()).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (!response.isSuccessful()) {
                        android.util.Log.e("AdminAuth", "Erreur suppression cloud: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                }
            });
        });
    }
}