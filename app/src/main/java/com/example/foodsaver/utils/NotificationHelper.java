package com.example.foodsaver.utils;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.foodsaver.R;

public class NotificationHelper {
    private static final String CHANNEL_ID = "foodsaver_channel";
    private static final int NOTIFICATION_ID = 1001;

    // 1. Créer le canal de notification (Obligatoire pour Android 8.0+)
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Nouveaux Paniers";
            String description = "Alertes quand un commerçant publie un nouveau panier anti-gaspi";
            int importance = NotificationManager.IMPORTANCE_HIGH; // HIGH pour faire vibrer le téléphone
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    // 2. Déclencher la notification
    public static void showNewPanierNotification(Context context, String titrePanier) {
        // Vérification de sécurité pour Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return; // Si l'utilisateur a refusé la permission, on annule
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // Remplacer par R.drawable.votre_logo si vous en avez un
                .setContentTitle("Nouveau Panier Anti-Gaspi !")
                .setContentText(titrePanier + " vient d'être ajouté. Dépêchez-vous de le sauver !")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    // NOUVEAU : Notification pour le Commerçant
    public static void showNewReservationNotification(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Nouvelle Réservation ! 🎉")
                .setContentText("Un client vient de réserver et payer un de vos paniers.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        // On utilise un ID différent (1002) pour ne pas écraser la notification client (1001)
        notificationManager.notify(1002, builder.build());
    }
}