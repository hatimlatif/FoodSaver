package com.example.foodsaver.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "reservations")
public class Reservation {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private int clientId; // Foreign key to User.id
    private int panierId; // Foreign key to Panier.id
    private String statut; // e.g., "EN_ATTENTE", "CONFIRMEE", "ANNULEE"

    public Reservation(int clientId, int panierId, String statut) {
        this.clientId = clientId;
        this.panierId = panierId;
        this.statut = statut;
    }

    public int getId() { return id; }
    public int getClientId() { return clientId; }
    public int getPanierId() { return panierId; }
    public String getStatut() { return statut; }

    public void setId(int id) { this.id = id; }
    public void setClientId(int clientId) { this.clientId = clientId; }
    public void setPanierId(int panierId) { this.panierId = panierId; }
    public void setStatut(String statut) { this.statut = statut; }
}