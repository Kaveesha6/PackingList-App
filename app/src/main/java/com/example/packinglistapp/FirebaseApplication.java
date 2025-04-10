package com.example.packinglistapp;

import android.app.Application;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Enable Firebase persistence only once at application start
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        } catch (Exception e) {
            // Persistence might have already been enabled
        }
    }
}