package com.example.foodsaver.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface UserDao {

    // Returns the newly inserted row ID
    @Insert
    long insertUser(User user);

    // Synchronous call for login verification
    @Query("SELECT * FROM users WHERE email = :email AND motDePasse = :password LIMIT 1")
    User login(String email, String password);

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    LiveData<User> getUserById(int id);
}