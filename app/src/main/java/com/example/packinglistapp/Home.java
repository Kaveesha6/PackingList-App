package com.example.packinglistapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import android.util.Log;
import java.util.HashMap;

import java.util.ArrayList;
import java.util.List;

public class Home extends AppCompatActivity {

    private ImageView profileImageView;
    private ImageView moreOptionsMenuIcon;
    private TextView userNameTextView;
    private EditText searchEditText;
    private ImageView clearSearchButton;
    private RecyclerView tripsRecyclerView;
    private TripAdapter tripAdapter;
    private SharedPreferences preferences;
    private Button btnGetStarted;
    private FirebaseAuth mAuth;

    // Firebase references
    private FirebaseDatabase database;
    private DatabaseReference tripsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Get selected items
        Map<String, List<PackingItem>> selectedItems = (HashMap<String, List<PackingItem>>) getIntent()
                .getSerializableExtra("selected_items");

        if (selectedItems != null) {
            // Display in your RecyclerView
            displaySelectedItems(selectedItems);
        };

        // Initialize Firebase
        database = FirebaseDatabase.getInstance();
        tripsRef = database.getReference("userTrips").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        // Initialize SharedPreferences
        preferences = getSharedPreferences("PackingListPrefs", MODE_PRIVATE);

        // Initialize UI elements
        profileImageView = findViewById(R.id.profileImage);
        moreOptionsMenuIcon = findViewById(R.id.moreOptionsMenu);
        userNameTextView = findViewById(R.id.textGreeting);
        btnGetStarted = findViewById(R.id.get_started);
        searchEditText = findViewById(R.id.searchEditText);
        clearSearchButton = findViewById(R.id.clearSearch);
        tripsRecyclerView = findViewById(R.id.tripsRecyclerView);

        // Set time-based greeting
        setTimeBasedGreeting();

        // Set up RecyclerView
        tripsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadTripsFromFirebase();

