package com.example.foodsaver.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.foodsaver.R;
import com.example.foodsaver.data.model.User;
import com.example.foodsaver.utils.SessionManager;
import com.example.foodsaver.viewmodel.AuthViewModel;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;
    private SessionManager sessionManager;
    private TextInputEditText etNom, etEmail, etPassword;
    private RadioGroup radioGroupRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        sessionManager = new SessionManager(this);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        etNom = findViewById(R.id.etNom);
        etEmail = findViewById(R.id.etRegEmail);
        etPassword = findViewById(R.id.etRegPassword);
        radioGroupRole = findViewById(R.id.radioGroupRole);
        Button btnRegister = findViewById(R.id.btnRegister);
        TextView tvGoToLogin = findViewById(R.id.tvGoToLogin);

        // V2 Update: Now expects the Supabase Auth token
        authViewModel.getAuthenticatedUser().observe(this, user -> {
            if (user != null) {
                // Instantly log them in using the new SessionManager signature
                sessionManager.createLoginSession(user.getId(), user.getRole(), user.getToken());
                Toast.makeText(RegisterActivity.this, "Compte créé avec succès !", Toast.LENGTH_SHORT).show();
                navigateToDashboard(user.getRole());
            }
        });

        authViewModel.getAuthError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });

        btnRegister.setOnClickListener(v -> {
            String nom = etNom.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (nom.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedId = radioGroupRole.getCheckedRadioButtonId();
            RadioButton selectedRadioButton = findViewById(selectedId);
            String roleStr = selectedRadioButton.getText().toString().toUpperCase();

            String role = roleStr.equals("CLIENT") ? "CLIENT" : "COMMERCANT";

            // CORRECTION : On met un ID temporaire ("") et on passe le mot de passe via le setter
            User newUser = new User("", nom, email, role);
            newUser.setToken(password);

            authViewModel.register(newUser);
        });

        tvGoToLogin.setOnClickListener(v -> finish());
    }

    private void navigateToDashboard(String role) {
        Intent intent;
        if ("COMMERCANT".equals(role)) {
            intent = new Intent(RegisterActivity.this, CommercantDashboardActivity.class);
        } else {
            intent = new Intent(RegisterActivity.this, ClientDashboardActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}