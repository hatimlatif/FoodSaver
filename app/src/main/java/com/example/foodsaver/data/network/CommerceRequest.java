package com.example.foodsaver.data.network;

import com.example.foodsaver.data.model.Commerce;
import com.google.gson.annotations.SerializedName;

public class CommerceRequest {
    @SerializedName("nom")
    private String nom;
    @SerializedName("adresse")
    private String adresse;
    @SerializedName("commercant_id")
    private String commercantId;

    public CommerceRequest(Commerce commerce) {
        this.nom = commerce.getNom();
        this.adresse = commerce.getAdresse();
        this.commercantId = commerce.getCommercantId();
    }
}