package com.example.packinglistapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class TripDetails extends AppCompatActivity {

    private String tripId;
    private TextView tripNameTextView, tripDateTextView, tripDestinationTextView, tripTypeTextView;
    private ImageView tripImageView, backButton;
    private RecyclerView packingListsRecyclerView;
    private PackingListAdapter packingListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);

        // Get trip ID from intent
        tripId = getIntent().getStringExtra("TRIP_ID");

        // Initialize views
        tripNameTextView = findViewById(R.id.tripNameTextView);
        tripDateTextView = findViewById(R.id.tripDatesTextView);
        tripDestinationTextView = findViewById(R.id.tripDestinationTextView);
        tripTypeTextView = findViewById(R.id.tripTypeTextView);
        tripImageView = findViewById(R.id.tripImageView);
        backButton = findViewById(R.id.backButton);
        packingListsRecyclerView = findViewById(R.id.packingListsRecyclerView);

        // Set up back button
        // Back Button Logic
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            // Finish current activity and go back to the previous one
            finish();
        });


        // Set up RecyclerView
        packingListsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        packingListAdapter = new PackingListAdapter(new ArrayList<>());
        packingListsRecyclerView.setAdapter(packingListAdapter);

        // Load trip details
        loadTripDetails();

        // Load packing lists
        loadPackingLists();

        // Load the trip details (replace with actual tripId)
        String tripId = "exampleTripId";  // Use actual tripId passed from previous activity
        loadTripDetailsFromFirebase(tripId);
        loadPackingItemsFromFirebase(tripId);
    }

    private void loadTripDetails() {
        DatabaseReference tripRef = FirebaseDatabase.getInstance().getReference("trips").child(tripId);
        tripRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Trip trip = dataSnapshot.getValue(Trip.class);
                if (trip != null) {
                    tripNameTextView.setText(trip.getName());
                    tripDateTextView.setText(trip.getStartDate() + " - " + trip.getEndDate());
                    tripDestinationTextView.setText(trip.getDestination());
                    tripTypeTextView.setText(trip.getTripType());

                    if (trip.getImageUrl() != null && !trip.getImageUrl().isEmpty()) {
                        Glide.with(TripDetails.this)
                                .load(trip.getImageUrl())
                                .centerCrop()
                                .into(tripImageView);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(TripDetails.this, "Failed to load trip details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPackingLists() {
        DatabaseReference packingListsRef = FirebaseDatabase.getInstance().getReference("packingLists");
        Query query = packingListsRef.orderByChild("tripId").equalTo(tripId);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<PackingList> packingLists = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    PackingList packingList = snapshot.getValue(PackingList.class);
                    if (packingList != null) {
                        packingLists.add(packingList);
                    }
                }

                packingListAdapter.updateData(packingLists);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(TripDetails.this, "Failed to load packing lists", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Adapter for packing lists
    private class PackingListAdapter extends RecyclerView.Adapter<PackingListAdapter.ViewHolder> {
        private List<PackingList> packingLists;

        public PackingListAdapter(List<PackingList> packingLists) {
            this.packingLists = packingLists;
        }

        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_packing_list, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            PackingList packingList = packingLists.get(position);
            holder.categoryNameTextView.setText(packingList.getCategoryName());

            int totalItems = packingList.getItemCount();
            int checkedItems = packingList.getCheckedItemCount();

            holder.progressTextView.setText(checkedItems + "/" + totalItems);

            // Set click listener to view packing list items
            holder.itemView.setOnClickListener(v -> {
                // Navigate to packing list items activity
                // You need to create this activity
            });
        }

        @Override
        public int getItemCount() {
            return packingLists.size();
        }

        public void updateData(List<PackingList> newPackingLists) {
            this.packingLists = newPackingLists;
            notifyDataSetChanged();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView categoryNameTextView, progressTextView;

            ViewHolder(View itemView) {
                super(itemView);
                categoryNameTextView = itemView.findViewById(R.id.categoryNameTextView);
                progressTextView = itemView.findViewById(R.id.progressTextView);
            }
        }
    }

    // Inside TripDetail.java

    private void loadTripDetailsFromFirebase(String tripId) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();  // Get the current user ID
        DatabaseReference tripRef = FirebaseDatabase.getInstance().getReference("userTrips")
                .child(userId)
                .child(tripId);  // Reference to the specific trip

        tripRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Fetch the trip details
                Trip trip = dataSnapshot.getValue(Trip.class);

                // Update your UI with the trip details (e.g., name, destination, dates)
                if (trip != null) {
                    // Example: populate the UI elements with the trip data
                    TextView tripName = findViewById(R.id.tripNameTextView);
                    tripName.setText(trip.getName());

                    TextView tripDestination = findViewById(R.id.tripDestinationTextView);
                    tripDestination.setText(trip.getDestination());

                    TextView tripDates = findViewById(R.id.tripDatesTextView);
                    tripDates.setText(trip.getStartDate() + " - " + trip.getEndDate());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Error loading trip details", databaseError.toException());
            }
        });
    }

    // Inside TripDetail.java

    private void loadPackingItemsFromFirebase(String tripId) {
        DatabaseReference packingItemsRef = FirebaseDatabase.getInstance().getReference("userTrips")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(tripId)
                .child("packingItems");

        packingItemsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<PackingList> packingLists = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    PackingItems item = snapshot.getValue(PackingItems.class);
                    if (item != null) {
                        PackingList packingList = new PackingList();
                        packingList.setCategoryName(item.getName()); // Adjust based on your class properties
                        packingLists.add(packingList);
                    }
                }
                packingListAdapter.updateData(packingLists); // Update adapter data
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(TripDetails.this, "Failed to load packing items", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Inside TripDetail.java

    private void loadBudgetDataFromFirebase(String tripId) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();  // Get the current user ID
        DatabaseReference budgetRef = FirebaseDatabase.getInstance().getReference("userTrips")
                .child(userId)
                .child(tripId)
                .child("budgetData");  // Reference to the budget data for the specific trip

        budgetRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Double> budgetData = (Map<String, Double>) dataSnapshot.getValue();

                if (budgetData != null) {
                    // Populate UI with the budget data (e.g., accommodation, transport, etc.)
                    TextView accommodationText = findViewById(R.id.accommodationTextView);
                    accommodationText.setText("$" + budgetData.get("accommodation"));

                    TextView transportText = findViewById(R.id.transportTextView);
                    transportText.setText("$" + budgetData.get("transport"));

                    TextView foodText = findViewById(R.id.foodTextView);
                    foodText.setText("$" + budgetData.get("food"));

                    TextView activitiesText = findViewById(R.id.activitiesTextView);
                    activitiesText.setText("$" + budgetData.get("activities"));

                    TextView miscellaneousText = findViewById(R.id.miscellaneousTextView);
                    miscellaneousText.setText("$" + budgetData.get("miscellaneous"));

                    TextView totalText = findViewById(R.id.totalTextView);
                    totalText.setText("$" + budgetData.get("total"));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Error loading budget data", databaseError.toException());
            }
        });
    }



}