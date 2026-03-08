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
import com.example.foodsaver.utils.NotificationHelper;
import com.example.foodsaver.utils.SessionManager;
import com.example.foodsaver.viewmodel.ClientViewModel;

public class ClientPaniersActivity extends AppCompatActivity {

    private ClientViewModel clientViewModel;
    private ClientPanierAdapter adapter;
    private String currentUserId;
    private int previousPanierCount = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_paniers);
        NotificationHelper.createNotificationChannel(this);

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

                // LA MAGIE DES NOTIFICATIONS :
                if (previousPanierCount != -1 && paniers.size() > previousPanierCount) {
                    // Si la taille de la liste a augmenté, on déclenche la notification !
                    String nouveauTitre = paniers.get(paniers.size() - 1).getTitre();
                    NotificationHelper.showNewPanierNotification(this, nouveauTitre);
                }
                previousPanierCount = paniers.size(); // On met à jour le compteur
            });
        }

        // Gérer le clic sur "Réserver"
        adapter.setOnPanierClickListener(panier -> {

            // 1. Instancier le Bottom Sheet avec le callback de succès
            PaymentBottomSheetFragment paymentSheet = new PaymentBottomSheetFragment(() -> {
                // 2. CE CODE S'EXÉCUTE UNIQUEMENT SI LE PAIEMENT RÉUSSIT
                clientViewModel.reserverPanier(currentUserId, panier.getId());
                Toast.makeText(this, "Réservation confirmée pour : " + panier.getTitre(), Toast.LENGTH_SHORT).show();
            });

            // 3. Afficher le Bottom Sheet à l'écran
            paymentSheet.show(getSupportFragmentManager(), "PaymentBottomSheet");
        });

        // Écouter la base de données pour savoir quels paniers sont déjà réservés
        clientViewModel.getReservedPanierIds(currentUserId).observe(this, reservedIds -> {
            adapter.setReservedPanierIds(reservedIds);
        });
    }
}