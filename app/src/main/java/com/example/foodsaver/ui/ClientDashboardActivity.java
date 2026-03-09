package com.example.foodsaver.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodsaver.R;
import com.example.foodsaver.adapter.CommerceAdapter;
import com.example.foodsaver.utils.NotificationHelper;
import com.example.foodsaver.utils.SessionManager;
import com.example.foodsaver.viewmodel.ClientViewModel;

public class ClientDashboardActivity extends AppCompatActivity {

    private ClientViewModel clientViewModel;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_dashboard);

        sessionManager = new SessionManager(this);

        // NOUVEAU: On affiche uniquement les Commerces sur cette page !
        RecyclerView recyclerView = findViewById(R.id.recyclerViewPaniers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        CommerceAdapter commerceAdapter = new CommerceAdapter();
        recyclerView.setAdapter(commerceAdapter);

        clientViewModel = new ViewModelProvider(this).get(ClientViewModel.class);

        // Canal de notification
        NotificationHelper.createNotificationChannel(this);

        // NOUVEAU : Demander la permission sur Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            String notificationPermission = android.Manifest.permission.POST_NOTIFICATIONS;
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, notificationPermission) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this, new String[]{notificationPermission}, 101);
            }
        }

        clientViewModel.getAllCommerces().observe(this, commerces -> {

            commerceAdapter.setCommerces(commerces);
        });

        // NOUVEAU : On s'assure que les paniers sont aussi frais pour les notifications
        clientViewModel.getAllPaniers().observe(this, paniers -> {
            // On n'affiche rien ici, c'est juste pour forcer le refresh et les notifications
        });

        // REFRESH AUTOMATIQUE (Toutes les 5 secondes)
        android.os.Handler handler = new android.os.Handler();
        Runnable refreshRunnable = new Runnable() {
            @Override
            public void run() {
                clientViewModel.getAllCommerces(); // Trigger refresh
                clientViewModel.getAllPaniers();   // Trigger refresh
                handler.postDelayed(this, 5000); // 5 secondes
            }
        };
        handler.postDelayed(refreshRunnable, 5000);


        // Quand on clique sur un commerce, on ouvre la page de ses paniers
        commerceAdapter.setOnCommerceClickListener(commerce -> {
            Intent intent = new Intent(ClientDashboardActivity.this, ClientPaniersActivity.class);
            intent.putExtra("COMMERCE_ID", commerce.getId());
            intent.putExtra("COMMERCE_NOM", commerce.getNom());
            startActivity(intent);
        });

        Button btnMesReservations = findViewById(R.id.btnMesReservations);
        btnMesReservations.setOnClickListener(v -> {
            startActivity(new Intent(ClientDashboardActivity.this, MesReservationsActivity.class));
        });

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            sessionManager.logoutUser();
            startActivity(new Intent(ClientDashboardActivity.this, LoginActivity.class));
            finish();
        });
    }
}