package com.example.foodsaver.data.network;

import com.example.foodsaver.data.model.Panier;
import com.google.gson.annotations.SerializedName;

public class PanierRequest {
    @SerializedName("titre")
    private String titre;

    @SerializedName("prix")
    private double prix;

    @SerializedName("quantite")
    private int quantite;

    @SerializedName("commerce_id")
    private int commerceId;

    @SerializedName("statut")
    private String statut;

    // Le constructeur prend le Panier local et extrait uniquement ce que Supabase a besoin de savoir
    public PanierRequest(Panier panier) {
        this.titre = panier.getTitre();
        this.prix = panier.getPrix();
        this.quantite = panier.getQuantite();
        this.commerceId = panier.getCommerceId();
    }
}
