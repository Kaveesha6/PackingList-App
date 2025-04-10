package com.example.packinglistapp;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Categories extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CategoryAdapter adapter;
    private List<CategoryItem> categoryList;
    private boolean isDeleteMode = false;
    private Button btnDeleteCategory;
    private Button btnAddCategory;
    private Button btnFinished;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        recyclerView = findViewById(R.id.categoryRecyclerView);
        btnDeleteCategory = findViewById(R.id.btnDeleteCategory);
        btnAddCategory = findViewById(R.id.btnAddCategory);
        btnFinished = findViewById(R.id.btnFinished);

        // Initialize category list
        categoryList = new ArrayList<>();
        initializeCategoryList();
        loadCustomCategories(); // Load any saved custom categories

        // Set up RecyclerView
        int numberOfColumns = 3;
        GridLayoutManager layoutManager = new GridLayoutManager(this, numberOfColumns);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                // Headers take all columns, items take 1
                if (position < categoryList.size() &&
                        categoryList.get(position).getType() == CategoryItem.TYPE_HEADER) {
                    return numberOfColumns;
                }
                return 1;
            }
        });
        recyclerView.setLayoutManager(layoutManager);

        // Initialize adapter with click listener
        adapter = new CategoryAdapter(categoryList, item -> {
            if (isDeleteMode) {
                // Delete mode logic
                if (item.getType() != CategoryItem.TYPE_HEADER) {
                    // Check if it's a default category that shouldn't be deleted
                    if (isDefaultCategory(item.getName())) {
                        Toast.makeText(Categories.this,
                                "Cannot delete default category", Toast.LENGTH_SHORT).show();
                    } else {
                        // Show confirmation dialog for deletion
                        confirmDelete(item);
                    }
                }
            } else {
                // Open category logic (unchanged)
                if (item.getType() != CategoryItem.TYPE_HEADER) {
                    Intent intent;

                    // Switch case for predefined categories
                    switch (item.getName()) {
                        case "Hotel":
                            intent = new Intent(Categories.this, HotelItemsList.class);
                            break;
                        case "Rental":
                            intent = new Intent(Categories.this, RentalItemList.class);
                            break;
                        case "Family / Friends":
                            intent = new Intent(Categories.this, FamilyFrendsItemList.class);
                            break;
                        case "Second Home":
                            intent = new Intent(Categories.this, SecondHomeItemList.class);
                            break;
                        case "Camping":
                            intent = new Intent(Categories.this, CampPackingList.class);
                            break;
                        case "Cruise":
                            intent = new Intent(Categories.this, CruiseItemList.class);
                            break;
                        case "Airplane":
                            intent = new Intent(Categories.this, AirplaneItemList.class);
                            break;
                        case "Car":
                            intent = new Intent(Categories.this, CarItemList.class);
                            break;
                        case "Train":
                            intent = new Intent(Categories.this, TrainItemList.class);
                            break;
                        case "Motorcycle":
                            intent = new Intent(Categories.this, MotorcycleItemList.class);
                            break;
                        case "Boat":
                            intent = new Intent(Categories.this, BoatItemList.class);
                            break;
                        case "Bus":
                            intent = new Intent(Categories.this, BusItemList.class);
                            break;
                        case "Essentials":
                            intent = new Intent(Categories.this, EssentialsItemList.class);
                            break;
                        case "Clothes":
                            intent = new Intent(Categories.this, ClothesItemList.class);
                            break;
                        case "International":
                            intent = new Intent(Categories.this, InternationalItemList.class);
                            break;
                        case "Work":
                            intent = new Intent(Categories.this, WorkItemList.class);
                            break;
                        case "Personal Care":
                            intent = new Intent(Categories.this, PersonalCareItemList.class);
                            break;
                        case "Beach":
                            intent = new Intent(Categories.this, BeachPackingList.class);
                            break;
                        case "Swimming":
                            intent = new Intent(Categories.this, SwimPackingList.class);
                            break;
                        case "Photography":
                            intent = new Intent(Categories.this, PhotographItemList.class);
                            break;
                        case "Snow sports":
                            intent = new Intent(Categories.this, SnowSportsItemList.class);
                            break;
                        case "Fitness":
                            intent = new Intent(Categories.this, FitnessItemList.class);
                            break;
                        case "Hiking":
                            intent = new Intent(Categories.this, HikePackingList.class);
                            break;
                        case "Business Meals":
                            intent = new Intent(Categories.this, BusinessMealsItemList.class);
                            break;
                        case "Todo List":
                            intent = new Intent(Categories.this, ToDoListItemList.class);
                            break;
                        case "Baby":
                            intent = new Intent(Categories.this, BabyNeedsPackingList.class);
                            break;
                        case "Budget Calculator":
                            intent = new Intent(Categories.this, BudgetCalculatorActivity.class);
                            break;
                        default:
                            // Handle custom categories
                            intent = new Intent(Categories.this, NewGenericItemList.class);
                            intent.putExtra("CATEGORY_NAME", item.getName());
                            break;
                    }
                    startActivity(intent);
                }
            }
        });

        recyclerView.setAdapter(adapter);

        // Add category button
        btnAddCategory.setOnClickListener(v -> showAddCategoryDialog());

        // Delete category button
        btnDeleteCategory.setOnClickListener(v -> {
            isDeleteMode = !isDeleteMode;
            if (isDeleteMode) {
                btnDeleteCategory.setText("CANCEL DELETE");
                Toast.makeText(Categories.this, "Select a category to delete", Toast.LENGTH_SHORT).show();
            } else {
                btnDeleteCategory.setText("DELETE CATEGORY");
            }
        });

        btnFinished.setOnClickListener(v -> {
            // 1. Collect all selected items from all categories
            Map<String, List<PackingItems>> selectedItemsByCategory = new HashMap<>();

            // We need to iterate through each category and collect checked items
            // This assumes you have a method to get all categories with their items
            List<CategoryWithItems> categoriesWithItems = getAllCategoriesWithItems();

            for (CategoryWithItems category : categoriesWithItems) {
                List<PackingItems> selectedItems = new ArrayList<>();
                for (PackingItems item : category.getItems()) {
                    if (item.isChecked()) {
                        selectedItems.add(item);
                    }
                }
                if (!selectedItems.isEmpty()) {
                    selectedItemsByCategory.put(category.getName(), selectedItems);
                }
            }

            // 2. Create intent to go to Home with the selected items
            Intent homeIntent = new Intent(Categories.this, Home.class);

            // Convert the Map to Bundle using Serializable
            Bundle bundle = new Bundle();
            bundle.putSerializable("selected_items", (Serializable) selectedItemsByCategory);
            homeIntent.putExtras(bundle);

            // 3. (Optional) Save to Firebase before navigating
            saveToFirebase(selectedItemsByCategory);

            // 4. Navigate to Home
            startActivity(homeIntent);
            finish();
        });
    }

    // Helper method to save to Firebase
    private void saveToFirebase(Map<String, List<PackingItems>> selectedItems) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(user.getUid())
                    .child("selected_items");

            userRef.setValue(selectedItems)
                    .addOnSuccessListener(aVoid -> Log.d("Firebase", "Data saved successfully"))
                    .addOnFailureListener(e -> Log.e("Firebase", "Failed to save data", e));
        }
    }

    // Helper method to get all categories with their items
    // This is just a placeholder method - you'll need to implement this based on your app structure
    private List<CategoryWithItems> getAllCategoriesWithItems() {
        List<CategoryWithItems> result = new ArrayList<>();
        // Implement your logic to get all categories with their items
        // This might involve querying your database or other data sources
        return result;
    }

    // Check if category is a default one that shouldn't be deleted
    private boolean isDefaultCategory(String categoryName) {
        String[] defaultCategories = {
                "Hotel", "Rental", "Family / Friends", "Second Home", "Camping", "Cruise",
                "Airplane", "Car", "Train", "Motorcycle", "Boat", "Bus",
                "Essentials", "Clothes", "International", "Work", "Personal Care",
                "Beach", "Swimming", "Photography", "Snow sports", "Fitness",
                "Hiking", "Business Meals", "Todo List", "Baby", "Budget Calculator"
        };

        for (String category : defaultCategories) {
            if (category.equals(categoryName)) {
                return true;
            }
        }
        return false;
    }

    // Show confirmation dialog before deleting
    private void confirmDelete(CategoryItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Category");
        builder.setMessage("Are you sure you want to delete \"" + item.getName() + "\"?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            deleteCategory(item);
        });
        builder.setNegativeButton("No", null);
        builder.show();
    }

    // Delete the category from the list
    private void deleteCategory(CategoryItem item) {
        int position = categoryList.indexOf(item);
        if (position != -1) {
            categoryList.remove(position);
            adapter.notifyItemRemoved(position);
            saveCustomCategories(); // Save changes
            Toast.makeText(this, "Category deleted", Toast.LENGTH_SHORT).show();

            // Exit delete mode after successful deletion
            isDeleteMode = false;
            btnDeleteCategory.setText("DELETE CATEGORY");
        }
    }

    // Save custom categories to SharedPreferences
    private void saveCustomCategories() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference categoriesRef = FirebaseDatabase.getInstance().getReference("users")
                    .child(user.getUid())
                    .child("categories");

            for (CategoryItem item : categoryList) {
                if (item.getType() != CategoryItem.TYPE_HEADER && !isDefaultCategory(item.getName())) {
                    String categoryId = categoriesRef.push().getKey(); // Create a new unique ID for the category
                    Category category = new Category(item.getName(), item.getImageResourceName(), new ArrayList<>());
                    categoriesRef.child(categoryId).setValue(category);
                }
            }
        }
    }


    // Get the section name for a category
    private String getSectionForCategory(CategoryItem item) {
        int index = categoryList.indexOf(item);
        if (index == -1) return "Activities / Items"; // Default section

        // Search backward for the nearest header
        for (int i = index - 1; i >= 0; i--) {
            if (categoryList.get(i).getType() == CategoryItem.TYPE_HEADER) {
                return categoryList.get(i).getName();
            }
        }

        return "Activities / Items"; // Default if no header found
    }

    // Load custom categories from SharedPreferences
    private void loadCustomCategories() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference categoriesRef = FirebaseDatabase.getInstance().getReference("users")
                    .child(user.getUid())
                    .child("categories");

            categoriesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    categoryList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Category category = snapshot.getValue(Category.class);
                        categoryList.add(new CategoryItem(category.getName(), category.getImage(), CategoryItem.TYPE_ITEM));
                    }
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("Firebase", "Error loading categories", databaseError.toException());
                }
            });
        }
    }

    private void initializeCategoryList() {
        // Add Accommodation section
        categoryList.add(new CategoryItem("Accommodation", "", CategoryItem.TYPE_HEADER));
        categoryList.add(new CategoryItem("Hotel", "hotel_image", CategoryItem.TYPE_ITEM));
        categoryList.add(new CategoryItem("Rental", "rental_image", CategoryItem.TYPE_ITEM));
        categoryList.add(new CategoryItem("Family / Friends", "family_friends_image", CategoryItem.TYPE_ITEM));
        categoryList.add(new CategoryItem("Second Home", "second_home_image", CategoryItem.TYPE_ITEM));
        categoryList.add(new CategoryItem("Camping", "camping_image", CategoryItem.TYPE_ITEM));
        categoryList.add(new CategoryItem("Cruise", "cruise_image", CategoryItem.TYPE_ITEM));

        // Add Transportation section
        categoryList.add(new CategoryItem("Transportation", "", CategoryItem.TYPE_HEADER));
        categoryList.add(new CategoryItem("Airplane", "airplane_image", CategoryItem.TYPE_ITEM));
        categoryList.add(new CategoryItem("Car", "car_image", CategoryItem.TYPE_ITEM));
        categoryList.add(new CategoryItem("Train", "train_image", CategoryItem.TYPE_ITEM));
        categoryList.add(new CategoryItem("Motorcycle", "motorcycle_image", CategoryItem.TYPE_ITEM));
        categoryList.add(new CategoryItem("Boat", "boat_image", CategoryItem.TYPE_ITEM));
        categoryList.add(new CategoryItem("Bus", "bus_image", CategoryItem.TYPE_ITEM));

        // Add Activities/Items section
        categoryList.add(new CategoryItem("Activities / Items", "", CategoryItem.TYPE_HEADER));
        categoryList.add(new CategoryItem("Essentials", "essentials_image", CategoryItem.TYPE_ITEM));
        categoryList.add(new CategoryItem("Clothes", "clothes_image", CategoryItem.TYPE_ITEM));
        categoryList.add(new CategoryItem("International", "international_image", CategoryItem.TYPE_ITEM));
        categoryList.add(new CategoryItem("Work", "work_image", CategoryItem.TYPE_ITEM));
        categoryList.add(new CategoryItem("Personal Care", "personal_care_image", CategoryItem.TYPE_ITEM));
        categoryList.add(new CategoryItem("Beach", "beach_image", CategoryItem.TYPE_ITEM));
        categoryList.add(new CategoryItem("Swimming", "swimming_image", CategoryItem.TYPE_ITEM));
        categoryList.add(new CategoryItem("Photography", "photography_image", CategoryItem.TYPE_ITEM));
        categoryList.add(new CategoryItem("Snow sports", "snow_sports_image", CategoryItem.TYPE_ITEM));
        categoryList.add(new CategoryItem("Fitness", "fitness_image", CategoryItem.TYPE_ITEM));
        categoryList.add(new CategoryItem("Hiking", "hiking_image", CategoryItem.TYPE_ITEM));
        categoryList.add(new CategoryItem("Business Meals", "business_meals_image", CategoryItem.TYPE_ITEM));

        // Add Family section
        categoryList.add(new CategoryItem("Family", "", CategoryItem.TYPE_HEADER));
        categoryList.add(new CategoryItem("Todo List", "todo_list_image", CategoryItem.TYPE_ITEM));
        categoryList.add(new CategoryItem("Baby", "baby_image", CategoryItem.TYPE_ITEM));

        //Budget Calculator
        categoryList.add(new CategoryItem("Budget Calculator","",CategoryItem.TYPE_HEADER));
        categoryList.add(new CategoryItem("Budget Calculator", "calculator_image",CategoryItem.TYPE_ITEM));

    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_category, null);
        builder.setView(dialogView);

        EditText editCategoryName = dialogView.findViewById(R.id.editCategoryName);
        Spinner spinnerSectionType = dialogView.findViewById(R.id.spinnerSectionType);

        // Set up spinner with section options
        String[] sections = {"Accommodation", "Transportation", "Activities / Items", "Family"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, sections);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSectionType.setAdapter(spinnerAdapter);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String categoryName = editCategoryName.getText().toString().trim();
            String selectedSection = spinnerSectionType.getSelectedItem().toString();

            if (!categoryName.isEmpty()) {
                // Find the position to insert the new category
                int insertPosition = findInsertPosition(selectedSection);

                if (insertPosition != -1) {
                    CategoryItem newCategory = new CategoryItem(categoryName, "default_image", CategoryItem.TYPE_ITEM);
                    categoryList.add(insertPosition, newCategory);
                    adapter.notifyItemInserted(insertPosition);
                    Toast.makeText(Categories.this, "Category added: " + categoryName, Toast.LENGTH_SHORT).show();

                    // Save the updated list
                    saveCustomCategories();
                }
            } else {
                Toast.makeText(Categories.this, "Please enter a category name", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private int findInsertPosition(String sectionName) {
        boolean foundSection = false;
        int position = -1;

        for (int i = 0; i < categoryList.size(); i++) {
            CategoryItem item = categoryList.get(i);

            if (item.getType() == CategoryItem.TYPE_HEADER && item.getName().equals(sectionName)) {
                foundSection = true;
            } else if (foundSection &&
                    (item.getType() == CategoryItem.TYPE_HEADER || i == categoryList.size() - 1)) {
                // If we found our section and now reached the next header or end of list
                // Insert before the next header or at the end
                position = i;
                break;
            }
        }

        // If we didn't find a suitable position but found the section, add to the end
        if (position == -1 && foundSection) {
            position = categoryList.size();
        }

        return position;
    }

    // This would be placed in your Calendar or category selection activity
    private void saveTrip(Trip trip, List<PackingItems> selectedItems, String category) {
        // Save trip to Firebase
        DatabaseReference tripsRef = FirebaseDatabase.getInstance().getReference("trips");
        String tripId = tripsRef.push().getKey();
        trip.setId(tripId);

        tripsRef.child(tripId).setValue(trip);

        // Save packing list
        DatabaseReference packingListsRef = FirebaseDatabase.getInstance().getReference("packingLists");
        String packingListId = packingListsRef.push().getKey();

        PackingList packingList = new PackingList();
        packingList.setId(packingListId);
        packingList.setTripId(tripId);
        packingList.setCategoryName(category);
        packingList.setItems(selectedItems);

        packingListsRef.child(packingListId).setValue(packingList);
    }

    // Helper class to store categories with their items
    private static class CategoryWithItems {
        private String name;
        private List<PackingItems> items;

        public CategoryWithItems(String name, List<PackingItems> items) {
            this.name = name;
            this.items = items;
        }

        public String getName() {
            return name;
        }

        public List<PackingItems> getItems() {
            return items;
        }
    }
}