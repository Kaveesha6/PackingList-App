package com.example.packinglistapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NewItemAdapter extends RecyclerView.Adapter<NewItemAdapter.ItemViewHolder> {

    private List<NewPackingItem> itemList;

    public NewItemAdapter(List<NewPackingItem> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_new_packing, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        NewPackingItem item = itemList.get(position);
        holder.checkBox.setText(item.getName());
        holder.checkBox.setChecked(item.isChecked());
        holder.quantityText.setText(String.valueOf(item.getQuantity()));

        holder.deleteButton.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                itemList.remove(adapterPosition);
                notifyItemRemoved(adapterPosition);
            }
        });

        holder.decreaseButton.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                NewPackingItem currentItem = itemList.get(adapterPosition);
                if (currentItem.getQuantity() > 1) {
                    currentItem.setQuantity(currentItem.getQuantity() - 1);
                    notifyItemChanged(adapterPosition);
                }
            }
        });

        holder.increaseButton.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                NewPackingItem currentItem = itemList.get(adapterPosition);
                currentItem.setQuantity(currentItem.getQuantity() + 1);
                notifyItemChanged(adapterPosition);
            }
        });

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                itemList.get(adapterPosition).setChecked(isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView quantityText;
        ImageButton decreaseButton, increaseButton, deleteButton;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBoxItem);
            quantityText = itemView.findViewById(R.id.textQuantity);
            decreaseButton = itemView.findViewById(R.id.btnDecrease);
            increaseButton = itemView.findViewById(R.id.btnIncrease);
            deleteButton = itemView.findViewById(R.id.btnDelete);
        }
    }
}