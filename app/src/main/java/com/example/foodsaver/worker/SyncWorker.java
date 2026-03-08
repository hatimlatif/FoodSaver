package com.example.foodsaver.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.foodsaver.api.ApiClient;
import com.example.foodsaver.api.SupabaseApiService;
import com.example.foodsaver.data.AppDatabase;
import com.example.foodsaver.data.model.Panier;
import com.example.foodsaver.data.local.PanierDao;
import com.example.foodsaver.data.network.PanierRequest;

import java.util.List;

public class SyncWorker extends Worker {
    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        PanierDao dao = AppDatabase.getDatabase(getApplicationContext()).panierDao();
        SupabaseApiService api = ApiClient.getClient().create(SupabaseApiService.class);

        // 1. Find all unsynced paniers
        List<Panier> unsynced = dao.getUnsyncedPaniersSync();

        for (Panier p : unsynced) {
            try {
                PanierRequest request = new PanierRequest(p);
                retrofit2.Response<List<Panier>> response = api.insertPanierCloud(request).execute();

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    // Remplacement du fantôme par le vrai ID cloud
                    Panier realCloudPanier = response.body().get(0);
                    realCloudPanier.setIsSynced(1);

                    dao.deletePanier(p);
                    dao.insertPanier(realCloudPanier);
                }
            } catch (Exception e) {
                return Result.retry();
            }
        }
        return Result.success();
    }
}