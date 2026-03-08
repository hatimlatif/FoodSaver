package com.example.foodsaver.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodsaver.R;
import com.example.foodsaver.data.model.Commerce;
import java.util.ArrayList;
import java.util.List;

public class AdminCommerceAdapter extends RecyclerView.Adapter<AdminCommerceAdapter.AdminViewHolder> {

    private List<Commerce> commerces = new ArrayList<>();
    private OnDeleteClickListener listener;

    public interface OnDeleteClickListener {
        void onDeleteClick(Commerce commerce);
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.listener = listener;
    }

    public void setCommerces(List<Commerce> commerces) {
        this.commerces = commerces;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AdminViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_commerce, parent, false);
        return new AdminViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminViewHolder holder, int position) {
        Commerce c = commerces.get(position);
        holder.tvNom.setText(c.getNom());
        holder.tvAdresse.setText(c.getAdresse());

        holder.btnSupprimer.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(c);
        });
    }

    @Override
    public int getItemCount() {
        return commerces.size();
    }

    class AdminViewHolder extends RecyclerView.ViewHolder {
        TextView tvNom, tvAdresse;
        Button btnSupprimer;

        public AdminViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNom = itemView.findViewById(R.id.tvAdminNomCommerce);
            tvAdresse = itemView.findViewById(R.id.tvAdminAdresseCommerce);
            btnSupprimer = itemView.findViewById(R.id.btnAdminSupprimer);
        }
    }

}