package com.example.foodsaver.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.foodsaver.data.model.Commerce;

import java.util.List;

@Dao
public interface CommerceDao {

    @Insert
    long insertCommerce(Commerce commerce);

    // For the Client: see all stores
    @Query("SELECT * FROM commerces")
    LiveData<List<Commerce>> getAllCommerces();

    // ... autres méthodes inchangées ...
    @Query("SELECT * FROM commerces WHERE commercantId = :commercantId")
    LiveData<List<Commerce>> getCommercesByCommercant(String commercantId); // int -> String

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllCommerces(List<Commerce> commerces);

    // Met à jour un commerce existant (utilisé pour passer isSynced de 0 à 1)
    @Update
    void updateCommerce(Commerce commerce);

    // Trouve tous les commerces créés hors-ligne pour le WorkManager
    @Query("SELECT * FROM commerces WHERE isSynced = 0")
    List<Commerce> getUnsyncedCommercesSync();

    @Delete
    void deleteCommerce(Commerce c);
}