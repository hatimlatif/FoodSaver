package com.example.foodsaver.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodsaver.R;
import com.example.foodsaver.adapter.ClientPanierAdapter;
import com.example.foodsaver.utils.SessionManager;
import com.example.foodsaver.viewmodel.ClientViewModel;

public class ClientPaniersActivity extends AppCompatActivity {

    private ClientViewModel clientViewModel;
    private ClientPanierAdapter adapter;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_paniers);

        // Récupérer les infos envoyées par le Dashboard
        int commerceId = getIntent().getIntExtra("COMMERCE_ID", -1);
        String commerceNom = getIntent().getStringExtra("COMMERCE_NOM");

        // Mettre à jour le titre
        TextView tvHeader = findViewById(R.id.tvCommerceNomHeader);
        if (commerceNom != null) {
            tvHeader.setText(commerceNom);
        }

        // Gérer le bouton Retour
        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        SessionManager sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getUserId();

        RecyclerView recyclerView = findViewById(R.id.recyclerCommercePaniers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // On réutilise l'adapter que l'on avait créé pour les paniers clients !
        adapter = new ClientPanierAdapter();
        recyclerView.setAdapter(adapter);

        clientViewModel = new ViewModelProvider(this).get(ClientViewModel.class);

        // Charger UNIQUEMENT les paniers de ce commerce spécifique
        if (commerceId != -1) {
            clientViewModel.getPaniersByCommerce(commerceId).observe(this, paniers -> {
                adapter.setPaniers(paniers);
            });
        }

        // Gérer le clic sur "Réserver"
        adapter.setOnPanierClickListener(panier -> {
            clientViewModel.reserverPanier(currentUserId, panier.getId());
            Toast.makeText(this, "Réservation effectuée pour : " + panier.getTitre(), Toast.LENGTH_SHORT).show();
            // Optionnel : Vous pourriez faire un finish() ici si vous voulez que ça retourne au dashboard après réservation
        });

        // Écouter la base de données pour savoir quels paniers sont déjà réservés
        clientViewModel.getReservedPanierIds(currentUserId).observe(this, reservedIds -> {
            adapter.setReservedPanierIds(reservedIds);
        });
    }
}