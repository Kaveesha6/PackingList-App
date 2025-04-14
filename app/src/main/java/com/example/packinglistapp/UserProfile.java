package com.example.packinglistapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class UserProfile extends AppCompatActivity {

    private ImageView profileImage;
    private TextView noOfLists;
    private TextInputEditText fullNameEditText, emailEditText, phoneEditText, passwordEditText;
    private Uri selectedImageUri;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private SharedPreferences preferences;
    private DatabaseReference database;
    private FirebaseUser currentUser;
    private User user;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI elements
        profileImage = findViewById(R.id.profile_image);
        TextView fullNameTextView = findViewById(R.id.full_name);
        noOfLists = findViewById(R.id.no_of_lists);
        fullNameEditText = findViewById(R.id.full_name_edit);
        emailEditText = findViewById(R.id.email_edit);
        phoneEditText = findViewById(R.id.phone_edit);
        Button btnUpdate = findViewById(R.id.update);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance().getReference("userTrips");
        fullNameTextView.setText(currentUser.getDisplayName());

        // Set up ActivityResultLauncher for gallery
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            // Display the image
                            profileImage.setImageURI(selectedImageUri);

                            // Save the image URI to SharedPreferences
                            PreferenceManager.saveString(this, "profile_image_uri", selectedImageUri.toString());
                        }
                    }
                }
        );

        String imageUriString = "";
        if (!imageUriString.isEmpty()) {
            Uri imageUri = Uri.parse(imageUriString);
            profileImage.setImageURI(imageUri);
        }

        // Load saved data
        loadUserData();
        updatePackingListCount();

        // Set up click listener for profile image
//        profileImage.setOnClickListener(v -> showImageSelectionDialog());

        // Set up update button click listener
        btnUpdate.setOnClickListener(v -> {
            saveUserData();
            Toast.makeText(UserProfile.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(UserProfile.this, Home.class);
            startActivity(intent);
            finish();
        });
    }

    private void showImageSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Profile Picture");
        builder.setItems(new CharSequence[]{"Choose from Gallery"}, (dialog, which) -> {
            if (which == 0) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galleryLauncher.launch(galleryIntent);
            }
        });
        builder.show();
    }

    private void loadUserData() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");

        userRef.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User resultedUser = new User();
                resultedUser.setName(snapshot.child("name").getValue(String.class));
                resultedUser.setEmail(snapshot.child("email").getValue(String.class));
                resultedUser.setPhone(snapshot.child("phone").getValue(String.class));

                // Set the data to UI components
                fullNameEditText.setText(resultedUser.getName());
                emailEditText.setText(resultedUser.getEmail());
                phoneEditText.setText(resultedUser.getPhone());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // Load user data from SharedPreferences
//        String fullName = preferences.getString("fullName", "Olivia Johnson");
//        String email = preferences.getString("email", "123@gmail.com");
//        String phone = preferences.getString("phone", "+760987654");
//        String password = preferences.getString("password", "A#g123@lk");
//        String imageUriString = preferences.getString("profile_image_uri", "");
//
//        // Set the data to UI components
//        fullNameEditText.setText(fullName);
//        emailEditText.setText(email);
//        phoneEditText.setText(phone);
//        passwordEditText.setText(password);
//
//        TextView fullNameTextView = findViewById(R.id.full_name);
//        fullNameTextView.setText(fullName);
//
//        // Load profile image if available
//        if (!imageUriString.isEmpty()) {
//            try {
//                selectedImageUri = Uri.parse(imageUriString);
//                profileImage.setImageURI(selectedImageUri);
//            } catch (Exception e) {
//                e.printStackTrace();
//                // If there's an error, use the default image
//                profileImage.setImageResource(R.drawable.profile);
//            }
//        }
    }

    private void saveUserData() {
        uploadImageToFirebase(selectedImageUri);

        // Get data from UI components
        String fullName = fullNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();

        Map<String, Object> updates = new HashMap<>();
        updates.put("email", email);
        updates.put("name", fullName);
        updates.put("phone", phone);

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
        userRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(UserProfile.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadImageToFirebase(Uri imageUri) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("profile_pictures/" + userId + ".jpg");

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // ✅ Save this URL to Firebase Auth or Realtime Database
//                        updateUserProfilePicture(uri.toString());
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show();
                });
    }


    private void updatePackingListCount() {
        // Get the count of packing lists from SharedPreferences
        database.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int packingListsCount = (int) snapshot.getChildrenCount();
                noOfLists.setText(String.valueOf(packingListsCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the packing lists count when returning to this activity
        updatePackingListCount();
    }
}