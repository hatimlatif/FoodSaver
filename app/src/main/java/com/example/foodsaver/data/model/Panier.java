package com.example.foodsaver.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "paniers")
public class Panier {

    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    private int id;

    @SerializedName("titre")
    private String titre;

    @SerializedName("prix")
    private double prix;

    @SerializedName("quantite")
    private int quantite;

    // Fait le lien entre le nom Java et la colonne Supabase
    @SerializedName("commerce_id")
    private int commerceId;

    // NOUVEAU: Le drapeau "Offline-First".
    private int isSynced = 1;

    public Panier(String titre, double prix, int quantite, int commerceId) {
        this.titre = titre;
        this.prix = prix;
        this.quantite = quantite;
        this.commerceId = commerceId;
    }

    public int getId() {
        return id;
    }

    public String getTitre() {
        return titre;
    }

    public double getPrix() {
        return prix;
    }

    public int getQuantite() {
        return quantite;
    }

    public int getCommerceId() {
        return commerceId;
    }

    public int getIsSynced() {
        return isSynced;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public void setCommerceId(int commerceId) {
        this.commerceId = commerceId;
    }

    public void setIsSynced(int isSynced) {
        this.isSynced = isSynced;
    }
}