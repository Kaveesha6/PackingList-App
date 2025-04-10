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

public class FamilyFrendsItemAdapter extends RecyclerView.Adapter<FamilyFrendsItemAdapter.ViewHolder> {

    private List<FamilyFrendsItem> itemList;
    private Context context;

    // Fixed constructor parameter type - was incorrect generic type
    public FamilyFrendsItemAdapter(Context context, List<FamilyFrendsItem> itemList) {
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
        FamilyFrendsItem item = itemList.get(position);

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

        // Fixed position issue by using holder.getAdapterPosition()
        holder.buttonDelete.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                itemList.remove(adapterPosition);
                notifyItemRemoved(adapterPosition);
                notifyItemRangeChanged(adapterPosition, itemList.size());
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public void addItem(FamilyFrendsItem item) {
        itemList.add(item);
        notifyItemInserted(itemList.size() - 1);
    }

    public List<FamilyFrendsItem> getItemList() {
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