package com.example.packinglistapp;

public class CategoryBreakdown {
    private String categoryName;
    private double totalAmount;
    private double percentage;

    public CategoryBreakdown(String categoryName, double totalAmount, double percentage) {
        this.categoryName = categoryName;
        this.totalAmount = totalAmount;
        this.percentage = percentage;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }
}