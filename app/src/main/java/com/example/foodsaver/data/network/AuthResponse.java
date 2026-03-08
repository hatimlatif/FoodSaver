package com.example.foodsaver.data.network;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {
    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("user")
    private SupabaseUser user;

    public String getAccessToken() {
        return accessToken;
    }

    public SupabaseUser getUser() {
        return user;
    }

    public static class SupabaseUser {
        @SerializedName("id")
        private String id; // This is the UUID from Supabase

        @SerializedName("user_metadata")
        private AuthRequest.UserData metadata;

        public String getId() {
            return id;
        }

        public String getNom() {
            return metadata != null ? metadata.getNom() : null;
        }

        public String getRole() {
            return metadata != null ? metadata.getRole() : null;
        }
    }
}