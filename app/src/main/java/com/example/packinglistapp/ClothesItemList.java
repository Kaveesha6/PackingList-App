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

public class ClothesItemList extends AppCompatActivity {

    private List<ClothesItem> allItems;
    private EditText editTextAddItem;
    private Button buttonAddItem;
    private Button btnAddToCheckList;
    private ImageButton btnBack;
    private ClothesItemAdapter adapter;  // Declare the adapter at the class level
    private RecyclerView recyclerViewItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clothes_items_list);

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
        allItems = createClothesItems();

        // Set up RecyclerView layout manager
        recyclerViewItems.setLayoutManager(new LinearLayoutManager(this));

        // Initialize and set the adapter once
        adapter = new ClothesItemAdapter(this, allItems);
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
                ClothesItem newItem = new ClothesItem(itemName);
                allItems.add(newItem);

                // Clear the input field
                editTextAddItem.setText("");

                // Notify adapter about the new item
                adapter.notifyItemInserted(allItems.size() - 1);

                // Save the item to Firebase
                String userId = "exampleUserId";  // Replace with actual user ID
                String tripId = "exampleTripId";  // Replace with actual trip ID
                saveClothesItemToFirebase(newItem, userId, tripId);

                Toast.makeText(this, "Item added", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please enter an item name", Toast.LENGTH_SHORT).show();
            }
        });

        // Add to checklist button
        btnAddToCheckList.setOnClickListener(v -> {
            // Count selected items
            int selectedCount = 0;
            for (ClothesItem item : allItems) {
                if (item.isChecked()) {
                    selectedCount++;
                }
            }

            if (selectedCount > 0) {
                // Here you would add the selected items to your checklist
                Toast.makeText(this, selectedCount + " items added to checklist", Toast.LENGTH_SHORT).show();

                // Optionally, clear selections after adding to checklist
                for (ClothesItem item : allItems) {
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

    private List<ClothesItem> createClothesItems() {
        List<ClothesItem> items = new ArrayList<>();

        // Add initial items (can be replaced by data from Firebase)
        items.add(new ClothesItem("Swimsuit / Swim trunks"));
        items.add(new ClothesItem("Swim cap"));
        items.add(new ClothesItem("Goggles (anti-fog, UV protection)"));
        items.add(new ClothesItem("Towel (quick-dry or regular)"));
        items.add(new ClothesItem("Flip-flops or pool slides"));
        items.add(new ClothesItem("Kickboard"));

        return items;
    }

    private void saveClothesItemToFirebase(ClothesItem clothesItem, String userId, String tripId) {
        DatabaseReference clothesRef = FirebaseDatabase.getInstance().getReference("userTrips")
                .child(userId)
                .child(tripId)
                .child("clothesItems");

        String itemId = clothesRef.push().getKey(); // Get a unique key for the item
        if (itemId != null) {
            clothesRef.child(itemId).setValue(clothesItem).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(ClothesItemList.this, "Item saved successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ClothesItemList.this, "Failed to save item", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void getClothesItemsFromFirebase(String userId, String tripId) {
        DatabaseReference clothesRef = FirebaseDatabase.getInstance().getReference("userTrips")
                .child(userId)
                .child(tripId)
                .child("clothesItems");

        clothesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                allItems.clear();  // Clear the list before adding new items
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ClothesItem clothesItem = snapshot.getValue(ClothesItem.class);
                    if (clothesItem != null) {
                        allItems.add(clothesItem);
                    }
                }
                adapter.notifyDataSetChanged(); // Notify adapter to update RecyclerView
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseError", "Error retrieving clothes items", databaseError.toException());
            }
        });
    }
}
