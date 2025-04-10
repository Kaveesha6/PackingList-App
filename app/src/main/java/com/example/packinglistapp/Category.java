package com.example.packinglistapp;

import java.util.List;

public class Category {
    private String name;
    private String image;
    private List<PackingItems> items; // This is the list of items in this category

    // Constructors, getters, and setters
    public Category() {
        // Default constructor required for Firebase
    }

    public Category(String name, String image, List<PackingItems> items) {
        this.name = name;
        this.image = image;
        this.items = items;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public List<PackingItems> getItems() {
        return items;
    }

    public void setItems(List<PackingItems> items) {
        this.items = items;
    }
}
