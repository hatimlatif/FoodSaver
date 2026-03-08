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

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReservationRepository {
    private ReservationDao reservationDao;
    private SupabaseApiService apiService;

    public ReservationRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        reservationDao = db.reservationDao();
        apiService = ApiClient.getClient().create(SupabaseApiService.class);
    }

    // --- LA MÉTHODE DE SYNCHRONISATION ---
    private void refreshReservations() {
        apiService.getReservationsFromCloud().enqueue(new Callback<List<Reservation>>() {
            @Override
            public void onResponse(Call<List<Reservation>> call, Response<List<Reservation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        // Écrase les anciennes données locales par les nouvelles du cloud
                        reservationDao.insertAllReservations(response.body());
                    });
                }
            }

            @Override
            public void onFailure(Call<List<Reservation>> call, Throwable t) {
                Log.e("SyncError", "Erreur de synchronisation des réservations: " + t.getMessage());
            }
        });
    }

    public LiveData<List<Reservation>> getReservationsByClient(String clientId) {
        refreshReservations(); // Sync before reading
        return reservationDao.getReservationsByClient(clientId);
    }

    public LiveData<List<ReservationDetails>> getHistoriqueClient(String clientId) {
        refreshReservations(); // Sync before reading
        return reservationDao.getHistoriqueClient(clientId);
    }

    public LiveData<List<ReservationDetails>> getReservationsRecues(String commercantId) {
        refreshReservations(); // Sync before reading
        return reservationDao.getReservationsRecues(commercantId);
    }

    public LiveData<List<Integer>> getReservedPanierIds(String clientId) {
        refreshReservations(); // Sync before reading
        return reservationDao.getReservedPanierIds(clientId);
    }

    public LiveData<Statistiques> getStatistiquesCommercant(String commercantId) {
        return reservationDao.getStatistiquesCommercant(commercantId);
    }

    public void insertReservation(Reservation reservation) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            reservation.setIsSynced(0); // Offline mode default
            long id = reservationDao.insertReservation(reservation);
            reservation.setId((int) id);

            ReservationRequest request = new ReservationRequest(reservation);
            apiService.insertReservationCloud(request).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        reservation.setIsSynced(1);
                        AppDatabase.databaseWriteExecutor.execute(() -> reservationDao.updateReservation(reservation));
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
            // Suppression locale
            reservationDao.annulerReservation(reservationId);

            // Suppression dans le Cloud Supabase
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