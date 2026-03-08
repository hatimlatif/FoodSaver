package com.example.foodsaver.data.network;

import com.google.gson.annotations.SerializedName;

public class AuthRequest {
    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    @SerializedName("data")
    private UserData metadata; // Used to send 'nom' and 'role' during signup

    public AuthRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public AuthRequest(String email, String password, String nom, String role) {
        this.email = email;
        this.password = password;
        this.metadata = new UserData(nom, role);
    }

    // Inner class for the custom metadata
    public static class UserData {
        private String nom;
        private String role;

        public UserData(String nom, String role) {
            this.nom = nom;
            this.role = role;
        }

        // NOUVEAU : On ajoute les getters pour que AuthResponse puisse y accéder
        public String getNom() {
            return nom;
        }

        public String getRole() {
            return role;
        }
    }
}