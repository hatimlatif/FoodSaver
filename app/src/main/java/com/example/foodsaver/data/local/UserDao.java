package com.example.foodsaver.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.foodsaver.data.model.User;

@Dao
public interface UserDao {

    // OnConflictStrategy.REPLACE ensures that if Supabase sends us updated user info, we overwrite the local copy
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(User user);

    // V2: Changed parameter from int to String
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    LiveData<User> getUserById(String id);

    // Facultatif : Pour vider la table lors d'une déconnexion
    @Query("DELETE FROM users")
    void deleteAllUsers();
}