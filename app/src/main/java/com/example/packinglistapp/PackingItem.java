package com.example.packinglistapp;

public class PackingItem {
    private String name;
    private String category;
    private int quantity;
    private double price;
    private boolean purchased;

    public PackingItem(String name, String category, int quantity, double price, boolean purchased) {
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.price = price;
        this.purchased = purchased;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isPurchased() {
        return purchased;
    }

    public void setPurchased(boolean purchased) {
        this.purchased = purchased;
    }

    public double getTotalPrice() {
        return price * quantity;
    }
}