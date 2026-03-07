package com.example.foodsaver.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.foodsaver.data.AppDatabase;
import com.example.foodsaver.data.Commerce;
import com.example.foodsaver.data.CommerceDao;

import java.util.List;

public class CommerceRepository {
    private CommerceDao commerceDao;

    public CommerceRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        commerceDao = db.commerceDao();
    }

    public LiveData<List<Commerce>> getAllCommerces() {
        return commerceDao.getAllCommerces();
    }

    public LiveData<List<Commerce>> getCommercesByCommercant(int commercantId) {
        return commerceDao.getCommercesByCommercant(commercantId);
    }

    public void insertCommerce(Commerce commerce) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            commerceDao.insertCommerce(commerce);
        });
    }
}