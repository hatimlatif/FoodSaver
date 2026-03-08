package com.example.foodsaver.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodsaver.R;
import com.example.foodsaver.adapter.AdminCommerceAdapter;
import com.example.foodsaver.utils.SessionManager;
import com.example.foodsaver.viewmodel.ClientViewModel; // On réutilise ça pour lire les commerces !

public class AdminDashboardActivity extends AppCompatActivity {

    private ClientViewModel clientViewModel;
    private AdminCommerceAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        RecyclerView recyclerView = findViewById(R.id.recyclerAdminCommerces);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AdminCommerceAdapter();
        recyclerView.setAdapter(adapter);

        clientViewModel = new ViewModelProvider(this).get(ClientViewModel.class);

        // 1. Charger tous les commerces pour l'Admin
        clientViewModel.getAllCommerces().observe(this, commerces -> {
            adapter.setCommerces(commerces);
        });

        // 2. Supprimer un commerce (Appelle directement le Repository pour aller vite)
        adapter.setOnDeleteClickListener(commerce -> {
            com.example.foodsaver.repository.CommerceRepository repo = new com.example.foodsaver.repository.CommerceRepository(getApplication());
            repo.deleteCommerceAdmin(commerce);
            Toast.makeText(this, commerce.getNom() + " banni avec succès", Toast.LENGTH_SHORT).show();
        });

        // 3. Déconnexion
        Button btnLogout = findViewById(R.id.btnAdminLogout);
        btnLogout.setOnClickListener(v -> {
            SessionManager sessionManager = new SessionManager(this);
            sessionManager.logoutUser();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}