package com.example.foodsaver.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "paniers")
public class Panier {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String titre;
    private double prix;
    private int quantite;
    private int commerceId; // Foreign key to Commerce.id

    public Panier(String titre, double prix, int quantite, int commerceId) {
        this.titre = titre;
        this.prix = prix;
        this.quantite = quantite;
        this.commerceId = commerceId;
    }

    public int getId() { return id; }
    public String getTitre() { return titre; }
    public double getPrix() { return prix; }
    public int getQuantite() { return quantite; }
    public int getCommerceId() { return commerceId; }

    public void setId(int id) { this.id = id; }
    public void setTitre(String titre) { this.titre = titre; }
    public void setPrix(double prix) { this.prix = prix; }
    public void setQuantite(int quantite) { this.quantite = quantite; }
    public void setCommerceId(int commerceId) { this.commerceId = commerceId; }
}