package com.example.foodsaver.ui;

import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodsaver.R;
import com.example.foodsaver.adapter.ClientPanierAdapter;
import com.example.foodsaver.repository.ReservationRepository;
import com.example.foodsaver.utils.NotificationHelper;
import com.example.foodsaver.utils.SessionManager;
import com.example.foodsaver.viewmodel.ClientViewModel;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Set;

public class ClientPaniersActivity extends AppCompatActivity {

    private ClientViewModel clientViewModel;
    private ClientPanierAdapter adapter;
    private String currentUserId;

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

        // Gérer le clic sur "Réserver"
        adapter.setOnPanierClickListener(panier -> {
            // NOUVEAU : Vérification du stock
            if (panier.getQuantite() <= 0) {
                Toast.makeText(this, "Désolé, ce panier est en rupture de stock !", Toast.LENGTH_SHORT).show();
                return;
            }

            clientViewModel.getTakenPickupTimesForPanier(panier.getId(), new ReservationRepository.PickupTimesCallback() {
                @Override
                public void onSuccess(Set<Long> takenPanierEpochMinutes) {
                    clientViewModel.getTakenPickupTimesForClient(currentUserId, new ReservationRepository.PickupTimesCallback() {
                        @Override
                        public void onSuccess(Set<Long> takenClientEpochMinutes) {
                            Set<Long> blockedMinutes = new java.util.HashSet<>(takenPanierEpochMinutes);
                            blockedMinutes.addAll(takenClientEpochMinutes);
                            showPickupDateTimePicker(panier, blockedMinutes);
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            Toast.makeText(ClientPaniersActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(ClientPaniersActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });


        // Écouter la base de données pour savoir quels paniers sont déjà réservés
        clientViewModel.getReservedPanierIds(currentUserId).observe(this, reservedIds -> {
            adapter.setReservedPanierIds(reservedIds);
        });

        // NOUVEAU : On observe la liste des paniers pour ce commerce !
        clientViewModel.getPaniersByCommerce(commerceId).observe(this, paniers -> {
            adapter.setPaniers(paniers);
        });
    }

    private void showPickupDateTimePicker(com.example.foodsaver.data.model.Panier panier, Set<Long> takenEpochMinutes) {
        java.time.LocalDateTime now = LocalDateTime.now();

        android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(
                this,
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    LocalDate selectedDate = LocalDate.of(year, month + 1, dayOfMonth);

                    android.app.TimePickerDialog timePickerDialog = new android.app.TimePickerDialog(
                            this,
                            (TimePicker timeView, int hourOfDay, int minute) -> {
                                LocalTime selectedTime = LocalTime.of(hourOfDay, minute);
                                LocalDateTime pickupLocal = LocalDateTime.of(selectedDate, selectedTime);

                                if (!pickupLocal.isAfter(now.plusMinutes(1))) {
                                    Toast.makeText(this, "L'heure de retrait doit etre dans le futur.", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                String pickupUtcIso = pickupLocal
                                        .atZone(ZoneId.systemDefault())
                                        .withZoneSameInstant(ZoneId.of("UTC"))
                                        .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

                                long selectedEpochMinute;
                                try {
                                    selectedEpochMinute = Instant.parse(pickupUtcIso).getEpochSecond() / 60L;
                                } catch (Exception e) {
                                    Toast.makeText(this, "Heure de retrait invalide.", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                if (takenEpochMinutes.contains(selectedEpochMinute)) {
                                                                    Toast.makeText(this, "Creneau indisponible (deja pris ou vous avez deja un retrait a cette heure).", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                openPaymentAndReserve(panier, pickupUtcIso);
                            },
                            now.getHour(),
                            now.getMinute(),
                            true
                    );
                    timePickerDialog.show();
                },
                now.getYear(),
                now.getMonthValue() - 1,
                now.getDayOfMonth()
        );
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void openPaymentAndReserve(com.example.foodsaver.data.model.Panier panier, String pickupUtcIso) {
        PaymentBottomSheetFragment paymentSheet = new PaymentBottomSheetFragment(() -> {
            clientViewModel.reserverPanier(currentUserId, panier.getId(), pickupUtcIso, new ReservationRepository.ReservationActionCallback() {
                @Override
                public void onSuccess() {
                    String localDisplayTime;
                    try {
                        localDisplayTime = Instant.parse(pickupUtcIso)
                                .atZone(ZoneId.systemDefault())
                                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                    } catch (Exception e) {
                        localDisplayTime = pickupUtcIso;
                    }
                    Toast.makeText(ClientPaniersActivity.this, "Retrait confirme pour : " + localDisplayTime, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onConflict(String message) {
                    Toast.makeText(ClientPaniersActivity.this, message, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(String message) {
                    Toast.makeText(ClientPaniersActivity.this, message, Toast.LENGTH_LONG).show();
                }
            });
        });

        paymentSheet.show(getSupportFragmentManager(), "PaymentBottomSheet");
    }
}
