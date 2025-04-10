package com.example.packinglistapp;

public class EssentialsItem {
    private String name;
    private int quantity;
    private boolean isChecked;

    public EssentialsItem(String name) {
        this.name = name;
        this.quantity = 1;
        this.isChecked = false;
    }

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
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
