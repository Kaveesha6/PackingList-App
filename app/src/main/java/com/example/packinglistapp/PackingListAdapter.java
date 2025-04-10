package com.example.packinglistapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.cardview.widget.CardView;

import java.util.List;

public class PackingListAdapter extends RecyclerView.Adapter<PackingListAdapter.ViewHolder> {

    private List<PackingItems> packingItems;

    // Constructor to initialize the list of packing items
    public PackingListAdapter(List<PackingItems> packingItems) {
        this.packingItems = packingItems;
    }

    // ViewHolder class to hold references to the views
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryNameTextView, progressTextView;
        ImageView iconImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            categoryNameTextView = itemView.findViewById(R.id.categoryNameTextView);
            progressTextView = itemView.findViewById(R.id.progressTextView);
            iconImageView = itemView.findViewById(R.id.iconImageView); // Assuming ImageView is present in the layout
        }
    }

    // Creates new views (called by the layout manager)
    @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_packing_list, parent, false);
            return new ViewHolder(view);
    }

    // Binds the packing item data to the views
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PackingItems item = packingItems.get(position);
        holder.categoryNameTextView.setText(item.getCategoryName());
        holder.progressTextView.setText(item.getCheckedItemCount() + "/" + item.getTotalItemCount());

        // Optionally, you can set an icon to the ImageView, if you have one
        holder.iconImageView.setImageResource(R.drawable.ic_search); // Use a valid drawable
    }

    // Returns the total number of items in the list
    @Override
    public int getItemCount() {
        return packingItems.size();
    }

    // Update the list of packing items and notify the adapter to refresh
    public void updateData(List<PackingItems> newPackingItems) {
        this.packingItems = newPackingItems;
        notifyDataSetChanged();
    }

}
