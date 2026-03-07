package com.example.foodsaver.repository;

import android.app.Application;

import com.example.foodsaver.data.AppDatabase;
import com.example.foodsaver.data.User;
import com.example.foodsaver.data.UserDao;

public class AuthRepository {
    private UserDao userDao;

    public AuthRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        userDao = db.userDao();
    }

    // We use an interface to pass the result back from the background thread
    public interface AuthCallback {
        void onSuccess(User user);

        void onFailure(String error);
    }

    public void registerUser(User user, AuthCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // insertUser returns the new row ID
                long newId = userDao.insertUser(user);
                if (newId > 0) {
                    user.setId((int) newId);
                    callback.onSuccess(user);
                } else {
                    callback.onFailure("Erreur lors de l'inscription");
                }
            } catch (Exception e) {
                callback.onFailure("Cet email existe peut-être déjà");
            }
        });
    }

    public void loginUser(String email, String password, AuthCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            User user = userDao.login(email, password);
            if (user != null) {
                callback.onSuccess(user);
            } else {
                callback.onFailure("Email ou mot de passe incorrect");
            }
        });
    }
}