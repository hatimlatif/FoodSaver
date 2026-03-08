package com.example.foodsaver.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;

import com.example.foodsaver.R;
import com.example.foodsaver.utils.SessionManager;
import com.example.foodsaver.viewmodel.AuthViewModel;
import com.example.foodsaver.worker.SyncWorker;
import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;
    private SessionManager sessionManager;
    private TextInputEditText etEmail, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest syncRequest = new PeriodicWorkRequest.Builder(
                SyncWorker.class, 15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build();

        androidx.work.WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "SupabaseSync",
                ExistingPeriodicWorkPolicy.KEEP, // Keep existing if already running
                syncRequest
        );

        sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            navigateToDashboard(sessionManager.getUserRole()); // Make sure this matches your SessionManager getter
            return;
        }

        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvGoToRegister = findViewById(R.id.tvGoToRegister);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // V2 Update: Grab the token from the user object
        authViewModel.getAuthenticatedUser().observe(this, user -> {
            if (user != null) {
                // Save the session using SharedPreferences with the cloud token
                sessionManager.createLoginSession(user.getId(), user.getRole(), user.getToken());
                Toast.makeText(LoginActivity.this, "Connexion réussie", Toast.LENGTH_SHORT).show();
                navigateToDashboard(user.getRole());
            }
        });

        authViewModel.getAuthError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }
            // Trigger the ViewModel to handle the Supabase network call
            authViewModel.login(email, password);
        });

        tvGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void navigateToDashboard(String role) {
        Intent intent;
        if ("ADMIN".equalsIgnoreCase(role)) {
            intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
        } else if ("COMMERCANT".equalsIgnoreCase(role)) {
            intent = new Intent(LoginActivity.this, CommercantDashboardActivity.class);
        } else {
            intent = new Intent(LoginActivity.this, ClientDashboardActivity.class);
        }
        startActivity(intent);
        finish();
    }
}