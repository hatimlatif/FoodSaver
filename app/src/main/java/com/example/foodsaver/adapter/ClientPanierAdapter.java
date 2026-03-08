package com.example.foodsaver.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodsaver.R;
import com.example.foodsaver.data.model.Panier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClientPanierAdapter extends RecyclerView.Adapter<ClientPanierAdapter.ClientViewHolder> {

    private List<Panier> paniers = new ArrayList<>();
    private OnPanierClickListener listener;

    // NOUVEAU : On garde en mémoire les ID des paniers déjà cliqués
    private Set<Integer> paniersReserves = new HashSet<>();

    public interface OnPanierClickListener {
        void onReserveClick(Panier panier);
    }

    public void setOnPanierClickListener(OnPanierClickListener listener) {
        this.listener = listener;
    }

    public void setPaniers(List<Panier> paniers) {
        this.paniers = paniers;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ClientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_panier_client, parent, false);
        return new ClientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClientViewHolder holder, int position) {
        Panier currentPanier = paniers.get(position);
        holder.tvTitre.setText(currentPanier.getTitre());
        holder.tvPrix.setText("Prix: " + currentPanier.getPrix() + " DH");

        // Vérifier si ce panier est dans notre liste de paniers réservés
        if (paniersReserves.contains(currentPanier.getId())) {
            holder.btnReserver.setEnabled(false); // Désactive le clic
            holder.btnReserver.setText("Réservé"); // Change le texte
        } else {
            holder.btnReserver.setEnabled(true);
            holder.btnReserver.setText("Réserver");
        }

        holder.btnReserver.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReserveClick(currentPanier);
            }
        });
    }

    @Override
    public int getItemCount() {
        return paniers.size();
    }

    class ClientViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitre, tvPrix;
        private Button btnReserver;

        public ClientViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitre = itemView.findViewById(R.id.tvClientTitrePanier);
            tvPrix = itemView.findViewById(R.id.tvClientPrixPanier);
            btnReserver = itemView.findViewById(R.id.btnReserver);
        }
    }

    // NOUVELLE MÉTHODE : Met à jour la liste depuis la base de données
    public void setReservedPanierIds(List<Integer> reservedIds) {
        this.paniersReserves.clear();
        if (reservedIds != null) {
            this.paniersReserves.addAll(reservedIds);
        }
        notifyDataSetChanged(); // Force la liste à se redessiner avec les bons boutons grisés
    }
}