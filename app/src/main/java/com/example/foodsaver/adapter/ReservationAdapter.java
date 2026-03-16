package com.example.foodsaver.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodsaver.R;
import com.example.foodsaver.data.model.ReservationDetails;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ViewHolder> {

    private List<ReservationDetails> reservations = new ArrayList<>();
    private OnCancelClickListener listener;

    public interface OnCancelClickListener {
        void onCancelClick(int reservationId);
    }

    public void setOnCancelClickListener(OnCancelClickListener listener) {
        this.listener = listener;
    }

    public void setReservations(List<ReservationDetails> reservations) {
        this.reservations = reservations;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reservation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReservationDetails res = reservations.get(position);
        holder.tvTitre.setText(res.titre);
        holder.tvPrix.setText("Prix: " + res.prix + " DH - " + res.statut + "\nRetrait: " + formatPickupTime(res.pickupTime));

        holder.btnAnnuler.setOnClickListener(v -> {
            if (listener != null) listener.onCancelClick(res.reservationId);
        });
    }

    @Override
    public int getItemCount() {
        return reservations.size();
    }

    private String formatPickupTime(String pickupTimeUtc) {
        if (pickupTimeUtc == null || pickupTimeUtc.trim().isEmpty()) {
            return "Non defini";
        }

        try {
            return Instant.parse(pickupTimeUtc)
                    .atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        } catch (Exception e) {
            return pickupTimeUtc;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitre, tvPrix;
        Button btnAnnuler;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitre = itemView.findViewById(R.id.tvResTitre);
            tvPrix = itemView.findViewById(R.id.tvResPrix);
            btnAnnuler = itemView.findViewById(R.id.btnAnnuler);
        }
    }
}