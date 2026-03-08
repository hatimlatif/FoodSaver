package com.example.foodsaver.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodsaver.R;
import com.example.foodsaver.adapter.PanierAdapter;
import com.example.foodsaver.data.model.Panier;
import com.example.foodsaver.utils.NotificationHelper;
import com.example.foodsaver.utils.SessionManager;
import com.example.foodsaver.viewmodel.CommercantViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class CommercantDashboardActivity extends AppCompatActivity {

    private CommercantViewModel commercantViewModel;
    private SessionManager sessionManager;
    private PanierAdapter adapter;
    private String currentUserId;
    private int currentCommerceId = -1; // CORRECTION: Variable declared here!
    private int previousReservationCount = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commercant_dashboard);

        // 1. INITIALISATION DU VIEWMODEL EN PREMIER !
        commercantViewModel = new ViewModelProvider(this).get(CommercantViewModel.class);

        TextView tvRevenus = findViewById(R.id.tvStatsRevenus);
        TextView tvVendus = findViewById(R.id.tvStatsVendus);

        sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getUserId();

        // 2. MAINTENANT ON PEUT L'UTILISER SANS CRASH
        commercantViewModel.getStatistiquesCommercant(currentUserId).observe(this, stats -> {
            if (stats != null) {
                tvRevenus.setText("Revenus: " + stats.totalRevenus + " DH");
                tvVendus.setText("Paniers Vendus: " + stats.paniersVendus);
            }
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerViewPaniers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PanierAdapter();
        recyclerView.setAdapter(adapter);

        // CORRECTION: We ONLY load the Commerce, and then load the Paniers based on that.
        commercantViewModel.getMonCommerce(currentUserId).observe(this, commerces -> {
            if (commerces == null || commerces.isEmpty()) {
                showCreateCommerceDialog();
            } else {
                currentCommerceId = commerces.get(0).getId();
                commercantViewModel.getMesPaniers(currentCommerceId).observe(this, paniers -> adapter.setPaniers(paniers));
            }
        });

        adapter.setOnPanierActionClickListener(new PanierAdapter.OnPanierActionClickListener() {
            @Override
            public void onEditClick(Panier panier) {
                showEditPanierDialog(panier);
            }

            @Override
            public void onDeleteClick(Panier panier) {
                commercantViewModel.supprimerPanier(panier);
                Toast.makeText(CommercantDashboardActivity.this, "Panier supprimé", Toast.LENGTH_SHORT).show();
            }
        });

        FloatingActionButton fabAddPanier = findViewById(R.id.fabAddPanier);
        fabAddPanier.setOnClickListener(v -> {
            if (currentCommerceId != -1) showAddPanierDialog(currentCommerceId);
        });

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            sessionManager.logoutUser();
            startActivity(new Intent(CommercantDashboardActivity.this, LoginActivity.class));
            finish();
        });

        Button btnVoirReservations = findViewById(R.id.btnVoirReservations);
        btnVoirReservations.setOnClickListener(v -> {
            startActivity(new Intent(CommercantDashboardActivity.this, CommercantReservationsActivity.class));
        });

        // 1. Initialiser le canal (au cas où il n'existe pas encore)
        NotificationHelper.createNotificationChannel(this);

        // 2. Observer les réservations pour déclencher la notification
        commercantViewModel.getReservationsRecues(currentUserId).observe(this, reservations -> {
            if (previousReservationCount != -1 && reservations.size() > previousReservationCount) {
                // Le nombre de réservations a augmenté !
                NotificationHelper.showNewReservationNotification(this);
            }
            previousReservationCount = reservations.size();
        });
    }

    private void showEditPanierDialog(Panier panier) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Modifier le panier");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        final EditText etTitre = new EditText(this);
        etTitre.setText(panier.getTitre());
        layout.addView(etTitre);

        final EditText etPrix = new EditText(this);
        etPrix.setText(String.valueOf(panier.getPrix()));
        etPrix.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(etPrix);

        final EditText etQuantite = new EditText(this);
        etQuantite.setText(String.valueOf(panier.getQuantite()));
        etQuantite.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(etQuantite);

        builder.setView(layout);

        builder.setPositiveButton("Enregistrer", (dialog, which) -> {
            try {
                panier.setTitre(etTitre.getText().toString().trim());
                panier.setPrix(Double.parseDouble(etPrix.getText().toString().trim()));
                panier.setQuantite(Integer.parseInt(etQuantite.getText().toString().trim()));

                commercantViewModel.modifierPanier(panier);
                Toast.makeText(this, "Panier mis à jour !", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Erreur de saisie", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    // CORRECTION: Method now accepts the commerceId parameter
    private void showAddPanierDialog(int commerceId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ajouter un nouveau panier");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        final EditText etTitre = new EditText(this);
        etTitre.setHint("Titre du panier (ex: Panier Boulangerie)");
        layout.addView(etTitre);

        final EditText etPrix = new EditText(this);
        etPrix.setHint("Prix (DH)");
        etPrix.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(etPrix);

        final EditText etQuantite = new EditText(this);
        etQuantite.setHint("Quantité disponible");
        etQuantite.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(etQuantite);

        builder.setView(layout);

        builder.setPositiveButton("Ajouter", (dialog, which) -> {
            String titre = etTitre.getText().toString().trim();
            String prixStr = etPrix.getText().toString().trim();
            String quantiteStr = etQuantite.getText().toString().trim();

            if (titre.isEmpty() || prixStr.isEmpty() || quantiteStr.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double prix = Double.parseDouble(prixStr);
                int quantite = Integer.parseInt(quantiteStr);

                // CORRECTION: Uses the specific commerceId now!
                commercantViewModel.ajouterPanier(titre, prix, quantite, commerceId);
                Toast.makeText(this, "Panier ajouté !", Toast.LENGTH_SHORT).show();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Prix ou quantité invalide", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showCreateCommerceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Configurez votre Magasin");
        builder.setCancelable(false); // Force la création

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        final EditText etNom = new EditText(this);
        etNom.setHint("Nom du Commerce");
        layout.addView(etNom);

        final EditText etAdresse = new EditText(this);
        etAdresse.setHint("Adresse");
        layout.addView(etAdresse);

        builder.setView(layout);
        builder.setPositiveButton("Créer", (dialog, which) -> {
            commercantViewModel.creerCommerce(etNom.getText().toString(), etAdresse.getText().toString(), currentUserId);
        });
        builder.show();
    }
}