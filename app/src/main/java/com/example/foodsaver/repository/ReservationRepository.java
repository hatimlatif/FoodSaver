package com.example.foodsaver.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.foodsaver.api.ApiClient;
import com.example.foodsaver.api.SupabaseApiService;
import com.example.foodsaver.data.AppDatabase;
import com.example.foodsaver.data.model.Reservation;
import com.example.foodsaver.data.local.ReservationDao;
import com.example.foodsaver.data.model.ReservationDetails;
import com.example.foodsaver.data.model.Statistiques;
import com.example.foodsaver.data.network.ReservationRequest;
import com.example.foodsaver.utils.NotificationHelper;
import com.example.foodsaver.utils.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReservationRepository {
    private ReservationDao reservationDao;
    private SupabaseApiService apiService;
    private Application application; // ADDED: Context for notifications

    public ReservationRepository(Application application) {
        this.application = application;
        AppDatabase db = AppDatabase.getDatabase(application);
        reservationDao = db.reservationDao();
        apiService = ApiClient.getClient().create(SupabaseApiService.class);
    }

    public void refreshReservations() {
        apiService.getReservationsFromCloud().enqueue(new Callback<List<Reservation>>() {
            @Override
            public void onResponse(Call<List<Reservation>> call, Response<List<Reservation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Reservation> cloudReservations = response.body();

                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        // 1. Get local IDs for comparison
                        List<Reservation> locals = reservationDao.getAllReservationsSync();
                        java.util.Set<Integer> localIds = new java.util.HashSet<>();
                        if (locals != null) {
                            for (Reservation r : locals) localIds.add(r.getId());
                        }

                        // 2. Identify local reservations that should be deleted (missing from cloud)
                        if (locals != null) {
                            java.util.Set<Integer> cloudIds = new java.util.HashSet<>();
                            for (Reservation r : cloudReservations) cloudIds.add(r.getId());

                            for (Reservation local : locals) {
                                if (local.getIsSynced() == 1 && !cloudIds.contains(local.getId())) {
                                    reservationDao.annulerReservation(local.getId());
                                }
                            }
                        }

                        // 3. PRO TRIGGER: Check for REALLY new reservations (by ID)
                        boolean hasNew = false;
                        for (Reservation r : cloudReservations) {
                            if (!localIds.contains(r.getId())) {
                                hasNew = true;
                                break;
                            }
                        }

                        if (hasNew && locals != null) {
                            SessionManager session = new SessionManager(application);
                            if ("COMMERCANT".equalsIgnoreCase(session.getUserRole())) {
                                NotificationHelper.showNewReservationNotification(application);
                            }
                        }




                        // --- UPSERT LOGIC (No delete) ---
                        for (Reservation r : cloudReservations) {
                            r.setIsSynced(1);
                        }
                        reservationDao.insertAllReservations(cloudReservations);
                    });
                }
            }

            @Override
            public void onFailure(Call<List<Reservation>> call, Throwable t) {
                Log.e("SyncError", "Erreur de synchronisation: " + t.getMessage());
            }
        });
    }

    public LiveData<List<Reservation>> getReservationsByClient(String clientId) {
        refreshReservations();
        return reservationDao.getReservationsByClient(clientId);
    }

    public LiveData<List<ReservationDetails>> getHistoriqueClient(String clientId) {
        refreshReservations();
        return reservationDao.getHistoriqueClient(clientId);
    }

    public LiveData<List<ReservationDetails>> getReservationsRecues(String commercantId) {
        refreshReservations();
        return reservationDao.getReservationsRecues(commercantId);
    }

    public LiveData<List<Integer>> getReservedPanierIds(String clientId) {
        refreshReservations();
        return reservationDao.getReservedPanierIds(clientId);
    }

    public LiveData<Statistiques> getStatistiquesCommercant(String commercantId) {
        refreshReservations();
        return reservationDao.getStatistiquesCommercant(commercantId);
    }


    public void insertReservation(Reservation reservation) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            reservation.setIsSynced(0);
            long id = reservationDao.insertReservation(reservation);
            reservation.setId((int) id);

            ReservationRequest request = new ReservationRequest(reservation);
            apiService.insertReservationCloud(request).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        reservation.setIsSynced(1);
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            reservationDao.updateReservation(reservation);
                            // NOUVEAU : Décrémenter le stock !
                            new PanierRepository(application).decrementQuantity(reservation.getPanierId());
                        });
                    }
                }


                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                }
            });
        });
    }

    public void annulerReservation(int reservationId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // 1. Suppression physique locale
            reservationDao.annulerReservation(reservationId);

            // 2. Suppression Cloud
            String filter = "eq." + reservationId;
            apiService.deleteReservationCloud(filter).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                }
            });
        });
    }

}