package com.example.foodsaver.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CommerceDao {

    @Insert
    long insertCommerce(Commerce commerce);

    // For the Client: see all stores
    @Query("SELECT * FROM commerces")
    LiveData<List<Commerce>> getAllCommerces();

    // For the Commerçant: see their specific stores
    @Query("SELECT * FROM commerces WHERE commercantId = :commercantId")
    LiveData<List<Commerce>> getCommercesByCommercant(int commercantId);
}