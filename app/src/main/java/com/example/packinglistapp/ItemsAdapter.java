package com.example.packinglistapp;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ItemViewHolder> {

    private List<PackingItem> allItems;
    private List<PackingItem> filteredItems;
    private String currencySymbol;
    private OnItemActionListener listener;
    private final DecimalFormat decimalFormat = new DecimalFormat("0.00");

    public interface OnItemActionListener {
        void onStatusChange(PackingItem item);
        void onRemove(PackingItem item);
    }

    public ItemsAdapter(List<PackingItem> items, OnItemActionListener listener, String currencySymbol) {
        this.allItems = items;
        this.filteredItems = new ArrayList<>(items);
        this.listener = listener;
        this.currencySymbol = currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public void setItems(List<PackingItem> items) {
        this.filteredItems = items;  // Set the new list
        notifyDataSetChanged();      // Notify the adapter about the change
    }


    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_itemrow, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        PackingItem item = filteredItems.get(position);

        holder.textItemName.setText(item.getName());
        holder.textCategory.setText(item.getCategory());
        holder.textQuantity.setText(String.valueOf(item.getQuantity()));
        holder.textPrice.setText(currencySymbol + decimalFormat.format(item.getPrice()));
        holder.textTotalPrice.setText(currencySymbol + decimalFormat.format(item.getTotalPrice()));

        if (item.isPurchased()) {
            holder.btnStatus.setText("Purchased");
            holder.btnStatus.setBackgroundColor(Color.parseColor("#E6F9E6"));
            holder.btnStatus.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            holder.btnStatus.setText("To Buy");
            holder.btnStatus.setBackgroundColor(Color.parseColor("#FFF8E1"));
            holder.btnStatus.setTextColor(Color.parseColor("#F57C00"));
        }

        holder.btnStatus.setOnClickListener(v -> {
            if (listener != null) {
                listener.onStatusChange(item);
            }
        });

        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemove(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredItems.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView textItemName, textCategory, textQuantity, textPrice, textTotalPrice;
        Button btnStatus;
        ImageButton btnRemove;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            textItemName = itemView.findViewById(R.id.textItemName);
            textCategory = itemView.findViewById(R.id.textCategory);
            textQuantity = itemView.findViewById(R.id.textQuantity);
            textPrice = itemView.findViewById(R.id.textPrice);
            textTotalPrice = itemView.findViewById(R.id.textTotalPrice);
            btnStatus = itemView.findViewById(R.id.btnStatus);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}