package com.example.packinglistapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;



public class Calendar extends AppCompatActivity {
    private AutoCompleteTextView searchPlace;
    private TextInputEditText tripName;
    private Button btnSelectLists;
    private LinearLayout btnBusiness, btnVacation;
    private ImageView placeImage, businessIcon, vacationIcon;
    private ImageView weatherIcon;
    private TextView weatherDescription, weatherTemperature;

    private TextView selectedDates, weatherInfo;
    private String selectedStartDate = "", selectedEndDate = "";
    private boolean isStartDateSelected = false;
    private String tripType = ""; // Business or Vacation
    private final String API_KEY = "e6314e431bdee40ead721f2ffbc5e2ae"; // Replace with your OpenWeatherMap API Key
    private final String CITY_NAME = "Colombo";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        // Initialize UI elements
        searchPlace = findViewById(R.id.searchPlace);
        tripName = findViewById(R.id.tripName);
        btnSelectLists = findViewById(R.id.btnSelectLists);
        btnBusiness = findViewById(R.id.btnBusiness);
        btnVacation = findViewById(R.id.btnVacation);
        placeImage = findViewById(R.id.placeImage);
        selectedDates = findViewById(R.id.selectedDates);
        weatherInfo = findViewById(R.id.weatherDescription);
        businessIcon = findViewById(R.id.businessIcon);
        vacationIcon = findViewById(R.id.vacationIcon);
        CalendarView calendarView = findViewById(R.id.calendarView);
        ImageView btnCloseCalendar = findViewById(R.id.closeButton);

        // In onCreate, ensure you're properly initializing all weather-related views
        weatherIcon = findViewById(R.id.weatherIcon);
        weatherDescription = findViewById(R.id.weatherDescription);
        weatherTemperature = findViewById(R.id.weatherTemperature);

// Initialize the weather container
        LinearLayout weatherContainer = findViewById(R.id.weatherContainer);


        // Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        }

        PlacesClient placesClient = Places.createClient(this);

        // Set up Place search
        searchPlace.setFocusable(false);
        searchPlace.setOnClickListener(v -> openAutocomplete());

