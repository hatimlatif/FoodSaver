package com.example.foodsaver.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "reservations")
public class Reservation {
    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    private int id;

    @SerializedName("client_id")
    private String clientId;

    @SerializedName("panier_id")
    private int panierId;

    @SerializedName("statut")
    private String statut;

    @SerializedName("pickup_time")
    private String pickupTime;

    private int isSynced = 1;

    public Reservation(String clientId, int panierId, String statut, String pickupTime) {
        this.clientId = clientId;
        this.panierId = panierId;
        this.statut = statut;
        this.pickupTime = pickupTime;
    }

    public int getId() {
        return id;
    }

    public String getClientId() {
        return clientId;
    }

    public int getPanierId() {
        return panierId;
    }

    public String getStatut() {
        return statut;
    }

    public String getPickupTime() {
        return pickupTime;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setPanierId(int panierId) {
        this.panierId = panierId;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public void setPickupTime(String pickupTime) {
        this.pickupTime = pickupTime;
    }

    public int getIsSynced() {
        return isSynced;
    }

    public void setIsSynced(int isSynced) {
        this.isSynced = isSynced;
    }
}