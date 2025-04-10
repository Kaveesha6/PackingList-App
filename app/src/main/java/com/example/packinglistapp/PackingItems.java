package com.example.packinglistapp;

import java.io.Serializable;

public class PackingItems implements Serializable {

    private String name;
    private int quantity;
    private boolean checked;
    private double price; // Assuming price is required for packing items
    private String categoryName; // New field for category
    private int checkedItemCount; // Number of items checked
    private int totalItemCount; // Total number of items in the category

    // Default constructor for Firebase
    public PackingItems() {
    }

    // Constructor to initialize the item
    public PackingItems(String name, int quantity, boolean checked, double price, String categoryName, int checkedItemCount, int totalItemCount) {
        this.name = name;
        this.quantity = quantity;
        this.checked = checked;
        this.price = price;
        this.categoryName = categoryName;
        this.checkedItemCount = checkedItemCount;
        this.totalItemCount = totalItemCount;
    }

    // Getters and Setters for the fields
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getCheckedItemCount() {
        return checkedItemCount;
    }

    public void setCheckedItemCount(int checkedItemCount) {
        this.checkedItemCount = checkedItemCount;
    }

    public int getTotalItemCount() {
        return totalItemCount;
    }

    public void setTotalItemCount(int totalItemCount) {
        this.totalItemCount = totalItemCount;
    }

    // Method to mark the item as purchased (checked)
    public void markAsPurchased() {
        this.checked = true;
    }

    // Method to mark the item as unpurchased (unchecked)
    public void markAsUnpurchased() {
        this.checked = false;
    }

    // Optional: Method to return a formatted price for display
    public String getFormattedPrice() {
        return "$" + String.format("%.2f", price);
    }

    @Override
    public String toString() {
        return "PackingItems{" +
                "name='" + name + '\'' +
                ", quantity=" + quantity +
                ", checked=" + checked +
                ", price=" + price +
                ", categoryName='" + categoryName + '\'' +
                '}';
    }
}
