package com.example.foodsaver.data.model;

import com.google.gson.annotations.SerializedName;

public class PublicUser {
    @SerializedName("id")
    private String id;

    @SerializedName("role")
    private String role;

    public String getId() {
        return id;
    }

    public String getRole() {
        return role;
    }
}