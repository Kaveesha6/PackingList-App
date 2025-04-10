package com.example.packinglistapp;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MotorcycleItemAdapter extends RecyclerView.Adapter<MotorcycleItemAdapter.ViewHolder> {

    private List<MotorcycleItem> itemList;
    private Context context;

    public MotorcycleItemAdapter(Context context, List<MotorcycleItem> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_checklist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MotorcycleItem item = itemList.get(position);

        holder.checkboxItem.setText(item.getName());
        holder.checkboxItem.setChecked(item.isChecked());
        holder.quantityValue.setText(String.valueOf(item.getQuantity()));

        holder.checkboxItem.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setChecked(isChecked);
        });

        holder.buttonPlus.setOnClickListener(v -> {
            int quantity = item.getQuantity() + 1;
            item.setQuantity(quantity);
            holder.quantityValue.setText(String.valueOf(quantity));
        });

        holder.buttonMinus.setOnClickListener(v -> {
            int quantity = item.getQuantity();
            if (quantity > 1) {
                quantity--;
                item.setQuantity(quantity);
                holder.quantityValue.setText(String.valueOf(quantity));
            }
        });

        holder.buttonDelete.setOnClickListener(v -> {
            itemList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, itemList.size());
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public void addItem(MotorcycleItem item) {
        itemList.add(item);
        notifyItemInserted(itemList.size() - 1);
    }

    public List<MotorcycleItem> getItemList() {
        return itemList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkboxItem;
        TextView quantityValue;
        ImageButton buttonPlus, buttonMinus, buttonDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkboxItem = itemView.findViewById(R.id.checkboxItem);
            quantityValue = itemView.findViewById(R.id.quantityValue);
            buttonPlus = itemView.findViewById(R.id.buttonPlus);
            buttonMinus = itemView.findViewById(R.id.buttonMinus);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}