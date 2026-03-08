package com.example.foodsaver.data.network;

import com.example.foodsaver.data.model.Reservation;
import com.google.gson.annotations.SerializedName;

public class ReservationRequest {
    @SerializedName("client_id")
    private String clientId;
    @SerializedName("panier_id")
    private int panierId;
    @SerializedName("statut")
    private String statut;

    public ReservationRequest(Reservation reservation) {
        this.clientId = reservation.getClientId();
        this.panierId = reservation.getPanierId();
        this.statut = reservation.getStatut();
    }
}