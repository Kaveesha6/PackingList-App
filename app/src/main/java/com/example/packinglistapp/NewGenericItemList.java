package com.example.packinglistapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

public class NewGenericItemList extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NewItemAdapter adapter;
    private List<NewPackingItem> itemList;
    private TextView categoryTitle;
    private EditText newItemInput;
    private Button addButton;
    private Button btnAddToCheckList; // Added button for checklist functionality
    private String categoryName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_item_list);

        // Get category name from intent
        categoryName = getIntent().getStringExtra("CATEGORY_NAME");
        if (categoryName == null) {
            categoryName = "Custom List";
        }

        // Initialize UI components
        categoryTitle = findViewById(R.id.categoryTitle);
        categoryTitle.setText(categoryName + " Packing List");

        newItemInput = findViewById(R.id.newItemInput);
        addButton = findViewById(R.id.btnAddItem);
        btnAddToCheckList = findViewById(R.id.btnAddToCheckList); // Initialize the Add to Checklist button
        recyclerView = findViewById(R.id.itemsRecyclerView);

        // Initialize item list
        itemList = new ArrayList<>();

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NewItemAdapter(itemList);
        recyclerView.setAdapter(adapter);

        // Set up Add button
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String itemText = newItemInput.getText().toString().trim();
                if (!itemText.isEmpty()) {
                    addItem(itemText);
                    newItemInput.setText("");
                } else {
                    Toast.makeText(NewGenericItemList.this, "Please enter an item", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set up Add to Checklist button
        btnAddToCheckList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Count selected items
                int selectedCount = 0;
                for (NewPackingItem item : itemList) {
                    if (item.isChecked()) {
                        selectedCount++;
                    }
                }

                if (selectedCount > 0) {
                    // Here you would add the selected items to the checklist
                    // This could involve saving to SharedPreferences or a database

                    // For now, just saving the selection state and showing a toast
                    saveItems();
                    Toast.makeText(NewGenericItemList.this, selectedCount + " items added to checklist", Toast.LENGTH_SHORT).show();

                    // Optionally, clear selections after adding to checklist
                    for (NewPackingItem item : itemList) {
                        if (item.isChecked()) {
                            item.setChecked(false);
                        }
                    }
                    adapter.notifyDataSetChanged();

                    // Save the updated state with cleared selections
                    saveItems();
                } else {
                    Toast.makeText(NewGenericItemList.this, "Please select items to add to checklist", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Back button
        findViewById(R.id.backButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Load existing items
        loadItems();
    }

    private void addItem(String itemName) {
        NewPackingItem newItem = new NewPackingItem(itemName, 1);
        itemList.add(newItem);
        adapter.notifyItemInserted(itemList.size() - 1);
        Toast.makeText(this, "Added: " + itemName, Toast.LENGTH_SHORT).show();

        // Save items to storage
        saveItems();
    }

    private void saveItems() {
        SharedPreferences prefs = getSharedPreferences("CategoryItems", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Create a string representation of items
        StringBuilder itemsData = new StringBuilder();
        for (NewPackingItem item : itemList) {
            itemsData.append(item.getName()).append(";")
                    .append(item.getQuantity()).append(";")
                    .append(item.isChecked()).append(",");
        }

        // Save the data with the category name as key
        editor.putString(categoryName, itemsData.toString());
        editor.apply();
    }

    private void loadItems() {
        SharedPreferences prefs = getSharedPreferences("CategoryItems", MODE_PRIVATE);
        String itemsData = prefs.getString(categoryName, "");

        if (!itemsData.isEmpty()) {
            String[] items = itemsData.split(",");
            for (String item : items) {
                if (!item.isEmpty()) {
                    String[] parts = item.split(";");
                    if (parts.length >= 3) {
                        String name = parts[0];
                        int quantity = Integer.parseInt(parts[1]);
                        boolean checked = Boolean.parseBoolean(parts[2]);

                        NewPackingItem packingItem = new NewPackingItem(name, quantity);
                        packingItem.setChecked(checked);
                        itemList.add(packingItem);
                    }
                }
            }
            adapter.notifyDataSetChanged();
        }
    }

    // If you want to save selected items to a separate checklist, you could implement this method
    private void saveToChecklist() {
        SharedPreferences prefs = getSharedPreferences("Checklist", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        StringBuilder checklistItems = new StringBuilder();
        for (NewPackingItem item : itemList) {
            if (item.isChecked()) {
                checklistItems.append(item.getName()).append(";")
                        .append(item.getQuantity()).append(",");
            }
        }

        // Append to existing checklist items
        String existingItems = prefs.getString("checklist_items", "");
        if (!existingItems.isEmpty()) {
            checklistItems.insert(0, existingItems);
        }

        editor.putString("checklist_items", checklistItems.toString());
        editor.apply();
    }
}