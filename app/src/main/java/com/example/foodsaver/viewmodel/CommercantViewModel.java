package com.example.foodsaver.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.foodsaver.data.model.Commerce;
import com.example.foodsaver.data.model.Panier;
import com.example.foodsaver.data.model.ReservationDetails;
import com.example.foodsaver.data.model.Statistiques;
import com.example.foodsaver.repository.CommerceRepository;
import com.example.foodsaver.repository.PanierRepository;
import com.example.foodsaver.repository.ReservationRepository;

import java.util.List;

public class CommercantViewModel extends AndroidViewModel {
    private PanierRepository panierRepository;
    private ReservationRepository reservationRepository;
    private CommerceRepository commerceRepository;

    public CommercantViewModel(@NonNull Application application) {
        super(application);
        panierRepository = new PanierRepository(application);
        reservationRepository = new ReservationRepository(application);
        commerceRepository = new CommerceRepository(application);
    }

    // CHANGÉ : commercantId est maintenant un String (UUID)
    public LiveData<List<Commerce>> getMonCommerce(String commercantId) {
        return commerceRepository.getCommercesByCommercant(commercantId);
    }

    public LiveData<Statistiques> getStatistiquesCommercant(String commercantId) {
        return reservationRepository.getStatistiquesCommercant(commercantId);
    }

    // CHANGÉ : commercantId est maintenant un String (UUID)
    public void creerCommerce(String nom, String adresse, String commercantId) {
        commerceRepository.insertCommerce(new Commerce(nom, adresse, commercantId));
    }

    public LiveData<List<Panier>> getMesPaniers(int commerceId) {
        return panierRepository.getPaniersByCommerce(commerceId);
    }

    public void ajouterPanier(String titre, double prix, int quantite, int commerceId) {
        panierRepository.insertPanier(new Panier(titre, prix, quantite, commerceId));
    }

    public void modifierPanier(Panier panier) {
        panierRepository.updatePanier(panier);
    }

    public void supprimerPanier(Panier panier) {
        panierRepository.deletePanier(panier);
    }

    // CHANGÉ : commercantId est maintenant un String (UUID)
    public LiveData<List<ReservationDetails>> getReservationsRecues(String commercantId) {
        return reservationRepository.getReservationsRecues(commercantId);
    }

    public void annulerReservation(int reservationId) {
        reservationRepository.annulerReservation(reservationId);
    }
}