        // Set up calendar date selection
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String date = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year);
            if (!isStartDateSelected) {
                selectedStartDate = date;
                isStartDateSelected = true;
                Toast.makeText(Calendar.this, "Select end date", Toast.LENGTH_SHORT).show();
            } else {
                // Check if end date is after start date
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    Date start = sdf.parse(selectedStartDate);
                    Date end = sdf.parse(date);

                    if (end.before(start)) {
                        Toast.makeText(Calendar.this, "End date cannot be before start date", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                selectedEndDate = date;
                isStartDateSelected = false;
                updateSelectedDates();
            }
        });



        // Set up close button
        btnCloseCalendar.setOnClickListener(v -> {
            // Go back to home screen
            Intent intent = new Intent(Calendar.this, Home.class);
            startActivity(intent);
            finish();
        });

        // Set up trip type selection
        btnBusiness.setOnClickListener(v -> selectCategory("Business"));
        btnVacation.setOnClickListener(v -> selectCategory("Vacation"));

        // Set up select lists button
        btnSelectLists.setOnClickListener(v -> saveData());

        // Fetch weather data
        getWeatherData(CITY_NAME);
    }

    private void selectCategory(String category) {
        tripType = category;
        if ("Business".equals(category)) {
            businessIcon.setColorFilter(getResources().getColor(R.color.colorPrimary));
            vacationIcon.clearColorFilter();
            Toast.makeText(this, "Business trip selected", Toast.LENGTH_SHORT).show();
        } else {
            vacationIcon.setColorFilter(getResources().getColor(R.color.colorPrimary));
            businessIcon.clearColorFilter();
            Toast.makeText(this, "Vacation trip selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void openAutocomplete() {
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.PHOTO_METADATAS);
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .build(this);
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                searchPlace.setText(place.getName());
                loadImageForPlace(place);

                if (place.getLatLng() != null) {
                    fetchWeather(place.getLatLng().latitude, place.getLatLng().longitude);
                }

                // If the trip name is empty, suggest place name as trip name
                if (tripName.getText() == null || tripName.getText().toString().isEmpty()) {
                    tripName.setText(place.getName());
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Toast.makeText(this, "Error: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    private void loadImageForPlace(Place place) {
        if (place.getPhotoMetadatas() != null && !place.getPhotoMetadatas().isEmpty()) {
            PhotoMetadata photoMetadata = place.getPhotoMetadatas().get(0);

            // Get the image using Glide and the Places API
            PlacesClient placesClient = Places.createClient(this);
            FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                    .setMaxWidth(600)
                    .setMaxHeight(400)
                    .build();

            placesClient.fetchPhoto(photoRequest).addOnSuccessListener(fetchPhotoResponse -> {
                Bitmap bitmap = fetchPhotoResponse.getBitmap();
                placeImage.setImageBitmap(bitmap);
                placeImage.setVisibility(View.VISIBLE);
            }).addOnFailureListener(exception -> {
                placeImage.setImageResource(R.drawable.placeholder_image); // Fallback image
                placeImage.setVisibility(View.VISIBLE);
            });
        } else {
            Glide.with(this)
                    .load("https://source.unsplash.com/600x400/?" + place.getName())
                    .placeholder(R.drawable.placeholder_image) // Make sure to add a placeholder image
                    .error(R.drawable.error_image) // Make sure to add an error image
                    .into(placeImage);
            placeImage.setVisibility(View.VISIBLE);
        }
    }

    // Update your getWeatherData method to include the same error handling
    private void fetchWeather(double lat, double lng) {
        String urlString = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lng + "&units=metric&appid=" + API_KEY;

        // Log the URL for debugging (remove in production)
        Log.d("WeatherAPI", "Fetching from: " + urlString);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(urlString)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.e("WeatherAPI", "Fetch failed: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(Calendar.this, "Weather fetch failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    final String responseData = response.body().string();
                    Log.d("WeatherAPI", "Response: " + responseData);
                    runOnUiThread(() -> parseWeatherData(responseData));
                } else {
                    Log.e("WeatherAPI", "Bad response: " + response.code());
                    runOnUiThread(() -> {
                        Toast.makeText(Calendar.this, "Weather API error: " + response.code(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    // This method parses the JSON weather data and updates the UI
    private void parseWeatherData(String response) {
        try {
            JSONObject json = new JSONObject(response);

            // Extract weather details
            String description = json.getJSONArray("weather").getJSONObject(0).getString("description");
            String icon = json.getJSONArray("weather").getJSONObject(0).getString("icon");
            double temp = json.getJSONObject("main").getDouble("temp");

            // Get the name of the location (good for confirming what location the weather is for)
            String locationName = json.getString("name");

            // Update UI on main thread
            runOnUiThread(() -> {
                // Update texts
                weatherDescription.setText(description.substring(0, 1).toUpperCase() + description.substring(1)); // Capitalize
                weatherTemperature.setText(String.format(Locale.getDefault(), "%.1f°C", temp));

                // Load weather icon dynamically
                String iconUrl = "https://openweathermap.org/img/wn/" + icon + "@2x.png";
                Glide.with(Calendar.this).load(iconUrl).into(weatherIcon);

                // CRITICAL: Make the weather container visible
                LinearLayout weatherContainer = findViewById(R.id.weatherContainer);
                if (weatherContainer != null) {
                    weatherContainer.setVisibility(View.VISIBLE);
                }

                Log.d("WeatherAPI", "Weather displayed for: " + locationName);
            });
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("WeatherAPI", "Parse error: " + e.getMessage());
        }
    }
    // Update your existing getWeatherData method with this version
    private void getWeatherData(String city) {
        // Show a loading indicator or message (optional)
        Toast.makeText(Calendar.this, "Loading weather for " + city + "...", Toast.LENGTH_SHORT).show();

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();

        // API URL
        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&units=metric&appid=" + API_KEY;
        Log.d("WeatherAPI", "Fetching city weather from: " + url);

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.e("WeatherAPI", "City weather fetch failed: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(Calendar.this, "Failed to fetch weather data", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    final String responseData = response.body().string();
                    Log.d("WeatherAPI", "City weather response: " + responseData);
                    runOnUiThread(() -> parseWeatherData(responseData));
                } else {
                    Log.e("WeatherAPI", "City weather bad response: " + response.code());
                    runOnUiThread(() -> Toast.makeText(Calendar.this, "Weather API error: " + response.code(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
    private void updateSelectedDates() {
        if (!selectedStartDate.isEmpty() && !selectedEndDate.isEmpty()) {
            long days = calculateDays(selectedStartDate, selectedEndDate);
            selectedDates.setText(selectedStartDate + " - " + selectedEndDate + " (" + days + " days / " + (days - 1) + " nights)");
            selectedDates.setVisibility(View.VISIBLE);
        }
    }

    private long calculateDays(String start, String end) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            long diff = sdf.parse(end).getTime() - sdf.parse(start).getTime();
            return TimeUnit.MILLISECONDS.toDays(diff) + 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void saveData() {
        // Validate inputs
        String destination = searchPlace.getText().toString().trim();
        String name = tripName.getText().toString().trim();

        if (destination.isEmpty()) {
            Toast.makeText(this, "Please select a destination", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedStartDate.isEmpty() || selectedEndDate.isEmpty()) {
            Toast.makeText(this, "Please select start and end dates", Toast.LENGTH_SHORT).show();
            return;
        }

        if (tripType.isEmpty()) {
            Toast.makeText(this, "Please select trip type (Business or Vacation)", Toast.LENGTH_SHORT).show();
            return;
        }

        // If name is empty, use destination as name
        if (name.isEmpty()) {
            name = "Trip to " + destination;
        }

        // Create a Trip object with the collected data
        Trip trip = new Trip(name, destination, selectedStartDate, selectedEndDate, tripType, calculateDays(selectedStartDate, selectedEndDate));

        // Save to Firebase
        String tripId = FirebaseDatabase.getInstance().getReference().child("userTrips").push().getKey();
        if (tripId != null) {
            FirebaseDatabase.getInstance().getReference("userTrips")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child(tripId)
                    .setValue(trip)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Save packing items and budget data
                            savePackingItemsToFirebase(tripId);
                            saveBudgetDataToFirebase(tripId);
                            Toast.makeText(this, "Trip saved successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Failed to save trip", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }


    private void saveTripToFirebase() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get the current user ID
        String tripId = FirebaseDatabase.getInstance().getReference().child("userTrips").push().getKey(); // Create a new unique trip ID

        Trip trip = new Trip("Trip to Colombo", "Colombo", "2025-05-01", "2025-05-10", "Vacation", 10); // Example trip data

        FirebaseDatabase.getInstance().getReference("userTrips")
                .child(userId)
                .child(tripId) // Use tripId as a unique key
                .setValue(trip)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Trip saved successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to save trip", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Inside Calendar.java (or in the relevant Activity for packing items)

    private void savePackingItemsToFirebase(String tripId) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference itemsRef = FirebaseDatabase.getInstance().getReference("userTrips")
                .child(userId)
                .child(tripId)
                .child("packingItems");

        // Create some example packing items
        PackingItems item1 = new PackingItems("Swimsuit", 2, false, 25.99,"",0,0);
        PackingItems item2 = new PackingItems("Towel", 1, false, 12.50,"",0,0);

        // Generate unique IDs for the items and save them to Firebase
        String itemId1 = itemsRef.push().getKey();
        if (itemId1 != null) {
            itemsRef.child(itemId1).setValue(item1);
        }

        String itemId2 = itemsRef.push().getKey();
        if (itemId2 != null) {
            itemsRef.child(itemId2).setValue(item2);
        }
    }


    // Inside Calendar.java (or in the relevant Activity for budget data)

    private void saveBudgetDataToFirebase(String tripId) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference budgetRef = FirebaseDatabase.getInstance().getReference("userTrips")
                .child(userId)
                .child(tripId)
                .child("budgetData");

        Map<String, Double> budgetData = new HashMap<>();
        budgetData.put("accommodation", 300.0);
        budgetData.put("transport", 150.0);
        budgetData.put("food", 100.0);
        budgetData.put("activities", 50.0);
        budgetData.put("miscellaneous", 30.0);
        budgetData.put("total", 630.0); // Total budget (sum of all categories)

        budgetRef.setValue(budgetData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Budget data saved successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save budget data", Toast.LENGTH_SHORT).show();
                });
    }



}