        // Set up click listeners
        btnGetStarted.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, Calendar.class);
            startActivity(intent);
        });

        profileImageView.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, UserProfile.class);
            startActivity(intent);
        });

        // Set up more options menu
        moreOptionsMenuIcon.setOnClickListener(v -> showMoreOptionsMenu());

        // Set up search functionality
        setupSearch();

        // Load user data when activity is created
        loadUserData();
    }

    // Method to show the more options menu
    private void showMoreOptionsMenu() {
        PopupMenu popupMenu = new PopupMenu(this, moreOptionsMenuIcon);
        popupMenu.inflate(R.menu.more_options_menu);

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.menu_user_profile) {
                // Navigate to User Profile
                Intent profileIntent = new Intent(Home.this, UserProfile.class);
                startActivity(profileIntent);
                return true;
            }
            else if (itemId == R.id.menu_about_us) {
                // Navigate to About Us
                Intent aboutUsIntent = new Intent(Home.this, AboutUs.class);
                startActivity(aboutUsIntent);
                return true;
            }
            else if (itemId == R.id.menu_logout) {
                // Sign out the user
                signOut();
                return true;
            }
            else if (itemId == R.id.menu_delete_account){
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                DatabaseReference userRef = FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(userId);

                userRef.removeValue().addOnCompleteListener(
                        task -> {
                            if (task.isSuccessful()) {
                                DatabaseReference usernameRef = FirebaseDatabase.getInstance()
                                        .getReference("usernames");

                                Query query = usernameRef.equalTo(userId);

                                query.getRef().removeValue();

                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                                if (user != null) {
                                    user.delete()
                                            .addOnCompleteListener(t -> {
                                                if (t.isSuccessful()) {
                                                    signOut();
                                                    Log.d("Firebase", "User account deleted.");
                                                    // Optionally: redirect to login or show confirmation
                                                } else {
                                                    Log.e("Firebase", "Error deleting user", t.getException());
                                                }
                                            });
                                }
                            }
                        }
                );
            }

            return false;
        });

        popupMenu.show();
    }

    // Method to sign out the user
    private void signOut() {
        // Sign out from Firebase Auth
        mAuth.signOut();

        // Clear user preferences if necessary
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();  // Clear all data
        // Or clear specific data:
        // editor.remove("fullName");
        // editor.remove("profile_image_uri");
        editor.apply();

        // Navigate to login/welcome screen
        Intent intent = new Intent(Home.this, Login.class); // Replace Login with your actual login/welcome activity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
        startActivity(intent);
        finish();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    // Add this method to your Home class

    private void setTimeBasedGreeting() {
        TextView textTitle = findViewById(R.id.textTitle);

        // Get current hour of day (24-hour format)
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int hourOfDay = calendar.get(java.util.Calendar.HOUR_OF_DAY);

        // Set greeting based on time of day
        String greeting;
        if (hourOfDay >= 1 && hourOfDay < 12) {
            greeting = "Good Morning!";
        } else if (hourOfDay >= 12 && hourOfDay < 18) {
            greeting = "Good Afternoon!";
        } else if (hourOfDay >= 18 && hourOfDay < 22) {
            greeting = "Good Evening!";
        } else {
            greeting = "Good Night!";
        }

        // Set the greeting text
        textTitle.setText(greeting);
    }

    private void displaySelectedItems(Map<String, List<PackingItem>> itemsByCategory) {
        for (Map.Entry<String, List<PackingItem>> entry : itemsByCategory.entrySet()) {
            String category = entry.getKey();
            List<PackingItem> items = entry.getValue();

            Log.d("HomeActivity", "Category: " + category);
            for (PackingItem item : items) {
                Log.d("HomeActivity", "- " + item.getName() + " (Qty: " + item.getQuantity() + ")");
            }
        }
    }

//    private void setupRecyclerView() {
//        // Set layout manager
//        tripsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//
//        // Initialize with sample data or load from database/preferences
//        List<Trip> tripList = getSampleTrips(); // Replace with your data source
//
//        // Create and set adapter
//        tripAdapter = new TripAdapter(tripList);
//        tripsRecyclerView.setAdapter(tripAdapter);
//    }

    private void loadTripsFromFirebase() {
        tripsRef.orderByChild("startDate").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Trip> trips = new ArrayList<>();

                for (DataSnapshot tripSnapshot : dataSnapshot.getChildren()) {
                    String tripId = tripSnapshot.getKey();
                    String name = tripSnapshot.child("name").getValue(String.class);
                    String tripDate = tripSnapshot.child("startDate").getValue(String.class);
                    int itemCount = tripSnapshot.child("itemCount").getValue(Integer.class);
                    Trip trip = new Trip(tripId,name,tripDate,itemCount);
                    trips.add(trip);
                }

                // Update the adapter with the loaded trips
                Collections.reverse(trips);
                tripAdapter = new TripAdapter(trips);
                tripsRecyclerView.setAdapter(tripAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private List<Trip> getSampleTrips() {
        // This is a placeholder. In a real app, you would load this data from a database or API
        List<Trip> trips = new ArrayList<>();
        trips.add(new Trip("123","Summer Vacation", "June 15 - June 30", 12));
        trips.add(new Trip("456","Business Trip", "April 10 - April 12", 5));
        trips.add(new Trip("789","Weekend Getaway", "May 5 - May 7", 8));
        return trips;
    }

    private void setupSearch() {
        // Set up clear button visibility
        clearSearchButton.setVisibility(View.GONE);

        // Add text change listener
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Show/hide clear button based on text
                clearSearchButton.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);

                // Filter trips list
                if (tripAdapter != null) {
                    tripAdapter.filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });

        // Set up clear button
        clearSearchButton.setOnClickListener(v -> {
            searchEditText.setText("");
            searchEditText.clearFocus();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh user data when returning to this activity
        loadUserData();
        // Update the time-based greeting
        setTimeBasedGreeting();
    }

    private void loadUserData() {
        // Load user data from SharedPreferences
        String fullName = preferences.getString("fullName", "Olivia Johnson");
        String imageUriString = preferences.getString("profile_image_uri", "");

        // Update the user name with proper formatting
        userNameTextView.setText("Hi, " + mAuth.getCurrentUser().getDisplayName() + "!");
        Uri gmailProfilePicture = mAuth.getCurrentUser().getPhotoUrl();

        // Update profile image if available
        if (!imageUriString.isEmpty()) {
            try {
                Uri imageUri = Uri.parse(imageUriString);
                profileImageView.setImageURI(imageUri);
            } catch (Exception e) {
                e.printStackTrace();
                // If there's an error, use the default image
                profileImageView.setImageURI(gmailProfilePicture);
            }
        }
    }

    // Method to increment packing list count when a new list is created
    // This can be called from other activities when a new packing list is created
    public static void incrementPackingListCount(SharedPreferences prefs) {
        int currentCount = prefs.getInt("packing_lists_count", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("packing_lists_count", currentCount + 1);
        editor.apply();
    }

    // Inner classes for RecyclerView
    public static class Trip {
        private String tripId;
        private String name;
        private String date;
        private int itemCount;

        public Trip(String tripId, String name, String date, int itemCount) {
            this.tripId = tripId;
            this.name = name;
            this.date = date;
            this.itemCount = itemCount;
        }

        public String getTripId() {
            return tripId;
        }

        public void setTripId(String tripId) {
            this.tripId = tripId;
        }

        public String getName() {
            return name;
        }

        public String getDate() {
            return date;
        }

        public int getItemCount() {
            return itemCount;
        }
    }

    public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripViewHolder> {
        private List<Trip> tripList;
        private List<Trip> filteredTripList;

        public TripAdapter(List<Trip> tripList) {
            this.tripList = tripList;
            this.filteredTripList = new ArrayList<>(tripList);
        }

        @Override
        public TripViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_trip, parent, false);
            return new TripViewHolder(view);
        }

        @Override
        public void onBindViewHolder(TripViewHolder holder, int position) {
            Trip trip = filteredTripList.get(position);
            holder.tripNameTextView.setText(trip.getName());
            holder.tripDateTextView.setText(trip.getDate());
            holder.itemCountTextView.setText(trip.getItemCount() + " items");

            // Set click listener for the item
            holder.itemView.setOnClickListener(v -> {
                // Navigate to trip details activity
                Intent intent = new Intent(Home.this, Calendar.class);
                intent.putExtra("tripId",trip.getTripId());
                startActivity(intent);
            });

            holder.itemView.setOnLongClickListener(v -> {
                new AlertDialog.Builder(holder.itemView.getContext())
                        .setTitle("Confirm")
                        .setMessage("Delete this item?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            // Get the current user's ID (you'll need to replace this with actual user ID)
                            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();  // Replace with actual user ID

                            // Save the hotel item to Firebase
                            DatabaseReference hotelRef = FirebaseDatabase.getInstance().getReference("userTrips")
                                    .child(userId)
                                    .child(trip.getTripId());

                            hotelRef.removeValue().addOnCompleteListener(
                                    task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(Home.this, "Trip removed successfully", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(Home.this, "Failed to remove Trip", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                            );
                        })
                        .setNegativeButton("No", null)
                        .show();

                return true;
            });
        }

        @Override
        public int getItemCount() {
            return filteredTripList.size();
        }

        public void filter(String query) {
            filteredTripList.clear();

            if (query.isEmpty()) {
                filteredTripList.addAll(tripList);
            } else {
                String lowerCaseQuery = query.toLowerCase();
                for (Trip trip : tripList) {
                    if (trip.getName().toLowerCase().contains(lowerCaseQuery) ||
                            trip.getDate().toLowerCase().contains(lowerCaseQuery)) {
                        filteredTripList.add(trip);
                    }
                }
            }

            notifyDataSetChanged();
        }

        class TripViewHolder extends RecyclerView.ViewHolder {
            TextView tripNameTextView;
            TextView tripDateTextView;
            TextView itemCountTextView;

            TripViewHolder(View itemView) {
                super(itemView);
                tripNameTextView = itemView.findViewById(R.id.tripNameTextView);
                tripDateTextView = itemView.findViewById(R.id.tripDateTextView);
                itemCountTextView = itemView.findViewById(R.id.itemCountTextView);
            }
        }
    }
}