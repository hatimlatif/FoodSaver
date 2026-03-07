package com.example.foodsaver.ui;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodsaver.R;
import com.example.foodsaver.adapter.ReservationAdapter;
import com.example.foodsaver.utils.SessionManager;
import com.example.foodsaver.viewmodel.CommercantViewModel;

public class CommercantReservationsActivity extends AppCompatActivity {

    private CommercantViewModel commercantViewModel;
    private ReservationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commercant_reservations);

        SessionManager sessionManager = new SessionManager(this);
        int commercantId = sessionManager.getUserId();

        RecyclerView recyclerView = findViewById(R.id.recyclerReservationsRecues);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ReservationAdapter();
        // We do NOT set the cancel listener here, so the button won't do anything for the Commerçant.
        // (In a real app, you might hide the button entirely inside the adapter based on the role).
        recyclerView.setAdapter(adapter);

        commercantViewModel = new ViewModelProvider(this).get(CommercantViewModel.class);

        commercantViewModel.getReservationsRecues(commercantId).observe(this, reservations -> {
            adapter.setReservations(reservations);
        });

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            finish(); // Closes this screen and returns to the Dashboard
        });
    }
}