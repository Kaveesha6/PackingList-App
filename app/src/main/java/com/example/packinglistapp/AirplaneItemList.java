package com.example.packinglistapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AirplaneItemList extends AppCompatActivity {

    private List<AirplaneItem> allItems;
    private EditText editTextAddItem;
    private Button buttonAddItem;
    private Button btnAddToCheckList;
    private ImageButton btnBack;
    private AirplaneItemAdapter adapter;
    private RecyclerView recyclerViewItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_airplane_items_list);

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

        // Set up RecyclerView layout manager
        recyclerViewItems.setLayoutManager(new LinearLayoutManager(this));

        // Initialize item list
        allItems = createAirplaneItems();

        // Initialize and set adapter
        adapter = new AirplaneItemAdapter(this, allItems);
        recyclerViewItems.setAdapter(adapter);
    }

    private void setupRecyclerView() {
        // RecyclerView setup is already handled in initializeViews()
    }

    private void setupClickListeners() {
        buttonAddItem.setOnClickListener(v -> {
            String itemName = editTextAddItem.getText().toString().trim();
            if (!itemName.isEmpty()) {
                // Add the new item to the list
                AirplaneItem newItem = new AirplaneItem(itemName);
                allItems.add(newItem);

                // Clear the input field
                editTextAddItem.setText("");

                // Notify adapter about the new item
                adapter.notifyItemInserted(allItems.size() - 1);

                // Save the item to Firebase
                String userId = "exampleUserId";  // Replace with actual user ID
                String tripId = "exampleTripId";  // Replace with actual trip ID
                saveAirplaneItemToFirebase(newItem, userId, tripId);

                Toast.makeText(this, "Item added", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please enter an item name", Toast.LENGTH_SHORT).show();
            }
        });

        // Add to checklist button
        btnAddToCheckList.setOnClickListener(v -> {
            // Count selected items
            int selectedCount = 0;
            for (AirplaneItem item : allItems) {
                if (item.isChecked()) {
                    selectedCount++;
                }
            }

            if (selectedCount > 0) {
                Toast.makeText(this, selectedCount + " items added to checklist", Toast.LENGTH_SHORT).show();

                // Optionally, clear selections after adding to checklist
                for (AirplaneItem item : allItems) {
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

    private List<AirplaneItem> createAirplaneItems() {
        List<AirplaneItem> items = new ArrayList<>();

        // Add initial items (can be replaced by data from Firebase)
        items.add(new AirplaneItem("Swimsuit / Swim trunks"));
        items.add(new AirplaneItem("Swim cap"));
        items.add(new AirplaneItem("Goggles (anti-fog, UV protection)"));
        items.add(new AirplaneItem("Towel (quick-dry or regular)"));
        items.add(new AirplaneItem("Flip-flops or pool slides"));
        items.add(new AirplaneItem("Kickboard"));

        return items;
    }

    private void saveAirplaneItemToFirebase(AirplaneItem airplaneItem, String userId, String tripId) {
        DatabaseReference airplaneRef = FirebaseDatabase.getInstance().getReference("userTrips")
                .child(userId)
                .child(tripId)
                .child("airplaneItems");

        String itemId = airplaneRef.push().getKey(); // Get a unique key for the item
        if (itemId != null) {
            airplaneRef.child(itemId).setValue(airplaneItem).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(AirplaneItemList.this, "Item saved successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AirplaneItemList.this, "Failed to save item", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void getAirplaneItemsFromFirebase(String userId, String tripId) {
        DatabaseReference airplaneRef = FirebaseDatabase.getInstance().getReference("userTrips")
                .child(userId)
                .child(tripId)
                .child("airplaneItems");

        airplaneRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                allItems.clear();  // Clear the list before adding new items
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    AirplaneItem airplaneItem = snapshot.getValue(AirplaneItem.class);
                    if (airplaneItem != null) {
                        allItems.add(airplaneItem);
                    }
                }
                adapter.notifyDataSetChanged(); // Notify adapter to update RecyclerView
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseError", "Error retrieving airplane items", databaseError.toException());
            }
        });
    }
}
