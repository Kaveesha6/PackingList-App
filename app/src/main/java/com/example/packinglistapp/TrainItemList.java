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

public class TrainItemList extends AppCompatActivity {

    private List<TrainItem> allItems;
    private EditText editTextAddItem;
    private Button buttonAddItem;
    private Button btnAddToCheckList;
    private ImageButton btnBack;
    private TrainItemAdapter adapter;  // Declare the adapter at the class level
    private RecyclerView recyclerViewItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train_items_list);

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
        allItems = createTrainItems();

        // Initialize and set adapter
        adapter = new TrainItemAdapter(this, allItems);  // Initialize the adapter here
        recyclerViewItems.setAdapter(adapter);
    }

    private void setupRecyclerView() {
        // No need to reinitialize the RecyclerView and adapter here, it's already done in initializeViews()
    }

    private void setupClickListeners() {
        buttonAddItem.setOnClickListener(v -> {
            String itemName = editTextAddItem.getText().toString().trim();
            if (!itemName.isEmpty()) {
                // Add the new item to the list
                TrainItem newItem = new TrainItem(itemName);
                allItems.add(newItem);

                // Clear the input field
                editTextAddItem.setText("");

                // Notify adapter about the new item
                adapter.notifyItemInserted(allItems.size() - 1);  // Correct adapter usage

                // Save the item to Firebase
                String userId = "exampleUserId";  // Replace with actual user ID
                String tripId = "exampleTripId";  // Replace with actual trip ID
                saveTrainItemToFirebase(newItem, userId, tripId);

                Toast.makeText(this, "Item added", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please enter an item name", Toast.LENGTH_SHORT).show();
            }
        });

        // Add to checklist button
        btnAddToCheckList.setOnClickListener(v -> {
            // Count selected items
            int selectedCount = 0;
            for (TrainItem item : allItems) {
                if (item.isChecked()) {
                    selectedCount++;
                }
            }

            if (selectedCount > 0) {
                // Here you would add the selected items to your checklist
                Toast.makeText(this, selectedCount + " items added to checklist", Toast.LENGTH_SHORT).show();

                // Optionally, clear selections after adding to checklist
                for (TrainItem item : allItems) {
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

    private List<TrainItem> createTrainItems() {
        List<TrainItem> items = new ArrayList<>();

        // Add all the Train items
        items.add(new TrainItem("Swimsuit / Swim trunks"));
        items.add(new TrainItem("Swim cap"));
        items.add(new TrainItem("Goggles (anti-fog, UV protection)"));
        items.add(new TrainItem("Towel (quick-dry or regular)"));
        items.add(new TrainItem("Flip-flops or pool slides"));
        items.add(new TrainItem("Kickboard"));

        return items;
    }

    private void saveTrainItemToFirebase(TrainItem trainItem, String userId, String tripId) {
        DatabaseReference trainRef = FirebaseDatabase.getInstance().getReference("userTrips")
                .child(userId)
                .child(tripId)
                .child("trainItems");

        String itemId = trainRef.push().getKey(); // Get a unique key for the item
        if (itemId != null) {
            trainRef.child(itemId).setValue(trainItem).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(TrainItemList.this, "Item saved successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(TrainItemList.this, "Failed to save item", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void getTrainItemsFromFirebase(String userId, String tripId) {
        DatabaseReference trainRef = FirebaseDatabase.getInstance().getReference("userTrips")
                .child(userId)
                .child(tripId)
                .child("trainItems");

        trainRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                allItems.clear();  // Clear the list before adding new items
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    TrainItem trainItem = snapshot.getValue(TrainItem.class);
                    if (trainItem != null) {
                        allItems.add(trainItem);
                    }
                }
                adapter.notifyDataSetChanged(); // Notify adapter to update RecyclerView
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseError", "Error retrieving Train items", databaseError.toException());
            }
        });
    }
}
