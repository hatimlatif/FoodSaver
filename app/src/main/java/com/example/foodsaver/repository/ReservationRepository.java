package com.example.foodsaver.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.foodsaver.data.AppDatabase;
import com.example.foodsaver.data.Reservation;
import com.example.foodsaver.data.ReservationDao;
import com.example.foodsaver.data.ReservationDetails;

import java.util.List;

public class ReservationRepository {
    private ReservationDao reservationDao;

    public ReservationRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        reservationDao = db.reservationDao();
    }

    public LiveData<List<Reservation>> getReservationsByClient(int clientId) {
        return reservationDao.getReservationsByClient(clientId);
    }

    public void insertReservation(Reservation reservation) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            reservationDao.insertReservation(reservation);
        });
    }

    public LiveData<List<ReservationDetails>> getHistoriqueClient(int clientId) {
        return reservationDao.getHistoriqueClient(clientId);
    }

    public void annulerReservation(int reservationId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            reservationDao.annulerReservation(reservationId);
        });
    }

    public LiveData<List<ReservationDetails>> getReservationsRecues(int commercantId) {
        return reservationDao.getReservationsRecues(commercantId);
    }

    public LiveData<List<Integer>> getReservedPanierIds(int clientId) {
        return reservationDao.getReservedPanierIds(clientId);
    }
}