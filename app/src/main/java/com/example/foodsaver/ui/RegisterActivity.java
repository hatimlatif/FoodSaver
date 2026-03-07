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
import com.example.foodsaver.data.User;
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

        // Map UI components
        etNom = findViewById(R.id.etNom);
        etEmail = findViewById(R.id.etRegEmail);
        etPassword = findViewById(R.id.etRegPassword);
        radioGroupRole = findViewById(R.id.radioGroupRole);
        Button btnRegister = findViewById(R.id.btnRegister);
        TextView tvGoToLogin = findViewById(R.id.tvGoToLogin);

        // Observe successful registration
        authViewModel.getAuthenticatedUser().observe(this, user -> {
            if (user != null) {
                // Instantly log them in using SharedPreferences [cite: 15]
                sessionManager.createLoginSession(user.getId(), user.getRole());
                Toast.makeText(RegisterActivity.this, "Compte créé avec succès !", Toast.LENGTH_SHORT).show();
                navigateToDashboard(user.getRole());
            }
        });

        // Observe errors (like email already exists)
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

            // Determine which role was selected
            int selectedId = radioGroupRole.getCheckedRadioButtonId();
            RadioButton selectedRadioButton = findViewById(selectedId);
            String roleStr = selectedRadioButton.getText().toString().toUpperCase();

            // Format the string to match our DB constants
            String role = roleStr.equals("CLIENT") ? "CLIENT" : "COMMERCANT";

            // Create the new User object and send to Room
            User newUser = new User(nom, email, password, role);
            authViewModel.register(newUser);
        });

        tvGoToLogin.setOnClickListener(v -> {
            // Simply close this activity to reveal the LoginActivity underneath it
            finish();
        });
    }

    private void navigateToDashboard(String role) {
        Intent intent;
        if ("COMMERCANT".equals(role)) {
            intent = new Intent(RegisterActivity.this, CommercantDashboardActivity.class);
        } else {
            intent = new Intent(RegisterActivity.this, ClientDashboardActivity.class);
        }
        // Clear the backstack so they can't press 'Back' to go to the registration page
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}