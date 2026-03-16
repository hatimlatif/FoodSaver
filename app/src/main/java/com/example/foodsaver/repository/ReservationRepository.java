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
import java.util.Set;
import java.util.HashSet;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

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

    public interface PickupTimesCallback {
        void onSuccess(Set<Long> takenEpochMinutes);

        void onFailure(String errorMessage);
    }

    public interface ReservationActionCallback {
        void onSuccess();

        void onConflict(String message);

        void onFailure(String message);
    }

    public void getTakenPickupTimesForPanier(int panierId, PickupTimesCallback callback) {
        String nowUtc = "gte." + DateTimeFormatter.ISO_INSTANT.format(Instant.now());
        apiService.getTakenPickupTimesForPanier(
                "eq." + panierId,
                "eq.CONFIRMÉE",
                nowUtc
        ).enqueue(new Callback<List<Reservation>>() {
            @Override
            public void onResponse(Call<List<Reservation>> call, Response<List<Reservation>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onFailure("Impossible de verifier les creneaux pour le moment.");
                    return;
                }

                Set<Long> takenMinutes = new HashSet<>();
                for (Reservation row : response.body()) {
                    try {
                        if (row.getPickupTime() != null) {
                            takenMinutes.add(Instant.parse(row.getPickupTime()).getEpochSecond() / 60L);
                        }
                    } catch (Exception ignored) {
                    }
                }

                callback.onSuccess(takenMinutes);
            }

            @Override
            public void onFailure(Call<List<Reservation>> call, Throwable t) {
                callback.onFailure("Erreur reseau lors de la verification des creneaux.");
            }
        });
    }

    public void getTakenPickupTimesForClient(String clientId, PickupTimesCallback callback) {
        String nowUtc = "gte." + DateTimeFormatter.ISO_INSTANT.format(Instant.now());
        apiService.getTakenPickupTimesForClient(
                "eq." + clientId,
                "eq.CONFIRMÉE",
                nowUtc
        ).enqueue(new Callback<List<Reservation>>() {
            @Override
            public void onResponse(Call<List<Reservation>> call, Response<List<Reservation>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onFailure("Impossible de verifier vos creneaux pour le moment.");
                    return;
                }

                Set<Long> takenMinutes = new HashSet<>();
                for (Reservation row : response.body()) {
                    try {
                        if (row.getPickupTime() != null) {
                            takenMinutes.add(Instant.parse(row.getPickupTime()).getEpochSecond() / 60L);
                        }
                    } catch (Exception ignored) {
                    }
                }

                callback.onSuccess(takenMinutes);
            }

            @Override
            public void onFailure(Call<List<Reservation>> call, Throwable t) {
                callback.onFailure("Erreur reseau lors de la verification de vos creneaux.");
            }
        });
    }

    private String mapConflictMessage(Response<Void> response) {
        String defaultMessage = "Ce creneau est indisponible. Choisissez une autre heure.";
        try {
            if (response.errorBody() == null) return defaultMessage;
            String error = response.errorBody().string();
            if (error.contains("reservations_unique_client_pickup_confirmed_idx")) {
                return "Vous avez deja un retrait confirme a cette heure.";
            }
            if (error.contains("reservations_unique_panier_pickup_confirmed_idx")) {
                return "Ce creneau est deja reserve. Choisissez une autre heure.";
            }
        } catch (Exception ignored) {
        }
        return defaultMessage;
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


    public void insertReservation(Reservation reservation, ReservationActionCallback callback) {
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
                        if (callback != null) callback.onSuccess();
                    } else {
                        AppDatabase.databaseWriteExecutor.execute(() -> reservationDao.annulerReservation(reservation.getId()));
                        refreshReservations();
                        Log.e("ReservationConflict", "Insert rejected by cloud: HTTP " + response.code());
                        if (callback != null) {
                            if (response.code() == 409) {
                                callback.onConflict(mapConflictMessage(response));
                            } else {
                                callback.onFailure("Reservation refusee par le serveur (" + response.code() + ").");
                            }
                        }
                    }
                }


                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    AppDatabase.databaseWriteExecutor.execute(() -> reservationDao.annulerReservation(reservation.getId()));
                    if (callback != null) callback.onFailure("Erreur reseau pendant la reservation.");
                }
            });
        });
    }

    public void annulerReservation(int reservationId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            Reservation existing = reservationDao.getReservationByIdSync(reservationId);
            Integer panierId = existing != null ? existing.getPanierId() : null;

            // 1. Suppression physique locale
            reservationDao.annulerReservation(reservationId);

            // 2. Suppression Cloud
            String filter = "eq." + reservationId;
            apiService.deleteReservationCloud(filter).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful() && panierId != null) {
                        new PanierRepository(application).incrementQuantity(panierId);
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e("ReservationCancel", "Echec suppression cloud: " + t.getMessage());
                }
            });
        });
    }

}