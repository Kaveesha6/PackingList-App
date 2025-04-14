package com.example.packinglistapp;

public class HotelItem {
    private String hotelItemId;
    private String name;
    private int quantity;
    private boolean checked;

    public HotelItem(String name) {
        this.name = name;
        this.quantity = 1;
        this.checked = false;
    }

    public String getHotelItemId() {
        return hotelItemId;
    }

    public void setHotelItemId(String hotelItemId) {
        this.hotelItemId = hotelItemId;
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

