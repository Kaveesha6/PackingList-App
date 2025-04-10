package com.example.packinglistapp;

import java.io.Serializable;

public class Trip implements Serializable {
    private String id;            // Changed from long to String for Firebase
    private String name;
    private String destination;
    private String startDate;
    private String endDate;
    private long duration;
    private String tripType;      // "Business" or "Vacation"
    private String imageUrl;      // For destination image
    private String weather;       // Weather at destination
    private int itemCount;        // Number of items in packing list

    public Trip() {
        // Empty constructor needed for Firebase
    }

    public Trip(String name, String destination, String startDate, String endDate, String tripType, long duration) {
        this.name = name;
        this.destination = destination;
        this.startDate = startDate;
        this.endDate = endDate;
        this.tripType = tripType;
        this.itemCount = 0;
        this.duration = duration;
    }

    // Original getters and setters
    public String getId() {       // Changed from long to String
        return id;
    }

    public void setId(String id) { // Changed from long to String
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getTripType() {
        return tripType;
    }

    public void setTripType(String tripType) {
        this.tripType = tripType;
    }

    // New getters and setters for added fields
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }
}