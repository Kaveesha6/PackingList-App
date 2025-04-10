package com.example.packinglistapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class HotelItemsList extends AppCompatActivity {

    private List<HotelItem> allItems;
    private EditText editTextAddItem;
    private Button buttonAddItem;
    private Button btnAddToCheckList;
    private ImageButton btnBack;
    private HotelItemAdapter adapter;
    private RecyclerView recyclerViewItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_items_list);

        // Initialize UI components
        initializeViews();

        // Set up the RecyclerView with adapter
        setupRecyclerView();

        // Set up click listeners for buttons
        setupClickListeners();
    }

    private void initializeViews() {
        recyclerViewItems = findViewById(R.id.recyclerViewItems);
        editTextAddItem = findViewById(R.id.editTextAddItem);
        buttonAddItem = findViewById(R.id.buttonAddItem);
        btnBack = findViewById(R.id.btnBack);
        btnAddToCheckList = findViewById(R.id.btnAddToCheckList);

        // Initialize item list
        allItems = createHotelItems();

        // Set up RecyclerView
        recyclerViewItems.setLayoutManager(new LinearLayoutManager(this));

        // Initialize and set adapter
        adapter = new HotelItemAdapter(this, allItems);
        recyclerViewItems.setAdapter(adapter);
    }

    private void setupRecyclerView() {
        // Set up RecyclerView if necessary (already handled in initializeViews)
    }

    private void setupClickListeners() {
        buttonAddItem.setOnClickListener(v -> {
            String itemName = editTextAddItem.getText().toString().trim();
            if (!itemName.isEmpty()) {
                // Add the new item to the list
                HotelItem newItem = new HotelItem(itemName);
                allItems.add(newItem);

                // Clear the input field
                editTextAddItem.setText("");

                // Notify adapter about the new item
                adapter.notifyItemInserted(allItems.size() - 1);

                // Save the item to Firebase
                saveHotelItemToFirebase(newItem);

                Toast.makeText(this, "Item added", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please enter an item name", Toast.LENGTH_SHORT).show();
            }
        });

        // Add to checklist button
        btnAddToCheckList.setOnClickListener(v -> {
            // Count selected items
            int selectedCount = 0;
            for (HotelItem item : allItems) {
                if (item.isChecked()) {
                    selectedCount++;
                }
            }

            if (selectedCount > 0) {
                // Save the selected items to checklist (or perform any other action)
                Toast.makeText(this, selectedCount + " items added to checklist", Toast.LENGTH_SHORT).show();

                // Optionally, clear selections after adding to checklist
                for (HotelItem item : allItems) {
                    if (item.isChecked()) {
                        item.setChecked(false);
                    }
                }
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Please select items to add to checklist", Toast.LENGTH_SHORT).show();
            }
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private List<HotelItem> createHotelItems() {
        List<HotelItem> items = new ArrayList<>();

        // Add some initial items (can be replaced by data from Firebase)
        items.add(new HotelItem("Swimsuit / Swim trunks"));
        items.add(new HotelItem("Swim cap"));
        items.add(new HotelItem("Goggles (anti-fog, UV protection)"));
        items.add(new HotelItem("Towel (quick-dry or regular)"));

        return items;
    }

    private void saveHotelItemToFirebase(HotelItem hotelItem) {
        // Get the current user's ID (you'll need to replace this with actual user ID)
        String userId = "exampleUserId";  // Replace with actual user ID
        String tripId = "exampleTripId";  // Replace with actual trip ID

        // Save the hotel item to Firebase
        DatabaseReference hotelRef = FirebaseDatabase.getInstance().getReference("userTrips")
                .child(userId)
                .child(tripId)
                .child("hotelItems");

        String itemId = hotelRef.push().getKey(); // Get a unique key for the item
        if (itemId != null) {
            hotelRef.child(itemId).setValue(hotelItem).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(HotelItemsList.this, "Hotel item saved successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(HotelItemsList.this, "Failed to save item", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void getHotelItemsFromFirebase(String userId, String tripId) {
        // Retrieve hotel items from Firebase
        DatabaseReference hotelRef = FirebaseDatabase.getInstance().getReference("userTrips")
                .child(userId)
                .child(tripId)
                .child("hotelItems");

        hotelRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                allItems.clear();  // Clear the list before adding new items
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    HotelItem hotelItem = snapshot.getValue(HotelItem.class);
                    if (hotelItem != null) {
                        allItems.add(hotelItem);
                    }
                }
                adapter.notifyDataSetChanged(); // Notify adapter to update RecyclerView
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseError", "Error retrieving hotel items", databaseError.toException());
            }
        });
    }
}
