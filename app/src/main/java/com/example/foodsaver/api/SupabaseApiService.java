package com.example.foodsaver.api;

import com.example.foodsaver.data.model.PublicUser;
import com.example.foodsaver.data.network.AuthRequest;
import com.example.foodsaver.data.network.AuthResponse;
import com.example.foodsaver.data.model.Commerce;
import com.example.foodsaver.data.network.CommerceRequest;
import com.example.foodsaver.data.model.Panier;
import com.example.foodsaver.data.network.PanierRequest;
import com.example.foodsaver.data.model.Reservation;
import com.example.foodsaver.data.network.ReservationRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.*;

public interface SupabaseApiService {

    // 1. S'inscrire (Register)
    @POST("auth/v1/signup")
    Call<AuthResponse> signUp(@Body AuthRequest request);

    // 2. Se connecter (Login)
    @POST("auth/v1/token?grant_type=password")
    Call<AuthResponse> signIn(@Body AuthRequest request);

    // ---------------------------------------------------
    // PHASE 2 : HYBRID SYNC POUR LES PANIERS
    // ---------------------------------------------------

    @GET("rest/v1/paniers?select=*")
    Call<List<Panier>> getPaniersFromCloud();

    // NOUVEAU : On force Supabase à nous renvoyer l'objet créé avec son vrai ID
    @Headers("Prefer: return=representation")
    @POST("rest/v1/paniers")
    Call<List<Panier>> insertPanierCloud(@Body PanierRequest panier);

    @PATCH("rest/v1/paniers")
    Call<Void> updatePanierCloud(@Query("id") String eqId, @Body PanierRequest panier);

    @DELETE("rest/v1/paniers")
    Call<Void> deletePanierCloud(@Query("id") String eqId);

    // ---------------------------------------------------
    // COMMERCES
    // ---------------------------------------------------

    // NOUVEAU : C'est la méthode qui manquait pour corriger l'erreur !
    @GET("rest/v1/commerces?select=*")
    Call<List<Commerce>> getCommercesFromCloud();

    // NOUVEAU : On force le retour de l'objet Commerce créé
    @Headers("Prefer: return=representation")
    @POST("rest/v1/commerces")
    Call<List<Commerce>> insertCommerceCloud(@Body CommerceRequest commerce);

    // ---------------------------------------------------
    // RESERVATIONS
    // ---------------------------------------------------

    @GET("rest/v1/reservations?select=*")
    Call<List<Reservation>> getReservationsFromCloud();

    @GET("rest/v1/reservations?select=pickup_time")
    Call<List<Reservation>> getTakenPickupTimesForPanier(
            @Query("panier_id") String eqPanierId,
            @Query("statut") String eqStatut,
            @Query("pickup_time") String gtePickupTime
    );

    @GET("rest/v1/reservations?select=pickup_time")
    Call<List<Reservation>> getTakenPickupTimesForClient(
            @Query("client_id") String eqClientId,
            @Query("statut") String eqStatut,
            @Query("pickup_time") String gtePickupTime
    );

    // Gardé uniquement la bonne version avec ReservationRequest
    @POST("rest/v1/reservations")
    Call<Void> insertReservationCloud(@Body ReservationRequest reservation);

    @PATCH("rest/v1/reservations")
    Call<Void> updateReservationCloud(@Query("id") String eqId, @Body ReservationRequest reservation);

    @DELETE("rest/v1/reservations")
    Call<Void> deleteReservationCloud(@Query("id") String eqId);

    // NOUVEAU : Endpoint pour supprimer un commerce via l'Admin (Soft delete)
    @PATCH("rest/v1/commerces")
    Call<Void> updateCommerceCloud(@Query("id") String eqId, @Body CommerceRequest commerce);

    @DELETE("rest/v1/commerces")
    Call<Void> deleteCommerceCloud(@Query("id") String eqId);


    // NOUVEAU : Récupérer le vrai rôle depuis public.users
    @GET("rest/v1/users")
    Call<List<PublicUser>> getPublicUserRole(@Query("id") String eqId);
}