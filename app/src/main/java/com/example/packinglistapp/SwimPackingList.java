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

public class SwimPackingList extends AppCompatActivity {

    private List<SwimItem> allItems;
    private EditText editTextAddItem;
    private Button buttonAddItem;
    private Button btnAddToCheckList;
    private ImageButton btnBack;
    private SwimItemAdapter adapter;  // Declare the adapter at the class level
    private RecyclerView recyclerViewItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swim_packing_list);

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
        allItems = createSwimmingItems();

        // Set up RecyclerView layout manager
        recyclerViewItems.setLayoutManager(new LinearLayoutManager(this));

        // Initialize and set the adapter only once
        adapter = new SwimItemAdapter(this, allItems);
        recyclerViewItems.setAdapter(adapter);
    }

    private void setupRecyclerView() {
        // RecyclerView and adapter setup is already handled in initializeViews()
    }

    private void setupClickListeners() {
        buttonAddItem.setOnClickListener(v -> {
            String itemName = editTextAddItem.getText().toString().trim();
            if (!itemName.isEmpty()) {
                // Add the new item to the list
                allItems.add(new SwimItem(itemName));

                // Clear the input field
                editTextAddItem.setText("");

                // Notify adapter about the new item
                adapter.notifyItemInserted(allItems.size() - 1);

                // Save the item to Firebase
                String userId = "exampleUserId";  // Replace with actual user ID
                String tripId = "exampleTripId";  // Replace with actual trip ID
                saveSwimItemToFirebase(new SwimItem(itemName), userId, tripId);

                Toast.makeText(this, "Item added", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please enter an item name", Toast.LENGTH_SHORT).show();
            }
        });

        // Add to checklist button
        btnAddToCheckList.setOnClickListener(v -> {
            // Count selected items
            int selectedCount = 0;
            for (SwimItem item : allItems) {
                if (item.isChecked()) {
                    selectedCount++;
                }
            }

            if (selectedCount > 0) {
                // Here you would add the selected items to your checklist
                Toast.makeText(this, selectedCount + " items added to checklist", Toast.LENGTH_SHORT).show();

                // Optionally, clear selections after adding to checklist
                for (SwimItem item : allItems) {
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

    private List<SwimItem> createSwimmingItems() {
        List<SwimItem> items = new ArrayList<>();

        // Add all the swimming items
        items.add(new SwimItem("Swimsuit / Swim trunks"));
        items.add(new SwimItem("Swim cap"));
        items.add(new SwimItem("Goggles (anti-fog, UV protection)"));
        items.add(new SwimItem("Towel (quick-dry or regular)"));
        items.add(new SwimItem("Flip-flops or pool slides"));
        items.add(new SwimItem("Kickboard"));
        items.add(new SwimItem("Pull buoy"));
        items.add(new SwimItem("Hand paddles"));
        items.add(new SwimItem("Swim fins"));
        items.add(new SwimItem("Snorkel (if training)"));
        items.add(new SwimItem("Shampoo & body wash (chlorine-removing)"));
        items.add(new SwimItem("Conditioner"));
        items.add(new SwimItem("Deodorant"));
        items.add(new SwimItem("Moisturizer (for after swimming)"));
        items.add(new SwimItem("Hairbrush or comb"));
        items.add(new SwimItem("Water bottle"));
        items.add(new SwimItem("Snack (for post-swim energy)"));
        items.add(new SwimItem("Mesh bag (for wet gear)"));
        items.add(new SwimItem("Lock (for locker)"));
        items.add(new SwimItem("Earplugs (optional)"));
        items.add(new SwimItem("Nose clip (optional)"));

        return items;
    }

    private void saveSwimItemToFirebase(SwimItem swimItem, String userId, String tripId) {
        DatabaseReference swimRef = FirebaseDatabase.getInstance().getReference("userTrips")
                .child(userId)
                .child(tripId)
                .child("swimItems");

        String itemId = swimRef.push().getKey(); // Get a unique key for the item
        if (itemId != null) {
            swimRef.child(itemId).setValue(swimItem).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(SwimPackingList.this, "Item saved successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SwimPackingList.this, "Failed to save item", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void getSwimItemsFromFirebase(String userId, String tripId) {
        DatabaseReference swimRef = FirebaseDatabase.getInstance().getReference("userTrips")
                .child(userId)
                .child(tripId)
                .child("swimItems");

        swimRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                allItems.clear();  // Clear the list before adding new items
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    SwimItem swimItem = snapshot.getValue(SwimItem.class);
                    if (swimItem != null) {
                        allItems.add(swimItem);
                    }
                }
                adapter.notifyDataSetChanged(); // Notify adapter to update RecyclerView
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseError", "Error retrieving swim items", databaseError.toException());
            }
        });
    }
}
