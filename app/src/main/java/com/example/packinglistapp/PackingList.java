package com.example.packinglistapp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PackingList implements Serializable {
    private String id;
    private String tripId;
    private String categoryName;  // e.g., "Hotel", "Airplane"
    private List<PackingItems> items;

    public PackingList() {
        // Empty constructor needed for Firebase
        items = new ArrayList<>();
    }

    public PackingList(String tripId, String categoryName) {
        this.tripId = tripId;
        this.categoryName = categoryName;
        this.items = new ArrayList<>();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public List<PackingItems> getItems() {
        return items;
    }

    public void setItems(List<PackingItems> items) {
        this.items = items;
    }

    // Helper methods
    public void addItem(PackingItems item) {
        if (items == null) {
            items = new ArrayList<>();
        }
        items.add(item);
    }

    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public int getCheckedItemCount() {
        int count = 0;
        if (items != null) {
            for (PackingItems item : items) {
                if (item.isChecked()) {
                    count++;
                }
            }
        }
        return count;
    }
}