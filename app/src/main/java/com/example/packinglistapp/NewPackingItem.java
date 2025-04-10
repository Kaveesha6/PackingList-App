package com.example.packinglistapp;

public class NewPackingItem {
    private String name;
    private int quantity;
    private boolean checked;

    public NewPackingItem(String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
        this.checked = false;
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
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}