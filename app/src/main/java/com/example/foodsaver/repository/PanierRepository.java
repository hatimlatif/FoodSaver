package com.example.foodsaver.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.foodsaver.data.AppDatabase;
import com.example.foodsaver.data.Panier;
import com.example.foodsaver.data.PanierDao;

import java.util.List;

public class PanierRepository {
    private PanierDao panierDao;

    public PanierRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        panierDao = db.panierDao();
    }

    // LiveData automatically runs on a background thread, so no executor needed here!
    public LiveData<List<Panier>> getAllPaniers() {
        return panierDao.getAllPaniers();
    }

    public LiveData<List<Panier>> getPaniersByCommerce(int commerceId) {
        return panierDao.getPaniersByCommerce(commerceId);
    }

    // Inserts MUST be on a background thread
    public void insertPanier(Panier panier) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            panierDao.insertPanier(panier);
        });
    }

    public void updatePanier(Panier panier) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            panierDao.updatePanier(panier);
        });
    }

    public void deletePanier(Panier panier) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            panierDao.deletePanier(panier);
        });
    }
}