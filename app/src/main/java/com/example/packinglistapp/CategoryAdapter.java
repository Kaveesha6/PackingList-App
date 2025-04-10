package com.example.packinglistapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ITEM = 1;

    private final List<CategoryItem> items;
    private final OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(CategoryItem item);
    }

    public CategoryAdapter(List<CategoryItem> items, OnCategoryClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType() == CategoryItem.TYPE_HEADER ? VIEW_TYPE_HEADER : VIEW_TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_category_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_category, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        CategoryItem item = items.get(position);

        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).headerTitle.setText(item.getName());
        } else if (holder instanceof ItemViewHolder) {
            ItemViewHolder itemHolder = (ItemViewHolder) holder;
            itemHolder.categoryName.setText(item.getName());

            // Set image based on category
            int resourceId = getImageResourceForCategory(item.getName(), item.getImageResourceName());
            itemHolder.categoryImage.setImageResource(resourceId);

            itemHolder.itemView.setOnClickListener(v -> listener.onCategoryClick(item));
        }
    }

    private int getImageResourceForCategory(String categoryName, String imageName) {
        // You would implement logic to return the appropriate drawable resource ID
        // based on the category name or image resource name

        // For example:
        switch (categoryName.toLowerCase()) {
            case "hotel":
                return R.drawable.hotel; // You'll need to create these drawable resources
            case "rental":
                return R.drawable.rental;
            case "family / friends":
                return R.drawable.family_friends;
            case "second home":
                return R.drawable.second_home;
            case "camping":
                return R.drawable.camping;
            case "cruise":
                return R.drawable.cruise;
            case "airplane":
                return R.drawable.airplane;
            case "car":
                return R.drawable.car_image;
            case "train":
                return R.drawable.train_image;
            case "motorcycle":
                return R.drawable.motorcycle_image;
            case "boat":
                return R.drawable.boat_image;
            case "bus":
                return R.drawable.bus_image;
            case "essentials":
                return R.drawable.essentials_image;
            case "clothes":
                return R.drawable.clothes_image;
            case "international":
                return R.drawable.international_image;
            case "work":
                return R.drawable.work_image;
            case "personal care":
                return R.drawable.personal_care_image;
            case "beach":
                return R.drawable.beach_image;
            case "swimming":
                return R.drawable.swimming_image;
            case "photography":
                return R.drawable.photography_image;
            case "snow sports":
                return R.drawable.snow_sports_image;
            case "fitness":
                return R.drawable.fitness_image;
            case "hiking":
                return R.drawable.hiking_image;
            case "business meals":
                return R.drawable.business_meals_image;
            case "todo list":
                return R.drawable.todo_list_image;
            case "baby":
                return R.drawable.baby_image;
            case "budget calculator":
                return R.drawable.calculator_image;
            default:
                return R.drawable.placeholder_image; // Fallback image
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView headerTitle;

        HeaderViewHolder(View itemView) {
            super(itemView);
            headerTitle = itemView.findViewById(R.id.headerTitle);
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView categoryImage;
        TextView categoryName;

        ItemViewHolder(View itemView) {
            super(itemView);
            categoryImage = itemView.findViewById(R.id.categoryImage);
            categoryName = itemView.findViewById(R.id.categoryName);
        }
    }
}