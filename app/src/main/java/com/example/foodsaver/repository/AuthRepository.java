package com.example.foodsaver.repository;

import android.app.Application;

import com.example.foodsaver.api.ApiClient;
import com.example.foodsaver.api.SupabaseApiService;
import com.example.foodsaver.data.AppDatabase;
import com.example.foodsaver.data.model.PublicUser;
import com.example.foodsaver.data.network.AuthRequest;
import com.example.foodsaver.data.network.AuthResponse;
import com.example.foodsaver.data.model.User;
import com.example.foodsaver.data.local.UserDao;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {
    private UserDao userDao;
    private SupabaseApiService apiService; // NOUVEAU : L'interface réseau

    public AuthRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        userDao = db.userDao();
        // Initialiser Retrofit
        apiService = ApiClient.getClient().create(SupabaseApiService.class);
    }

    // We use an interface to pass the result back from the background thread
    public interface AuthCallback {
        void onSuccess(User user);

        void onFailure(String error);
    }

    public void registerUser(User user, AuthCallback callback) {
        // Le mot de passe a été temporairement stocké dans le champ 'token' du modèle User
        String password = user.getToken();

        // 1. Préparer la requête JSON pour Supabase
        AuthRequest request = new AuthRequest(user.getEmail(), password, user.getNom(), user.getRole());

        // 2. Envoyer la requête via Retrofit (s'exécute automatiquement en arrière-plan)
        apiService.signUp(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getUser() != null) {
                    AuthResponse authResponse = response.body();

                    // 3. Reconstruire l'objet User avec l'UUID sécurisé généré par Supabase
                    User cloudUser = new User(
                            authResponse.getUser().getId(),
                            user.getNom(),
                            user.getEmail(),
                            user.getRole()
                    );
                    cloudUser.setToken(authResponse.getAccessToken());

                    // 4. Sauvegarder localement dans Room pour le mode hors-ligne
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        userDao.insertUser(cloudUser);
                        // Retourner le résultat à l'UI
                        callback.onSuccess(cloudUser);
                    });

                } else {
                    callback.onFailure("Erreur d'inscription : L'email existe peut-être déjà.");
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                callback.onFailure("Erreur réseau : " + t.getMessage());
            }
        });
    }

    public void loginUser(String email, String password, AuthCallback callback) {
        // 1. Préparer la requête JSON
        AuthRequest request = new AuthRequest(email, password);

        // 2. Envoyer la requête de connexion (Étape 1 : Auth)
        apiService.signIn(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getUser() != null) {
                    AuthResponse authResponse = response.body();
                    String userId = authResponse.getUser().getId();
                    String token = authResponse.getAccessToken();

                    // 3. LA NOUVELLE ÉTAPE : On interroge public.users avec l'ID
                    apiService.getPublicUserRole("eq." + userId).enqueue(new Callback<List<PublicUser>>() {
                        @Override
                        public void onResponse(Call<List<PublicUser>> call2, Response<List<PublicUser>> response2) {
                            if (response2.isSuccessful() && response2.body() != null && !response2.body().isEmpty()) {

                                // On extrait le VRAI rôle de la base de données publique
                                String realDatabaseRole = response2.body().get(0).getRole();

                                // On recrée l'utilisateur avec le rôle officiel du serveur
                                User cloudUser = new User(
                                        userId,
                                        authResponse.getUser().getNom(),
                                        email,
                                        realDatabaseRole // <-- Injection du rôle sécurisé
                                );
                                cloudUser.setToken(token);

                                // 4. Mettre à jour la base locale et renvoyer à l'UI
                                AppDatabase.databaseWriteExecutor.execute(() -> {
                                    userDao.insertUser(cloudUser);
                                    callback.onSuccess(cloudUser);
                                });

                            } else {
                                callback.onFailure("Erreur : Impossible de lire le rôle dans public.users");
                            }
                        }

                        @Override
                        public void onFailure(Call<List<PublicUser>> call2, Throwable t) {
                            callback.onFailure("Crash réseau lors de la lecture du rôle public");
                        }
                    });

                } else {
                    callback.onFailure("Email ou mot de passe incorrect");
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                callback.onFailure("Erreur réseau : impossible de se connecter au serveur");
            }
        });
    }
}