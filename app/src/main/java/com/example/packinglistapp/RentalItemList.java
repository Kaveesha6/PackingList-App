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

public class RentalItemList extends AppCompatActivity {

    private List<RentalItem> allItems;
    private EditText editTextAddItem;
    private Button buttonAddItem;
    private Button btnAddToCheckList;
    private ImageButton btnBack;
    private RentalItemAdapter adapter;
    private RecyclerView recyclerViewItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rental_items_list);

        // Initialize UI components
        initializeViews();

        // Set up RecyclerView with adapter
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
        allItems = createRentalItems();

        // Set up RecyclerView
        recyclerViewItems.setLayoutManager(new LinearLayoutManager(this));

        // Initialize and set adapter
        adapter = new RentalItemAdapter(this, allItems);
        recyclerViewItems.setAdapter(adapter);
    }

    private void setupRecyclerView() {
        // Already handled in initializeViews()
    }

    private void setupClickListeners() {
        buttonAddItem.setOnClickListener(v -> {
            String itemName = editTextAddItem.getText().toString().trim();
            if (!itemName.isEmpty()) {
                // Add the new item to the list
                RentalItem newItem = new RentalItem(itemName);
                allItems.add(newItem);

                // Clear the input field
                editTextAddItem.setText("");

                // Notify adapter about the new item
                adapter.notifyItemInserted(allItems.size() - 1);

                // Save the item to Firebase
                saveRentalItemToFirebase(newItem);

                Toast.makeText(this, "Item added", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please enter an item name", Toast.LENGTH_SHORT).show();
            }
        });

        // Add to checklist button
        btnAddToCheckList.setOnClickListener(v -> {
            // Count selected items
            int selectedCount = 0;
            for (RentalItem item : allItems) {
                if (item.isChecked()) {
                    selectedCount++;
                }
            }

            if (selectedCount > 0) {
                // Here you would add the selected items to your checklist
                // This could involve saving to a database, or navigating to another activity
                Toast.makeText(this, selectedCount + " items added to checklist", Toast.LENGTH_SHORT).show();

                // Optionally, clear selections after adding to checklist
                for (RentalItem item : allItems) {
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

    private List<RentalItem> createRentalItems() {
        List<RentalItem> items = new ArrayList<>();

        // Add all the Rental items
        items.add(new RentalItem("Swimsuit / Swim trunks"));
        items.add(new RentalItem("Swim cap"));
        items.add(new RentalItem("Goggles (anti-fog, UV protection)"));
        items.add(new RentalItem("Towel (quick-dry or regular)"));
        items.add(new RentalItem("Flip-flops or pool slides"));
        items.add(new RentalItem("Kickboard"));

        return items;
    }

    private void saveRentalItemToFirebase(RentalItem rentalItem) {
        // Get the current user's ID (replace with actual user ID)
        String userId = "exampleUserId";  // Replace with actual user ID
        String tripId = "exampleTripId";  // Replace with actual trip ID

        // Save the rental item to Firebase
        DatabaseReference rentalRef = FirebaseDatabase.getInstance().getReference("userTrips")
                .child(userId)
                .child(tripId)
                .child("rentalItems");

        String itemId = rentalRef.push().getKey(); // Get a unique key for the item
        if (itemId != null) {
            rentalRef.child(itemId).setValue(rentalItem).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(RentalItemList.this, "Rental item saved successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RentalItemList.this, "Failed to save item", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void getRentalItemsFromFirebase(String userId, String tripId) {
        DatabaseReference rentalRef = FirebaseDatabase.getInstance().getReference("userTrips")
                .child(userId)
                .child(tripId)
                .child("rentalItems");

        rentalRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                allItems.clear(); // Clear the list before adding new items
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    RentalItem rentalItem = snapshot.getValue(RentalItem.class);
                    if (rentalItem != null) {
                        allItems.add(rentalItem);
                    }
                }
                adapter.notifyDataSetChanged(); // Notify adapter to update RecyclerView
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseError", "Error retrieving rental items", databaseError.toException());
            }
        });
    }
}
