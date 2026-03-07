package com.example.foodsaver.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.foodsaver.R;
import com.example.foodsaver.utils.SessionManager;
import com.example.foodsaver.viewmodel.AuthViewModel;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;
    private SessionManager sessionManager;
    private TextInputEditText etEmail, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize SessionManager first to check if already logged in
        sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            navigateToDashboard(sessionManager.getRole());
            return; // Stop the activity from drawing the login screen
        }

        setContentView(R.layout.activity_login);

        // Map XML elements to Java objects
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvGoToRegister = findViewById(R.id.tvGoToRegister);

        // Initialize the ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Observe the ViewModel for a successful login
        authViewModel.getAuthenticatedUser().observe(this, user -> {
            if (user != null) {
                // Save the session using SharedPreferences
                sessionManager.createLoginSession(user.getId(), user.getRole());
                Toast.makeText(LoginActivity.this, "Connexion réussie", Toast.LENGTH_SHORT).show();
                navigateToDashboard(user.getRole());
            }
        });

        // Observe the ViewModel for errors (like wrong password)
        authViewModel.getAuthError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });

        // Handle button clicks
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }
            // Trigger the background Room database check
            authViewModel.login(email, password);
        });

        tvGoToRegister.setOnClickListener(v -> {
            // Use an Intent to move to the Register screen
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void navigateToDashboard(String role) {
        Intent intent;
        if ("COMMERCANT".equals(role)) {
            intent = new Intent(LoginActivity.this, CommercantDashboardActivity.class);
        } else {
            intent = new Intent(LoginActivity.this, ClientDashboardActivity.class);
        }
        startActivity(intent);

        // Call finish() so the user cannot press the "Back" button to return to the login screen
        finish();
    }
}