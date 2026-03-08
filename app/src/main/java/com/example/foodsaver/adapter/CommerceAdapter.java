package com.example.foodsaver.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodsaver.R;
import com.example.foodsaver.data.model.Commerce;

import java.util.ArrayList;
import java.util.List;

public class CommerceAdapter extends RecyclerView.Adapter<CommerceAdapter.ViewHolder> {
    private List<Commerce> commerces = new ArrayList<>();
    private OnCommerceClickListener listener;

    public interface OnCommerceClickListener {
        void onClick(Commerce commerce);
    }

    public void setOnCommerceClickListener(OnCommerceClickListener listener) {
        this.listener = listener;
    }

    public void setCommerces(List<Commerce> commerces) {
        this.commerces = commerces;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_commerce, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Commerce c = commerces.get(position);
        holder.tvNom.setText(c.getNom());
        holder.tvAdresse.setText(c.getAdresse());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(c);
        });
    }

    @Override
    public int getItemCount() {
        return commerces.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNom, tvAdresse;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNom = itemView.findViewById(R.id.tvNomCommerce);
            tvAdresse = itemView.findViewById(R.id.tvAdresseCommerce);
        }
    }
}