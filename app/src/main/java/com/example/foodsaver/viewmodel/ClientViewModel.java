package com.example.foodsaver.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.foodsaver.data.Commerce;
import com.example.foodsaver.data.Panier;
import com.example.foodsaver.data.Reservation;
import com.example.foodsaver.data.ReservationDetails;
import com.example.foodsaver.repository.CommerceRepository;
import com.example.foodsaver.repository.PanierRepository;
import com.example.foodsaver.repository.ReservationRepository;

import java.util.List;

public class ClientViewModel extends AndroidViewModel {

    private PanierRepository panierRepository;
    private ReservationRepository reservationRepository;
    private CommerceRepository commerceRepository; // Ajoutez en haut

    public ClientViewModel(@NonNull Application application) {
        super(application);
        panierRepository = new PanierRepository(application);
        reservationRepository = new ReservationRepository(application);
        commerceRepository = new CommerceRepository(application);
    }

    // Fetches ALL available baskets from the whole app
    public LiveData<List<Panier>> getAllPaniers() {
        return panierRepository.getAllPaniers();
    }

    // Creates a new reservation
    public void reserverPanier(int clientId, int panierId) {
        Reservation reservation = new Reservation(clientId, panierId, "EN_ATTENTE");
        reservationRepository.insertReservation(reservation);
    }

    public LiveData<List<ReservationDetails>> getMesReservations(int clientId) {
        return reservationRepository.getHistoriqueClient(clientId);
    }

    public void annulerReservation(int reservationId) {
        reservationRepository.annulerReservation(reservationId);
    }

    // Nouvelle méthode :
    public LiveData<List<Commerce>> getAllCommerces() {
        return commerceRepository.getAllCommerces();
    }

    // Modifiez getPaniersByCommerce :
    public LiveData<List<Panier>> getPaniersByCommerce(int commerceId) {
        return panierRepository.getPaniersByCommerce(commerceId);
    }
}