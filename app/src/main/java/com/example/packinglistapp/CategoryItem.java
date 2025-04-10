package com.example.packinglistapp;

public class CategoryItem {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_ITEM = 1;

    private String name;
    private String imageResourceName;
    private int type;

    public CategoryItem(String name, String imageResourceName, int type) {
        this.name = name;
        this.imageResourceName = imageResourceName;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getImageResourceName() {
        return imageResourceName;
    }

    public int getType() {
        return type;
    }
}