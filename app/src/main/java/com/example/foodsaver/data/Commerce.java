package com.example.foodsaver.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "commerces")
public class Commerce {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String nom;
    private String adresse;
    private int commercantId; // Foreign key to User.id

    public Commerce(String nom, String adresse, int commercantId) {
        this.nom = nom;
        this.adresse = adresse;
        this.commercantId = commercantId;
    }

    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getAdresse() { return adresse; }
    public int getCommercantId() { return commercantId; }

    public void setId(int id) { this.id = id; }
    public void setNom(String nom) { this.nom = nom; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    public void setCommercantId(int commercantId) { this.commercantId = commercantId; }
}