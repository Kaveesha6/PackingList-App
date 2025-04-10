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

public class BabyNeedsPackingList extends AppCompatActivity {

    private List<BabyNeedsItem> allItems;
    private EditText editTextAddItem;
    private Button buttonAddItem;
    private Button btnAddToCheckList;
    private ImageButton btnBack;
    private BabyNeedsItemAdapter adapter;  // Declare the adapter at the class level
    private RecyclerView recyclerViewItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baby_needs_packing_list);

        // Initialize UI components
        initializeViews();

        // Set up the RecyclerView with adapter (done only once in initializeViews)
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
        allItems = createBabyNeedsItems();

        // Initialize and set adapter only once
        adapter = new BabyNeedsItemAdapter(this, allItems);
        recyclerViewItems.setAdapter(adapter);
    }

    private void setupRecyclerView() {
        // This is already handled in initializeViews(), no need to reinitialize here
    }

    private void setupClickListeners() {
        buttonAddItem.setOnClickListener(v -> {
            String itemName = editTextAddItem.getText().toString().trim();
            if (!itemName.isEmpty()) {
                // Add the new item to the list
                allItems.add(new BabyNeedsItem(itemName));

                // Clear the input field
                editTextAddItem.setText("");

                // Notify adapter about the new item
                adapter.notifyItemInserted(allItems.size() - 1);

                // Save the item to Firebase
                String userId = "exampleUserId";  // Replace with actual user ID
                String tripId = "exampleTripId";  // Replace with actual trip ID
                saveBabyNeedsItemToFirebase(new BabyNeedsItem(itemName), userId, tripId);

                Toast.makeText(this, "Item added", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please enter an item name", Toast.LENGTH_SHORT).show();
            }
        });

        // Add to checklist button
        btnAddToCheckList.setOnClickListener(v -> {
            // Count selected items
            int selectedCount = 0;
            for (BabyNeedsItem item : allItems) {
                if (item.isChecked()) {
                    selectedCount++;
                }
            }

            if (selectedCount > 0) {
                // Optionally add selected items to your checklist
                Toast.makeText(this, selectedCount + " items added to checklist", Toast.LENGTH_SHORT).show();

                // Optionally, clear selections after adding to checklist
                for (BabyNeedsItem item : allItems) {
                    if (item.isChecked()) {
                        item.setChecked(false);
                    }
                }
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Please select items to add to checklist", Toast.LENGTH_SHORT).show();
            }
        });

        // Back button listener
        btnBack.setOnClickListener(v -> finish());
    }

    private List<BabyNeedsItem> createBabyNeedsItems() {
        List<BabyNeedsItem> items = new ArrayList<>();

        // Add all the baby needs items
        items.add(new BabyNeedsItem("Swimsuit / Swim trunks"));
        items.add(new BabyNeedsItem("Swim cap"));
        items.add(new BabyNeedsItem("Goggles (anti-fog, UV protection)"));
        items.add(new BabyNeedsItem("Towel (quick-dry or regular)"));
        items.add(new BabyNeedsItem("Flip-flops or pool slides"));
        items.add(new BabyNeedsItem("Kickboard"));

        return items;
    }

    private void saveBabyNeedsItemToFirebase(BabyNeedsItem babyNeedsItem, String userId, String tripId) {
        DatabaseReference babyNeedsRef = FirebaseDatabase.getInstance().getReference("userTrips")
                .child(userId)
                .child(tripId)
                .child("babyNeedsItems");

        String itemId = babyNeedsRef.push().getKey(); // Get a unique key for the item
        if (itemId != null) {
            babyNeedsRef.child(itemId).setValue(babyNeedsItem).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(BabyNeedsPackingList.this, "Item saved successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(BabyNeedsPackingList.this, "Failed to save item", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void getBabyNeedsItemsFromFirebase(String userId, String tripId) {
        DatabaseReference babyNeedsRef = FirebaseDatabase.getInstance().getReference("userTrips")
                .child(userId)
                .child(tripId)
                .child("babyNeedsItems");

        babyNeedsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                allItems.clear();  // Clear the list before adding new items
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    BabyNeedsItem babyNeedsItem = snapshot.getValue(BabyNeedsItem.class);
                    if (babyNeedsItem != null) {
                        allItems.add(babyNeedsItem);
                    }
                }
                adapter.notifyDataSetChanged(); // Notify adapter to update RecyclerView
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseError", "Error retrieving baby needs items", databaseError.toException());
            }
        });
    }
}
