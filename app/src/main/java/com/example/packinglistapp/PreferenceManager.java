package com.example.packinglistapp;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    private static final String PREF_NAME = "PackingListPrefs";

    // Get the shared preferences instance
    public static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Save a string value
    public static void saveString(Context context, String key, String value) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putString(key, value);
        editor.apply();
    }

    // Get a string value with a default
    public static String getString(Context context, String key, String defaultValue) {
        return getPreferences(context).getString(key, defaultValue);
    }

    // Save an integer value
    public static void saveInt(Context context, String key, int value) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putInt(key, value);
        editor.apply();
    }

    // Get an integer value with a default
    public static int getInt(Context context, String key, int defaultValue) {
        return getPreferences(context).getInt(key, defaultValue);
    }

    // Increment a counter value
    public static int incrementCounter(Context context, String key, int defaultStartValue) {
        int currentValue = getInt(context, key, defaultStartValue);
        int newValue = currentValue + 1;
        saveInt(context, key, newValue);
        return newValue;
    }
}