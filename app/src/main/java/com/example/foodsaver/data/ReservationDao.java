package com.example.foodsaver.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ReservationDao {

    @Insert
    long insertReservation(Reservation reservation);

    @Update
    void updateReservation(Reservation reservation);

    // For the Client: View their booking history
    @Query("SELECT * FROM reservations WHERE clientId = :clientId")
    LiveData<List<Reservation>> getReservationsByClient(int clientId);

    // For the Commerçant: View all reservations made for their specific commerce
    // This uses a subquery to link reservations -> paniers -> commerce
    @Query("SELECT * FROM reservations WHERE panierId IN (SELECT id FROM paniers WHERE commerceId = :commerceId)")
    LiveData<List<Reservation>> getReservationsForCommerce(int commerceId);

    // Joins the tables to get readable details for the client
    @Query("SELECT r.id AS reservationId, p.titre, p.prix, r.statut FROM reservations r INNER JOIN paniers p ON r.panierId = p.id WHERE r.clientId = :clientId")
    LiveData<List<ReservationDetails>> getHistoriqueClient(int clientId);

    // Deletes the reservation
    @Query("DELETE FROM reservations WHERE id = :reservationId")
    void annulerReservation(int reservationId);

    // Fetch all reservations made for the paniers belonging to this commercant
    @Query("SELECT r.id AS reservationId, p.titre, p.prix, r.statut FROM reservations r INNER JOIN paniers p ON r.panierId = p.id WHERE p.commerceId = :commercantId")
    LiveData<List<ReservationDetails>> getReservationsRecues(int commercantId);

    @Query("SELECT panierId FROM reservations WHERE clientId = :clientId")
    LiveData<List<Integer>> getReservedPanierIds(int clientId);
}