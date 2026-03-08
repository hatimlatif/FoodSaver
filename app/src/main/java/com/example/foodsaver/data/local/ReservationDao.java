package com.example.foodsaver.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.foodsaver.data.model.ReservationDetails;
import com.example.foodsaver.data.model.Reservation;
import com.example.foodsaver.data.model.Statistiques;

import java.util.List;

@Dao
public interface ReservationDao {

    @Insert
    long insertReservation(Reservation reservation);

    @Update
    void updateReservation(Reservation reservation);

    // For the Client: View their booking history
    @Query("SELECT * FROM reservations WHERE clientId = :clientId")
    LiveData<List<Reservation>> getReservationsByClient(String clientId); // int -> String

    // For the Commerçant: View all reservations made for their specific commerce
    // This uses a subquery to link reservations -> paniers -> commerce
    @Query("SELECT * FROM reservations WHERE panierId IN (SELECT id FROM paniers WHERE commerceId = :commerceId)")
    LiveData<List<Reservation>> getReservationsForCommerce(int commerceId);

    // Joins the tables to get readable details for the client
    @Query("SELECT r.id AS reservationId, p.titre, p.prix, r.statut FROM reservations r INNER JOIN paniers p ON r.panierId = p.id WHERE r.clientId = :clientId")
    LiveData<List<ReservationDetails>> getHistoriqueClient(String clientId); // int -> String

    // Deletes the reservation
    @Query("DELETE FROM reservations WHERE id = :reservationId")
    void annulerReservation(int reservationId);

    // CORRECTION LOGIQUE : On relie 'paniers' à 'commerces' pour vérifier le bon commercantId (UUID)
    @Query("SELECT r.id AS reservationId, p.titre, p.prix, r.statut FROM reservations r " +
            "INNER JOIN paniers p ON r.panierId = p.id " +
            "INNER JOIN commerces c ON p.commerceId = c.id " +
            "WHERE c.commercantId = :commercantId")
    LiveData<List<ReservationDetails>> getReservationsRecues(String commercantId); // int -> String

    @Query("SELECT panierId FROM reservations WHERE clientId = :clientId")
    LiveData<List<Integer>> getReservedPanierIds(String clientId); // int -> String

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllReservations(List<Reservation> reservations);

    @Query("SELECT * FROM reservations WHERE isSynced = 0")
    List<Reservation> getUnsyncedReservationsSync();

    // Fait la somme des prix et compte les réservations uniquement si elles sont "CONFIRMÉE"
    @Query("SELECT COALESCE(SUM(p.prix), 0.0) AS totalRevenus, COUNT(r.id) AS paniersVendus " +
            "FROM reservations r " +
            "INNER JOIN paniers p ON r.panierId = p.id " +
            "INNER JOIN commerces c ON p.commerceId = c.id " +
            "WHERE c.commercantId = :commercantId AND r.statut = 'CONFIRMÉE'")
    LiveData<Statistiques> getStatistiquesCommercant(String commercantId);
}