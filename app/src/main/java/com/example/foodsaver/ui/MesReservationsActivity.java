package com.example.foodsaver.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodsaver.R;
import com.example.foodsaver.adapter.ReservationAdapter;
import com.example.foodsaver.utils.SessionManager;
import com.example.foodsaver.viewmodel.ClientViewModel;

public class MesReservationsActivity extends AppCompatActivity {

    private ClientViewModel clientViewModel;
    private ReservationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mes_reservations);

        // Fetching the user from SharedPreferences!
        SessionManager sessionManager = new SessionManager(this);
        String clientId = sessionManager.getUserId();

        RecyclerView recyclerView = findViewById(R.id.recyclerReservations);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReservationAdapter();
        recyclerView.setAdapter(adapter);

        clientViewModel = new ViewModelProvider(this).get(ClientViewModel.class);

        // Auto-updating list of history
        clientViewModel.getMesReservations(clientId).observe(this, reservations -> {
            adapter.setReservations(reservations);
        });

        // Cancel booking
        adapter.setOnCancelClickListener(reservationId -> {
            clientViewModel.annulerReservation(reservationId);
            Toast.makeText(this, "Réservation annulée", Toast.LENGTH_SHORT).show();
        });

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            finish(); // Closes this screen and returns to the Dashboard
        });
    }
}