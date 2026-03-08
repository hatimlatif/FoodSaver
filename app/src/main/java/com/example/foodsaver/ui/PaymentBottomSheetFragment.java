package com.example.foodsaver.ui; // Ajustez le package selon votre structure

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.example.foodsaver.R;

public class PaymentBottomSheetFragment extends BottomSheetDialogFragment {

    private PaymentListener listener;

    // Interface pour communiquer avec l'Activity
    public interface PaymentListener {
        void onPaymentSuccess();
    }

    public PaymentBottomSheetFragment(PaymentListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_payment_bottom_sheet, container, false);

        Button btnPayer = view.findViewById(R.id.btn_confirmer_paiement);

        btnPayer.setOnClickListener(v -> {
            // Désactiver le bouton pour éviter les doubles clics
            btnPayer.setEnabled(false);
            btnPayer.setText("Traitement en cours...");

            // Simulation d'un délai réseau pour faire plus réaliste (1.5 secondes)
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Toast.makeText(getContext(), "Paiement validé avec succès !", Toast.LENGTH_SHORT).show();
                if (listener != null) {
                    listener.onPaymentSuccess(); // Déclenche la réservation dans l'Activity
                }
                dismiss(); // Ferme le Bottom Sheet
            }, 1500);
        });

        return view;
    }
}