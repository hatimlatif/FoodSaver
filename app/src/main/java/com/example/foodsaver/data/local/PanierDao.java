package com.example.foodsaver.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.foodsaver.data.model.Panier;

import java.util.List;

@Dao
public interface PanierDao {

    // NOUVEAU : OnConflictStrategy.REPLACE permet d'écraser les vieilles données locales
    // par les nouvelles données fraîches de Supabase
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertPanier(Panier panier);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllPaniers(List<Panier> paniers); // Pour la synchronisation de masse

    @Update
    void updatePanier(Panier panier);

    @Delete
    void deletePanier(Panier panier);

    @Query("SELECT * FROM paniers")
    LiveData<List<Panier>> getAllPaniers();

    @Query("SELECT * FROM paniers WHERE commerceId = :commerceId")
    LiveData<List<Panier>> getPaniersByCommerce(int commerceId);

    @Query("SELECT * FROM paniers WHERE isSynced = 0")
    List<Panier> getUnsyncedPaniersSync();

    @Query("SELECT * FROM paniers")
    List<Panier> getAllPaniersSync();

    @Query("UPDATE paniers SET quantite = quantite - 1 WHERE id = :panierId AND quantite > 0")
    void decrementQuantity(int panierId);

    @Query("UPDATE paniers SET quantite = quantite + 1 WHERE id = :panierId")
    void incrementQuantity(int panierId);

    @Query("SELECT * FROM paniers WHERE id = :panierId")
    Panier getPanierByIdSync(int panierId);
}


