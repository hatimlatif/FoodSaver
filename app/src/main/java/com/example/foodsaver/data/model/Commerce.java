package com.example.foodsaver.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "commerces")
public class Commerce {
    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    private int id;

    @SerializedName("nom")
    private String nom;

    @SerializedName("adresse")
    private String adresse;

    @SerializedName("commercant_id")
    private String commercantId;

    private int isSynced = 1;

    public Commerce(String nom, String adresse, String commercantId) {
        this.nom = nom;
        this.adresse = adresse;
        this.commercantId = commercantId;
    }

    public int getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getAdresse() {
        return adresse;
    }

    public String getCommercantId() {
        return commercantId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public void setCommercantId(String commercantId) {
        this.commercantId = commercantId;
    }

    public int getIsSynced() {
        return isSynced;
    }

    public void setIsSynced(int isSynced) {
        this.isSynced = isSynced;
    }
}

