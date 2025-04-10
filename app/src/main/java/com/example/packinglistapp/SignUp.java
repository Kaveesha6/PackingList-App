package com.example.packinglistapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;
import android.util.Log;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class SignUp extends AppCompatActivity {

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // UI Components
    private TextInputLayout regName, regUsername, regEmail, regPhoneNo, regPassword;
    private CountryCodePicker countryCodePicker;
    private ProgressDialog progressDialog;
    private ProgressDialogUtil progressDialogUtil;

    // Constants
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]{3,20}$");
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final String TAG = "SignUp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();

        // Get Firebase Database instance without trying to enable persistence again
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize UI components
        initViews();

        // Set click listeners
        findViewById(R.id.signup_btn).setOnClickListener(v -> validateAndRegister());
        findViewById(R.id.already_have_account_btn).setOnClickListener(v -> navigateToLogin());
    }

    private void initViews() {
        regName = findViewById(R.id.name);
        regUsername = findViewById(R.id.username);
        regEmail = findViewById(R.id.email);
        regPhoneNo = findViewById(R.id.phoneNo);
        regPassword = findViewById(R.id.password);
        countryCodePicker = findViewById(R.id.country_code_picker);

        // Initialize ProgressDialogUtil
        progressDialogUtil = new ProgressDialogUtil(this);

    }

    private void checkExistingAccounts() {
        if (mAuth.getCurrentUser() != null) {
            Log.d(TAG, "User already signed in: " + mAuth.getCurrentUser().getEmail());
            // Sign out before trying to create a new account
            mAuth.signOut();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void validateAndRegister() {
        checkExistingAccounts();

        // Get input values
        String name = Objects.requireNonNull(regName.getEditText()).getText().toString().trim();
        String username = Objects.requireNonNull(regUsername.getEditText()).getText().toString().trim();
        String email = Objects.requireNonNull(regEmail.getEditText()).getText().toString().trim();
        String phoneNo = Objects.requireNonNull(regPhoneNo.getEditText()).getText().toString().trim();
        String password = Objects.requireNonNull(regPassword.getEditText()).getText().toString().trim();
        String countryCode = countryCodePicker.getSelectedCountryCodeWithPlus();

        // Validate inputs
        if (!validateInputs(name, username, email, phoneNo, password)) {
            return;
        }

        progressDialogUtil.showProgressDialog("Validating your information...");

        // Check username availability first, then register user
        checkUsernameAvailability(username, isAvailable -> {
            if (isAvailable) {
                registerUser(email, password, name, username, formatPhoneNumber(countryCode, phoneNo));
            } else {
                progressDialogUtil.hideProgressDialog();
                regUsername.setError("Username already taken");
                regUsername.requestFocus();
            }
        });
    }

    private boolean validateInputs(String name, String username, String email, String phoneNo, String password) {
        boolean isValid = true;

        // Name validation
        if (name.isEmpty()) {
            regName.setError("Full name is required");
            regName.requestFocus();
            isValid = false;
        } else if (name.length() < 3) {
            regName.setError("Name too short (min 3 characters)");
            regName.requestFocus();
            isValid = false;
        } else {
            regName.setError(null);
        }

        // Username validation
        if (username.isEmpty()) {
            regUsername.setError("Username is required");
            regUsername.requestFocus();
            isValid = false;
        } else if (!USERNAME_PATTERN.matcher(username).matches()) {
            regUsername.setError("Only letters, numbers, ._- allowed (3-20 chars)");
            regUsername.requestFocus();
            isValid = false;
        } else {
            regUsername.setError(null);
        }

        // Email validation
        if (email.isEmpty()) {
            regEmail.setError("Email is required");
            regEmail.requestFocus();
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            regEmail.setError("Enter a valid email address");
            regEmail.requestFocus();
            isValid = false;
        } else {
            regEmail.setError(null);
        }

        // Phone validation
        if (phoneNo.isEmpty()) {
            regPhoneNo.setError("Phone number is required");
            regPhoneNo.requestFocus();
            isValid = false;
        } else if (phoneNo.length() < 6) {
            regPhoneNo.setError("Enter a valid phone number");
            regPhoneNo.requestFocus();
            isValid = false;
        } else {
            regPhoneNo.setError(null);
        }

        // Password validation
        if (password.isEmpty()) {
            regPassword.setError("Password is required");
            regPassword.requestFocus();
            isValid = false;
        } else if (password.length() < MIN_PASSWORD_LENGTH) {
            regPassword.setError("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
            regPassword.requestFocus();
            isValid = false;
        } else {
            regPassword.setError(null);
        }

        return isValid;
    }

    private void checkUsernameAvailability(String username, UsernameCheckCallback callback) {
        Log.d(TAG, "Checking availability for username: " + username);
        mDatabase.child("usernames").child(username.toLowerCase())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean isAvailable = !snapshot.exists();
                        Log.d(TAG, "Username " + username + " available: " + isAvailable);
                        callback.onComplete(isAvailable);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressDialogUtil.hideProgressDialog();

                        // Handle different error codes
                        switch (error.getCode()) {
                            case DatabaseError.PERMISSION_DENIED:
                                Toast.makeText(SignUp.this,
                                        "Firebase permission denied. Check your rules.",
                                        Toast.LENGTH_LONG).show();
                                // Log detailed error information
                                Log.e(TAG, "Permission denied error: " + error.getMessage() +
                                        ", Details: " + error.getDetails(), error.toException());
                                break;
                            case DatabaseError.NETWORK_ERROR:
                                Toast.makeText(SignUp.this,
                                        "Network error. Check your internet connection.",
                                        Toast.LENGTH_LONG).show();
                                break;
                            default:
                                Toast.makeText(SignUp.this,
                                        "Database error: " + error.getMessage(),
                                        Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private String formatPhoneNumber(String countryCode, String phoneNo) {
        return countryCode + phoneNo.replaceAll("[^0-9]", "");
    }

    private void registerUser(String email, String password, String name, String username, String phoneNo) {
        Log.d(TAG, "Starting registration for: " + email);

        // First, check if the email is already in use before trying to create an account
        mAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> signInMethods = task.getResult().getSignInMethods();
                        if (signInMethods != null && !signInMethods.isEmpty()) {
                            // Email is already in use
                            progressDialogUtil.hideProgressDialog();
                            regEmail.setError("Email already in use");
                            regEmail.requestFocus();
                            return;
                        }

                        // Email is not in use, proceed with account creation
                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(createTask -> {
                                    if (createTask.isSuccessful()) {
                                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                        if (firebaseUser != null) {
                                            // Update profile with display name
                                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                    .setDisplayName(name)
                                                    .build();

                                            firebaseUser.updateProfile(profileUpdates)
                                                    .addOnCompleteListener(profileTask -> {
                                                        if (profileTask.isSuccessful()) {
                                                            // Now reserve username and save user data
                                                            reserveUsernameAndSaveUser(firebaseUser, name, username, email, phoneNo);
                                                        } else {
                                                            progressDialogUtil.hideProgressDialog();
                                                            handleFirebaseError(profileTask.getException());
                                                        }
                                                    });
                                        } else {
                                            progressDialogUtil.hideProgressDialog();
                                            Toast.makeText(SignUp.this, "Failed to retrieve user after creation",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        progressDialogUtil.hideProgressDialog();
                                        handleFirebaseError(createTask.getException());
                                    }
                                });
                    } else {
                        // Failed to check email
                        progressDialogUtil.hideProgressDialog();
                        Toast.makeText(SignUp.this, "Failed to verify email availability",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void reserveUsernameAndSaveUser(FirebaseUser user, String name, String username, String email, String phoneNo) {
        // First check if user still exists (might have been deleted during error handling)
        if (user == null || mAuth.getCurrentUser() == null) {
            progressDialogUtil.hideProgressDialog();
            Toast.makeText(this, "Authentication error occurred", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the authentication token to use for database operations
        user.getIdToken(true)
                .addOnCompleteListener(tokenTask -> {
                    if (tokenTask.isSuccessful()) {
                        // Create username reference first
                        mDatabase.child("usernames").child(username.toLowerCase())
                                .setValue(user.getUid())
                                .addOnSuccessListener(aVoid -> {
                                    // Success, now save user data
                                    saveUserData(user.getUid(), name, username, email, phoneNo);
                                })
                                .addOnFailureListener(e -> {
                                    progressDialogUtil.hideProgressDialog();
                                    Log.e(TAG, "Failed to reserve username", e);

                                    // Handle Firebase permission denied errors specifically
                                    if (e.getMessage() != null && e.getMessage().contains("Permission denied")) {
                                        showErrorDialog("Firebase Database Rules Error",
                                                "Failed to reserve username: Permission denied. Please check your Firebase Database Rules or contact support.");
                                    } else {
                                        showErrorMessage("Failed to reserve username: " + e.getMessage());
                                    }

                                    // Delete the user account if username reservation fails
                                    user.delete().addOnCompleteListener(deleteTask -> {
                                        if (deleteTask.isSuccessful()) {
                                            Log.d(TAG, "User account deleted after username reservation failed");
                                        } else {
                                            Log.e(TAG, "Failed to delete user after username reservation failed",
                                                    deleteTask.getException());
                                        }
                                    });
                                });
                    } else {
                        progressDialogUtil.hideProgressDialog();
                        Log.e(TAG, "Failed to get auth token", tokenTask.getException());
                        Toast.makeText(SignUp.this, "Authentication error: Failed to get token",
                                Toast.LENGTH_SHORT).show();

                        // Clean up by deleting the user
                        user.delete();
                    }
                });
    }

    private void saveUserData(String userId, String name, String username, String email, String phoneNo) {
        User user = new User(name, username, email, phoneNo);
        user.setEmailVerified(false);

        // Create user document with initial data structure
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("users/" + userId, user.toMap());
        userUpdates.put("userTrips/" + userId + "/trips", new HashMap<>());

        mDatabase.updateChildren(userUpdates)
                .addOnSuccessListener(aVoid -> {
                    progressDialogUtil.hideProgressDialog();
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    if (currentUser != null) {
                        sendVerificationEmail(currentUser);
                        showSuccessMessage();
                        navigateToLogin();
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialogUtil.hideProgressDialog();
                    Log.e(TAG, "Failed to save user data", e);

                    // Handle Firebase permission denied errors specifically
                    if (e.getMessage() != null && e.getMessage().contains("Permission denied")) {
                        showErrorDialog("Firebase Database Rules Error",
                                "Failed to save user data: Permission denied. Please check your Firebase Database Rules or contact support.");
                    } else {
                        showErrorMessage("Failed to save user data: " + e.getMessage());
                    }

                    // Clean up: delete user account and username reference
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    if (currentUser != null) {
                        currentUser.delete();
                        mDatabase.child("usernames").child(username.toLowerCase()).removeValue();
                    }
                });
    }

    private void sendVerificationEmail(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(this,
                                "Failed to send verification email. Please check your email settings.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleFirebaseError(Exception exception) {
        progressDialogUtil.hideProgressDialog();
        String errorMessage = "Registration failed. Please try again.";

        Log.e(TAG, "Firebase error: " + exception.getMessage(), exception);

        if (exception instanceof FirebaseAuthUserCollisionException) {
            errorMessage = "Email already in use";
            regEmail.setError(errorMessage);
            regEmail.requestFocus();
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            errorMessage = "Invalid email format";
            regEmail.setError(errorMessage);
            regEmail.requestFocus();
        } else if (exception instanceof FirebaseAuthWeakPasswordException) {
            errorMessage = "Password is too weak";
            regPassword.setError(errorMessage);
            regPassword.requestFocus();
        } else {
            // Log the exact exception for debugging
            Log.e(TAG, "Unhandled exception type: " + exception.getClass().getName());
            if (exception.getMessage() != null) {
                errorMessage = exception.getMessage();
            }
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }

    private void showSuccessMessage() {
        Toast.makeText(this,
                "Registration successful! Please check your email to verify your account.",
                Toast.LENGTH_LONG).show();
    }

    private void showErrorMessage(String message) {
        Toast.makeText(this, "Error: " + message, Toast.LENGTH_LONG).show();
    }

    private void showErrorDialog(String title, String message) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void navigateToLogin() {
        startActivity(new Intent(this, Login.class));
        finishAffinity();
    }

    interface UsernameCheckCallback {
        void onComplete(boolean isAvailable);
    }
}