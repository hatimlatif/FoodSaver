package com.example.foodsaver.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodsaver.R;
import com.example.foodsaver.data.Panier;

import java.util.ArrayList;
import java.util.List;

public class PanierAdapter extends RecyclerView.Adapter<PanierAdapter.PanierViewHolder> {

    private List<Panier> paniers = new ArrayList<>();
    private OnPanierActionClickListener listener;

    public interface OnPanierActionClickListener {
        void onEditClick(Panier panier);

        void onDeleteClick(Panier panier);
    }

    public void setOnPanierActionClickListener(OnPanierActionClickListener listener) {
        this.listener = listener;
    }

    public void setPaniers(List<Panier> paniers) {
        this.paniers = paniers;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PanierViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_panier, parent, false);
        return new PanierViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PanierViewHolder holder, int position) {
        Panier currentPanier = paniers.get(position);
        holder.tvTitre.setText(currentPanier.getTitre());
        holder.tvPrix.setText("Prix: " + currentPanier.getPrix() + " DH");
        holder.tvQuantite.setText("Qté: " + currentPanier.getQuantite());

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEditClick(currentPanier);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(currentPanier);
        });
    }

    @Override
    public int getItemCount() {
        return paniers.size();
    }

    class PanierViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitre, tvPrix, tvQuantite;
        private Button btnEdit, btnDelete;

        public PanierViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitre = itemView.findViewById(R.id.tvTitrePanier);
            tvPrix = itemView.findViewById(R.id.tvPrixPanier);
            tvQuantite = itemView.findViewById(R.id.tvQuantitePanier);
            btnEdit = itemView.findViewById(R.id.btnEditPanier);
            btnDelete = itemView.findViewById(R.id.btnDeletePanier);
        }
    }
}