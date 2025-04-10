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

public class FamilyFrendsItemList extends AppCompatActivity {

    private List<FamilyFrendsItem> allItems;
    private EditText editTextAddItem;
    private Button buttonAddItem;
    private Button btnAddToCheckList;
    private ImageButton btnBack;
    private FamilyFrendsItemAdapter adapter;
    private RecyclerView recyclerViewItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_frends_item_list);

        // Initialize UI components
        initializeViews();

        // Set up RecyclerView with adapter
        setupRecyclerView();

        // Set up click listeners for buttons
        setupClickListeners();

        // Retrieve FamilyFrends items from Firebase
        // Replace with actual userId and tripId
        String userId = "exampleUserId"; // Replace with actual user ID
        String tripId = "exampleTripId"; // Replace with actual trip ID
        getFamilyFrendsItemsFromFirebase(userId, tripId);
    }

    private void initializeViews() {
        recyclerViewItems = findViewById(R.id.recyclerViewItems);
        editTextAddItem = findViewById(R.id.editTextAddItem);
        buttonAddItem = findViewById(R.id.buttonAddItem);
        btnBack = findViewById(R.id.btnBack);
        btnAddToCheckList = findViewById(R.id.btnAddToCheckList);

        // Initialize item list
        allItems = createFamilyFrendsItems();

        // Set up RecyclerView
        recyclerViewItems.setLayoutManager(new LinearLayoutManager(this));

        // Initialize and set adapter
        adapter = new FamilyFrendsItemAdapter(this, allItems);
        recyclerViewItems.setAdapter(adapter);
    }

    private void setupRecyclerView() {
        // RecyclerView is set up in initializeViews
    }

    private void setupClickListeners() {
        buttonAddItem.setOnClickListener(v -> {
            String itemName = editTextAddItem.getText().toString().trim();
            if (!itemName.isEmpty()) {
                // Add the new item to the list
                FamilyFrendsItem newItem = new FamilyFrendsItem(itemName);
                allItems.add(newItem);

                // Clear the input field
                editTextAddItem.setText("");

                // Notify adapter about the new item
                adapter.notifyItemInserted(allItems.size() - 1);

                // Save the item to Firebase
                saveFamilyFrendsItemToFirebase(newItem);

                Toast.makeText(this, "Item added", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please enter an item name", Toast.LENGTH_SHORT).show();
            }
        });

        // Add to checklist button
        btnAddToCheckList.setOnClickListener(v -> {
            // Count selected items
            int selectedCount = 0;
            for (FamilyFrendsItem item : allItems) {
                if (item.isChecked()) {
                    selectedCount++;
                }
            }

            if (selectedCount > 0) {
                // Here you would add the selected items to your checklist
                // This could involve saving to a database, or navigating to another activity
                Toast.makeText(this, selectedCount + " items added to checklist", Toast.LENGTH_SHORT).show();

                // Optionally, clear selections after adding to checklist
                for (FamilyFrendsItem item : allItems) {
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

    private List<FamilyFrendsItem> createFamilyFrendsItems() {
        List<FamilyFrendsItem> items = new ArrayList<>();

        // Add initial items (can be replaced by data from Firebase)
        items.add(new FamilyFrendsItem("Towel"));
        items.add(new FamilyFrendsItem("Sunscreen"));
        items.add(new FamilyFrendsItem("Flip-flops"));
        items.add(new FamilyFrendsItem("Shampoo"));
        items.add(new FamilyFrendsItem("Toothbrush"));

        return items;
    }

    private void saveFamilyFrendsItemToFirebase(FamilyFrendsItem familyFrendsItem) {
        // Get the current user's ID (replace with actual user ID)
        String userId = "exampleUserId";  // Replace with actual user ID
        String tripId = "exampleTripId";  // Replace with actual trip ID

        // Save the FamilyFrends item to Firebase
        DatabaseReference familyFrendsRef = FirebaseDatabase.getInstance().getReference("userTrips")
                .child(userId)
                .child(tripId)
                .child("familyFrendsItems");

        String itemId = familyFrendsRef.push().getKey(); // Get a unique key for the item
        if (itemId != null) {
            familyFrendsRef.child(itemId).setValue(familyFrendsItem).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(FamilyFrendsItemList.this, "FamilyFrends item saved successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(FamilyFrendsItemList.this, "Failed to save item", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void getFamilyFrendsItemsFromFirebase(String userId, String tripId) {
        DatabaseReference familyFrendsRef = FirebaseDatabase.getInstance().getReference("userTrips")
                .child(userId)
                .child(tripId)
                .child("familyFrendsItems");

        familyFrendsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                allItems.clear();  // Clear the list before adding new items
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    FamilyFrendsItem familyFrendsItem = snapshot.getValue(FamilyFrendsItem.class);
                    if (familyFrendsItem != null) {
                        allItems.add(familyFrendsItem);
                    }
                }
                adapter.notifyDataSetChanged(); // Notify adapter to update RecyclerView
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseError", "Error retrieving FamilyFrends items", databaseError.toException());
            }
        });
    }
}
