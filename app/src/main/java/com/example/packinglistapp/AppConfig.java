package com.example.packinglistapp;

public class AppConfig {
    // App settings
    public static final boolean REQUIRE_EMAIL_VERIFICATION = true; // Set to true for production
    public static final boolean ALLOW_OFFLINE_LOGIN = true;

    // Firebase paths
    public static final String USERS_PATH = "users";
    public static final String USERNAMES_PATH = "usernames";
    public static final String USER_TRIPS_PATH = "userTrips";
}
