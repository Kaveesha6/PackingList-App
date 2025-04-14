package com.example.packinglistapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
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
    public String tripId;
    public String itemListType;
    private TextView itemListHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_items_list);

        itemListHeader = findViewById(R.id.tvTitle);

        Intent intent = getIntent();
        tripId = intent.getStringExtra("tripId");
        itemListType = intent.getStringExtra("itemListType");
        switch (itemListType) {
            case "hotelItems":
                itemListHeader.setText("Hotel Item List");
                break;
            case "rentalItems":
                itemListHeader.setText("Rental Item List");
                break;
            case "familyItems":
                itemListHeader.setText("Family / Friends Item List");
                break;
            case "secondHomeItems":
                itemListHeader.setText("Second Home Item List");
                break;
            case "campingItems":
                itemListHeader.setText("Camping Item List");
                break;
            case "cruiseItems":
                itemListHeader.setText("Cruise Item List");
                break;
            case "airplaneItems":
                itemListHeader.setText("Airplane Item List");
                break;
            case "carItems":
                itemListHeader.setText("Car Item List");
                break;
            case "trainItems":
                itemListHeader.setText("Train Item List");
                break;
            case "motorcycleItems":
                itemListHeader.setText("Motorcycle Item List");
                break;
            case "boatItems":
                itemListHeader.setText("Boat Item List");
                break;
            case "busItems":
                itemListHeader.setText("Bus Item List");
                break;
            case "essentialsItems":
                itemListHeader.setText("Essentials Item List");
                break;
            case "clothesItems":
                itemListHeader.setText("Clothes Item List");
                break;
            case "internationalItems":
                itemListHeader.setText("International Item List");
                break;
            case "workItems":
                itemListHeader.setText("Work Item List");
                break;
            case "personalCareItems":
                itemListHeader.setText("Personal Care Item List");
                break;
            case "beachItems":
                itemListHeader.setText("Beach Item List");
                break;
            case "swimmingItems":
                itemListHeader.setText("Swimming Item List");
                break;
            case "photographyItems":
                itemListHeader.setText("Photography Item List");
                break;
            case "snowSportsItems":
                itemListHeader.setText("Snow Sports Item List");
                break;
            case "fitnessItems":
                itemListHeader.setText("Fitness Item List");
                break;
            case "hikingItems":
                itemListHeader.setText("Hiking Item List");
                break;
            case "businessMealsItems":
                itemListHeader.setText("Business Meals Item List");
                break;
            case "todoItems":
                itemListHeader.setText("Todo List Items");
                break;
            case "babyItems":
                itemListHeader.setText("Baby Item List");
                break;
            case "budgetCalculator":
                itemListHeader.setText("Budget Calculator");
                break;
            default:
                itemListHeader.setText("Packing List");
                break;
        }

        // Initialize UI components
        initializeViews();

        // Set up the RecyclerView with adapter
        setupRecyclerView();

        // Set up click listeners for buttons
        setupClickListeners();

        getHotelItemsFromFirebase();
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
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();  // Replace with actual user ID

        // Save the hotel item to Firebase
        DatabaseReference hotelRef = FirebaseDatabase.getInstance().getReference("userTrips")
                .child(userId)
                .child(tripId)
                .child(itemListType);

        String itemId = hotelRef.push().getKey(); // Get a unique key for the item
        if (itemId != null) {
            hotelRef.child(itemId).setValue(hotelItem).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    updateItemQuantity();
                    Toast.makeText(HotelItemsList.this, "Hotel item saved successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(HotelItemsList.this, "Failed to save item", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void getHotelItemsFromFirebase() {
        // Retrieve hotel items from Firebase
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference hotelRef = FirebaseDatabase.getInstance().getReference("userTrips")
                .child(userId)
                .child(tripId)
                .child(itemListType);

        hotelRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                allItems.clear();  // Clear the list before adding new items
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    String name = snapshot.child("name").getValue(String.class);
                    boolean checked = snapshot.child("checked").getValue(Boolean.class);
                    int quantity = snapshot.child("quantity").getValue(Integer.class);
                    HotelItem hotelItem = new HotelItem(name);
                    hotelItem.setChecked(checked);
                    hotelItem.setQuantity(quantity);
                    hotelItem.setHotelItemId(snapshot.getKey());
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

    private void updateItemQuantity() {
        // Get the current user's ID (you'll need to replace this with actual user ID)
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();  // Replace with actual user ID
        final int[] quantity = new int[1];

        // Save the hotel item to Firebase
        DatabaseReference hotelRef = FirebaseDatabase.getInstance().getReference("userTrips")
                .child(userId)
                .child(tripId);

        hotelRef.child("itemCount").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                quantity[0] = snapshot.getValue(Integer.class);
                quantity[0] += 1;

                hotelRef.child("itemCount").setValue(quantity[0]);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
