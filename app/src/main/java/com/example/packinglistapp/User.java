package com.example.packinglistapp;

import com.google.firebase.database.Exclude;
import java.util.HashMap;
import java.util.Map;

public class User {
    private String name;
    private String username;
    private String email;
    private String phone;
    private String profileImageUrl;
    private long createdAt;
    private boolean emailVerified;

    // Required empty constructor for Firebase
    public User() {
        this.profileImageUrl = "";
        this.createdAt = System.currentTimeMillis();
        this.emailVerified = false;
    }

    public User(String name, String username, String email, String phone) {
        this();
        this.name = name;
        this.username = username;
        this.email = email;
        this.phone = phone;
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }

    // Convert User object to Map for Firebase
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("username", username);
        result.put("email", email);
        result.put("phone", phone);
        result.put("profileImageUrl", profileImageUrl);
        result.put("createdAt", createdAt);
        result.put("emailVerified", emailVerified);
        return result;
    }
}