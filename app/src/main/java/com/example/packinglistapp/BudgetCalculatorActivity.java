package com.example.packinglistapp;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BudgetCalculatorActivity extends AppCompatActivity {

    // UI Components
    private Spinner currencySpinner;
    private EditText editTotalBudget;
    private TextView textRemainingBudget, textUnpurchasedAmount, textSpentAmount;
    private TextView currencySymbolBudget, currencySymbolRemaining, currencySymbolUnpurchased, currencySymbolSpent;
    private EditText editItemName, editQuantity, editPrice;
    private AutoCompleteTextView categoryDropdown;
    private Button btnAddItem;
    private Spinner filterCategorySpinner;
    private RecyclerView recyclerViewItems, recyclerViewCategories;

    // Data
    private double totalBudget = 0.0;
    private double spentAmount = 0.0;
    private double unpurchasedAmount = 0.0;
    private double remainingBudget = 0.0;
    private String currencySymbol = "$";
    private List<PackingItem> itemsList = new ArrayList<>();
    private List<CategoryBreakdown> categoryBreakdowns = new ArrayList<>();
    private List<String> categories = new ArrayList<>(Arrays.asList(
            "All Categories", "Essentials", "Clothing", "Electronics", "Toiletries", "Food", "Misc"));
    private final DecimalFormat decimalFormat = new DecimalFormat("0.00");

    // Adapters
    private ItemsAdapter itemsAdapter;
    private CategoryBreakdownAdapter categoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_calculator);

        // Initialize UI components
        initViews();
        setupCurrencySpinner();
        setupCategoryDropdown();
        setupFilterSpinner();
        setupRecyclerViews();
        setupListeners();

        // Retrieve saved data from Firebase
        retrieveBudgetDataFromFirebase();
    }

    private void initViews() {
        currencySpinner = findViewById(R.id.currencySpinner);
        editTotalBudget = findViewById(R.id.editTotalBudget);
        textRemainingBudget = findViewById(R.id.textRemainingBudget);
        textUnpurchasedAmount = findViewById(R.id.textUnpurchasedAmount);
        textSpentAmount = findViewById(R.id.textSpentAmount);

        currencySymbolBudget = findViewById(R.id.currencySymbolBudget);
        currencySymbolRemaining = findViewById(R.id.currencySymbolRemaining);
        currencySymbolUnpurchased = findViewById(R.id.currencySymbolUnpurchased);
        currencySymbolSpent = findViewById(R.id.currencySymbolSpent);

        editItemName = findViewById(R.id.editItemName);
        editQuantity = findViewById(R.id.editQuantity);
        editPrice = findViewById(R.id.editPrice);
        categoryDropdown = findViewById(R.id.categoryDropdown);
        btnAddItem = findViewById(R.id.btnAddItem);
        filterCategorySpinner = findViewById(R.id.filterCategorySpinner);
        recyclerViewItems = findViewById(R.id.recyclerViewItems);
        recyclerViewCategories = findViewById(R.id.recyclerViewCategories);
    }

    private void setupCurrencySpinner() {
        List<String> currencies = new ArrayList<>(Arrays.asList(
                "USD ($)", "EUR (€)", "GBP (£)", "JPY (¥)", "INR (₹)", "CAD (C$)", "AUD (A$)"
        ));

        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, currencies);
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currencySpinner.setAdapter(currencyAdapter);

        currencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = currencies.get(position);
                currencySymbol = selected.substring(selected.indexOf("(") + 1, selected.indexOf(")"));
                updateCurrencySymbols();
                updateItemsAdapter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updateCurrencySymbols() {
        currencySymbolBudget.setText(currencySymbol);
        currencySymbolRemaining.setText(currencySymbol);
        currencySymbolUnpurchased.setText(currencySymbol);
        currencySymbolSpent.setText(currencySymbol);
    }

    private void setupCategoryDropdown() {
        List<String> itemCategories = new ArrayList<>(categories);
        itemCategories.remove(0);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, itemCategories);
        categoryDropdown.setAdapter(adapter);
    }

    private void setupFilterSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterCategorySpinner.setAdapter(adapter);

        filterCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCategory = categories.get(position);  // Get the selected category
                filterItems(selectedCategory);  // Filter items based on the selected category
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupRecyclerViews() {
        recyclerViewItems.setLayoutManager(new LinearLayoutManager(this));
        itemsAdapter = new ItemsAdapter(itemsList, new ItemsAdapter.OnItemActionListener() {
            @Override
            public void onStatusChange(PackingItem item) {
                toggleItemStatus(item);
            }

            @Override
            public void onRemove(PackingItem item) {
                removeItem(item);
            }
        }, currencySymbol);
        recyclerViewItems.setAdapter(itemsAdapter);

        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(this));
        categoryAdapter = new CategoryBreakdownAdapter(categoryBreakdowns, currencySymbol);
        recyclerViewCategories.setAdapter(categoryAdapter);
    }

    private void setupListeners() {
        btnAddItem.setOnClickListener(v -> addNewItem());

        editTotalBudget.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                updateBudget();
            }
        });
    }

    private void updateBudget() {
        try {
            totalBudget = Double.parseDouble(editTotalBudget.getText().toString());
        } catch (NumberFormatException e) {
            totalBudget = 0.0;
        }

        calculateTotals();
        updateUI();
        saveBudgetDataToFirebase();
    }

    private void addNewItem() {
        String name = editItemName.getText().toString().trim();
        String category = categoryDropdown.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter an item name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (category.isEmpty()) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }

        int quantity = 1;
        try {
            quantity = Integer.parseInt(editQuantity.getText().toString());
        } catch (NumberFormatException e) {}

        double price = 0.0;
        try {
            price = Double.parseDouble(editPrice.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid price", Toast.LENGTH_SHORT).show();
            return;
        }

        PackingItem newItem = new PackingItem(name, category, quantity, price, false);
        itemsList.add(newItem);

        calculateTotals();
        updateCategoryBreakdowns();
        updateUI();

        editItemName.setText("");
        editPrice.setText("");
        editQuantity.setText("1");
        categoryDropdown.setText("");

        Toast.makeText(this, "Item added successfully", Toast.LENGTH_SHORT).show();
    }

    private void toggleItemStatus(PackingItem item) {
        item.setPurchased(!item.isPurchased());
        calculateTotals();
        updateCategoryBreakdowns();
        updateUI();
    }

    private void removeItem(PackingItem item) {
        itemsList.remove(item);
        calculateTotals();
        updateCategoryBreakdowns();
        updateUI();
    }

    private void calculateTotals() {
        spentAmount = 0.0;
        unpurchasedAmount = 0.0;

        for (PackingItem item : itemsList) {
            double itemTotal = item.getPrice() * item.getQuantity();

            if (item.isPurchased()) {
                spentAmount += itemTotal;
            } else {
                unpurchasedAmount += itemTotal;
            }
        }

        remainingBudget = totalBudget - spentAmount;
    }

    private void updateCategoryBreakdowns() {
        Map<String, Double> categoryTotals = new HashMap<>();
        double grandTotal = spentAmount + unpurchasedAmount;

        for (PackingItem item : itemsList) {
            String category = item.getCategory();
            double itemTotal = item.getPrice() * item.getQuantity();

            categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + itemTotal);
        }

        categoryBreakdowns.clear();
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            double percentage = grandTotal > 0 ? (entry.getValue() / grandTotal) * 100 : 0;
            categoryBreakdowns.add(new CategoryBreakdown(entry.getKey(), entry.getValue(), percentage));
        }

        categoryAdapter.notifyDataSetChanged();
    }

    private void updateUI() {
        textRemainingBudget.setText(decimalFormat.format(remainingBudget));
        textUnpurchasedAmount.setText(decimalFormat.format(unpurchasedAmount));
        textSpentAmount.setText(decimalFormat.format(spentAmount));

        itemsAdapter.notifyDataSetChanged();
        categoryAdapter.notifyDataSetChanged();
    }

    private void updateItemsAdapter() {
        itemsAdapter.setCurrencySymbol(currencySymbol);
        itemsAdapter.notifyDataSetChanged();
        categoryAdapter.setCurrencySymbol(currencySymbol);
        categoryAdapter.notifyDataSetChanged();
    }

    // Save Budget Data to Firebase
    private void saveBudgetDataToFirebase() {
        String userId = "exampleUserId";  // Replace with actual user ID
        String tripId = "exampleTripId";  // Replace with actual trip ID

        DatabaseReference budgetRef = FirebaseDatabase.getInstance().getReference("userTrips")
                .child(userId)
                .child(tripId)
                .child("budgetData");

        Map<String, Object> budgetData = new HashMap<>();
        budgetData.put("totalBudget", totalBudget);
        budgetData.put("spentAmount", spentAmount);
        budgetData.put("unpurchasedAmount", unpurchasedAmount);
        budgetData.put("remainingBudget", remainingBudget);

        budgetRef.setValue(budgetData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("BudgetCalculator", "Budget data saved successfully.");
            } else {
                Log.e("BudgetCalculator", "Failed to save budget data.");
            }
        });
    }

    // Retrieve Budget Data from Firebase
    private void retrieveBudgetDataFromFirebase() {
        String userId = "exampleUserId";  // Replace with actual user ID
        String tripId = "exampleTripId";  // Replace with actual trip ID

        DatabaseReference budgetRef = FirebaseDatabase.getInstance().getReference("userTrips")
                .child(userId)
                .child(tripId)
                .child("budgetData");

        budgetRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    totalBudget = dataSnapshot.child("totalBudget").getValue(Double.class);
                    spentAmount = dataSnapshot.child("spentAmount").getValue(Double.class);
                    unpurchasedAmount = dataSnapshot.child("unpurchasedAmount").getValue(Double.class);
                    remainingBudget = dataSnapshot.child("remainingBudget").getValue(Double.class);

                    calculateTotals();
                    updateUI();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("BudgetCalculator", "Failed to retrieve budget data", databaseError.toException());
            }
        });
    }

    // Method for filtering items based on selected category
    private void filterItems(String category) {
        // If "All Categories" is selected, display all items
        if ("All Categories".equals(category)) {
            itemsAdapter.setItems(itemsList);  // Show all items
        } else {
            List<PackingItem> filteredItems = new ArrayList<>();
            // Filter items by category
            for (PackingItem item : itemsList) {
                if (item.getCategory().equalsIgnoreCase(category)) {
                    filteredItems.add(item);
                }
            }
            itemsAdapter.setItems(filteredItems);  // Update the adapter with filtered items
        }
        itemsAdapter.notifyDataSetChanged();  // Notify adapter about the change
    }

}
