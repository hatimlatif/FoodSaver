package com.example.foodsaver.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PanierDao {

    @Insert
    long insertPanier(Panier panier);

    @Update
    void updatePanier(Panier panier);

    @Delete
    void deletePanier(Panier panier);

    // For the Client: see all available baskets across the app
    @Query("SELECT * FROM paniers")
    LiveData<List<Panier>> getAllPaniers();

    // For the Commerçant: see only the baskets belonging to their commerce
    @Query("SELECT * FROM paniers WHERE commerceId = :commerceId")
    LiveData<List<Panier>> getPaniersByCommerce(int commerceId);
}