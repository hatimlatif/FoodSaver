package com.example.foodsaver.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.foodsaver.data.local.CommerceDao;
import com.example.foodsaver.data.local.PanierDao;
import com.example.foodsaver.data.local.ReservationDao;
import com.example.foodsaver.data.local.UserDao;
import com.example.foodsaver.data.model.Commerce;
import com.example.foodsaver.data.model.Panier;
import com.example.foodsaver.data.model.Reservation;
import com.example.foodsaver.data.model.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// If you change the entities later (like adding a column for V2), you will increment the version number here.
@Database(entities = {User.class, Commerce.class, Panier.class, Reservation.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    // These abstract methods link the DAOs to the database
    public abstract UserDao userDao();

    public abstract CommerceDao commerceDao();

    public abstract PanierDao panierDao();

    public abstract ReservationDao reservationDao();

    // The Singleton pattern prevents having multiple instances of the database opened at the same time
    private static volatile AppDatabase INSTANCE;

    // We create a background thread pool specifically for database write operations
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "foodsaver_database")
                            // This wipes and rebuilds the database if you change the schema without a migration path
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}