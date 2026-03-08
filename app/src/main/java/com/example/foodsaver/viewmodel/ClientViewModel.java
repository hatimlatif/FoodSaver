package com.example.foodsaver.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.foodsaver.data.model.Commerce;
import com.example.foodsaver.data.model.Panier;
import com.example.foodsaver.data.model.Reservation;
import com.example.foodsaver.data.model.ReservationDetails;
import com.example.foodsaver.repository.CommerceRepository;
import com.example.foodsaver.repository.PanierRepository;
import com.example.foodsaver.repository.ReservationRepository;

import java.util.List;

public class ClientViewModel extends AndroidViewModel {

    private PanierRepository panierRepository;
    private ReservationRepository reservationRepository;
    private CommerceRepository commerceRepository;

    public ClientViewModel(@NonNull Application application) {
        super(application);
        panierRepository = new PanierRepository(application);
        reservationRepository = new ReservationRepository(application);
        commerceRepository = new CommerceRepository(application);
    }

    public LiveData<List<Panier>> getAllPaniers() {
        return panierRepository.getAllPaniers();
    }

    // CHANGÉ : clientId est maintenant un String (UUID)
    public void reserverPanier(String clientId, int panierId) {
        Reservation reservation = new Reservation(clientId, panierId, "CONFIRMÉE");
        reservationRepository.insertReservation(reservation);
    }

    // CHANGÉ : clientId est maintenant un String (UUID)
    public LiveData<List<ReservationDetails>> getMesReservations(String clientId) {
        return reservationRepository.getHistoriqueClient(clientId);
    }

    public void annulerReservation(int reservationId) {
        reservationRepository.annulerReservation(reservationId);
    }

    public LiveData<List<Commerce>> getAllCommerces() {
        return commerceRepository.getAllCommerces();
    }

    public LiveData<List<Panier>> getPaniersByCommerce(int commerceId) {
        return panierRepository.getPaniersByCommerce(commerceId);
    }

    // CHANGÉ : clientId est maintenant un String (UUID)
    public LiveData<List<Integer>> getReservedPanierIds(String clientId) {
        return reservationRepository.getReservedPanierIds(clientId);
    }
}