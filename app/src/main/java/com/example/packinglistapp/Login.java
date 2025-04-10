package com.example.packinglistapp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {

    // Constants
    private static final String TAG = "Login";

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // UI Components
    private TextInputLayout usernameOrEmail, passwordLayout;
    private CheckBox rememberMeCheckBox;
    private ImageView facebookLoginBtn, googleLoginBtn, twitterLoginBtn;

    // Session Manager
    private UserSessionManager sessionManager;

    private ProgressDialog progressDialog;

    private ProgressDialogUtil progressDialogUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Initialize Firebase with improved method
        initFirebase();
        checkAndFixDatabaseAccess();

        // Initialize Session Manager
        sessionManager = new UserSessionManager(this);

        // Initialize UI components
        initViews();

        // Check if user is already signed in
        checkCurrentUser();

        //Call Network Available checker
        isNetworkAvailable();

        // Call Firebase connection checker
        checkFirebaseConnection();

        // Setup click listeners for regular login
        setupRegularLoginButtons();

        // For now, just hide the social login buttons or make them show a "coming soon" message
        //setupSocialButtonsAsComingSoon();

        // Check for saved credentials
        if (sessionManager.isUserLoggedIn()) {
            String[] userData = sessionManager.getUserDetails();
            if (userData[0] != null && userData[1] != null) {
                // Auto-fill the email field
                usernameOrEmail.getEditText().setText(userData[0]);
                passwordLayout.getEditText().setText(userData[1]);
                rememberMeCheckBox.setChecked(true);
            }
        }
    }

    // Method to check if network is available
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
        return false;
    }

    // Method to check Firebase connection status
    private void checkFirebaseConnection() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference connectedRef = database.getReference(".info/connected");

        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = dataSnapshot.getValue(Boolean.class);
                if (!connected) {
                    // If not connected, show offline message
                    Toast.makeText(Login.this, "You appear to be offline. Some features may be limited.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseError", "Error in checking connection", databaseError.toException());
            }
        });
    }

    // initFirebase()
    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        // Enable offline persistence more reliably
        try {
            database.setPersistenceEnabled(true);
        } catch (Exception e) {
            Log.w(TAG, "Persistence already enabled", e);
        }

        mDatabase = database.getReference();

        // Add a connection status listener
        DatabaseReference connectedRef = database.getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean connected = Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
                if (!connected) {
                    // Show offline warning but don't block login
                    Toast.makeText(Login.this, "You appear to be offline. Some features may be limited.",
                            Toast.LENGTH_LONG).show();
                    Log.w(TAG, "Firebase connection lost or unavailable");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Firebase connection listener cancelled", error.toException());
            }
        });
    }
    private void initViews() {
        usernameOrEmail = findViewById(R.id.username);
        passwordLayout = findViewById(R.id.password);
        rememberMeCheckBox = findViewById(R.id.remember_me_checkbox);

        // Social login buttons
        facebookLoginBtn = findViewById(R.id.facebook_login);
        googleLoginBtn = findViewById(R.id.google_login);
        twitterLoginBtn = findViewById(R.id.twitter_login);

        // Initialize ProgressDialogUtil
        progressDialogUtil = new ProgressDialogUtil(this);

        // You can keep this if you want to transition gradually
        progressDialog = new ProgressDialog(this);
    }

    private void setupRegularLoginButtons() {
        Button btnLogin = findViewById(R.id.login_btn);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndLogin();
            }
        });

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        Button btnNewUser = findViewById(R.id.new_user_btn);
        btnNewUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, SignUp.class);
                startActivity(intent);
            }
        });

        // Setup forget password button
        Button btnForgetPassword = findViewById(R.id.forget_password_btn);
        btnForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleForgetPassword();
            }
        });
    }

    private void setupSocialButtonsAsComingSoon() {
        // Set click listeners that show "Coming soon" toast
        View.OnClickListener comingSoonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Login.this, "Social login coming soon!", Toast.LENGTH_SHORT).show();
            }
        };

        facebookLoginBtn.setOnClickListener(comingSoonListener);
        googleLoginBtn.setOnClickListener(comingSoonListener);
        twitterLoginBtn.setOnClickListener(comingSoonListener);

        // You might want to gray out the buttons to indicate they're not functional
        facebookLoginBtn.setAlpha(0.5f);
        googleLoginBtn.setAlpha(0.5f);
        twitterLoginBtn.setAlpha(0.5f);
    }

    private void checkCurrentUser() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // For email/password login, check verification
            if (currentUser.getProviderData().size() > 1 &&
                    currentUser.getProviderData().get(1).getProviderId().equals("password")) {
                if (currentUser.isEmailVerified()) {
                    navigateToHome();
                } else {
                    // Email is not verified, sign out
                    mAuth.signOut();
                    Toast.makeText(Login.this,
                            "Please verify your email before logging in",
                            Toast.LENGTH_LONG).show();
                }
            } else {
                // Social login doesn't need email verification
                navigateToHome();
            }
        }
    }

    // In Login.java, modify the validateAndLogin method to handle offline:
    private void validateAndLogin() {
        String userInput = usernameOrEmail.getEditText().getText().toString().trim();
        String password = passwordLayout.getEditText().getText().toString().trim();

        // Reset errors
        usernameOrEmail.setError(null);
        passwordLayout.setError(null);

        // Check for empty fields
        if (userInput.isEmpty() || password.isEmpty()) {
            if (userInput.isEmpty()) {
                usernameOrEmail.setError("Username or Email is required");
            }
            if (password.isEmpty()) {
                passwordLayout.setError("Password is required");
            }
            return;
        }

        // Check if device is online
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();

        if (!isConnected) {
            // If offline, check if we have credentials cached
            if (rememberMeCheckBox.isChecked() && sessionManager.isUserLoggedIn()) {
                String[] savedData = sessionManager.getUserDetails();
                // If trying to login with saved credentials, allow it
                if (userInput.equals(savedData[0]) && password.equals(savedData[1])) {
                    Toast.makeText(Login.this, "Logging in with cached credentials",
                            Toast.LENGTH_SHORT).show();
                    navigateToHome();
                    return;
                }
            }
            Toast.makeText(Login.this, "You're offline. Please connect to the internet to log in.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Show ProgressDialog
        showProgressDialog("Logging in...");

        // Continue with normal login flow
        boolean isEmail = userInput.contains("@");
        if (isEmail) {
            loginWithEmail(userInput, password);
        } else {
            findEmailByUsername(userInput, password);
        }
    }

    private void findEmailByUsername(String username, String password) {
        // Show loading state
        showProgressDialog("Looking up username...");

        Log.d(TAG, "Looking up username: " + username);

        // Debug the path
        Log.d(TAG, "Database path: " + mDatabase.child("users").toString());

        // ISSUE: The current path might not match your Firebase structure
        // Change this line to correctly reference your users data
        Query query = mDatabase.child("users").orderByChild("username").equalTo(username);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean emailFound = false; // Declare variable inside the method

                if (dataSnapshot.exists()) { // Added check if data exists
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        if (username.equals(userSnapshot.child("username").getValue(String.class))) {
                            String email = userSnapshot.child("email").getValue(String.class);
                            if (email != null) {
                                // Login with the found email
                                loginWithEmail(email, password);
                                emailFound = true;
                                break;
                            }
                        }
                    }

                    if (!emailFound) {
                        // More specific error message
                        hideProgressDialog(); // Make sure to hide dialog
                        usernameOrEmail.setError("Found user but email is missing. Please contact support.");
                        usernameOrEmail.requestFocus();
                        Log.e(TAG, "User found but email field is null for username: " + username);
                    }
                } else {
                    // Username not found
                    hideProgressDialog(); // Make sure to hide dialog
                    usernameOrEmail.setError("Username not found");
                    usernameOrEmail.requestFocus();
                    Log.e(TAG, "Username not found: " + username);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                hideProgressDialog();
                // Enhanced error logging
                Log.e(TAG, "Database query cancelled - Code: " + databaseError.getCode() +
                        ", Message: " + databaseError.getMessage() +
                        ", Details: " + databaseError.getDetails());

                Toast.makeText(Login.this,
                        "Database error: " + databaseError.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loginWithEmail(String email, String password) {
        // Show loading state
        showProgressDialog("Signing in...");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, signInTask -> {  // Renamed from 'task' to 'signInTask'
                    hideProgressDialog();

                    if (signInTask.isSuccessful()) {
                        // Sign in success
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Replace this block with your new code
                            if (AppConfig.REQUIRE_EMAIL_VERIFICATION && !user.isEmailVerified()) {
                                // Email verification is required but not verified
                                Toast.makeText(Login.this,
                                        "Please verify your email before logging in",
                                        Toast.LENGTH_LONG).show();

                                // Option to resend verification email
                                user.sendEmailVerification()
                                        .addOnCompleteListener(verifyTask -> {
                                            if (verifyTask.isSuccessful()) {
                                                Toast.makeText(Login.this,
                                                        "Verification email resent",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                // Sign out since email is not verified
                                mAuth.signOut();
                            } else {
                                // Either verification not required or email is verified
                                Toast.makeText(Login.this, "Login successful",
                                        Toast.LENGTH_SHORT).show();

                                // Save user credentials if "Remember Me" is checked
                                if (rememberMeCheckBox.isChecked()) {
                                    sessionManager.createLoginSession(email, password);
                                } else {
                                    // Clear any saved credentials
                                    sessionManager.logoutUser();
                                }

                                navigateToHome();
                            }
                        }
                    } else {
                        // Handle login failures
                        handleLoginError(signInTask.getException());
                    }
                });
    }

    private void handleLoginError(Exception exception) {
        Log.e(TAG, "Login failed", exception);

        if (exception instanceof FirebaseAuthInvalidUserException) {
            // Email doesn't exist
            usernameOrEmail.setError("No account found with this email");
            usernameOrEmail.requestFocus();
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            // Wrong password
            passwordLayout.setError("Invalid password");
            passwordLayout.requestFocus();
        } else {
            // Generic error
            Toast.makeText(Login.this,
                    "Login failed: " + exception.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void handleForgetPassword() {
        String userInput = usernameOrEmail.getEditText().getText().toString().trim();

        if (userInput.isEmpty()) {
            usernameOrEmail.setError("Please enter your registered email address or username");
            usernameOrEmail.requestFocus();
            return;
        }

        // Check if input is email or username
        boolean isEmail = userInput.contains("@");

        if (isEmail) {
            // Direct password reset with email
            sendPasswordResetEmail(userInput);
        } else {
            // Find email associated with username and then send reset
            findEmailForPasswordReset(userInput);
        }
    }

    private void findEmailForPasswordReset(String username) {
        // Show loading state
        showProgressDialog("Looking up account...");

        // Query to find email by username
        Query query = mDatabase.child("users").orderByChild("username").equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Username found, get the associated email
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String email = snapshot.child("email").getValue(String.class);
                        if (email != null) {
                            // Send password reset to the found email
                            sendPasswordResetEmail(email);
                            return;
                        }
                    }
                    // If we get here, email was null
                    hideProgressDialog();
                    usernameOrEmail.setError("Account error. Please use email to reset password.");
                    usernameOrEmail.requestFocus();
                } else {
                    // Username not found
                    hideProgressDialog();
                    usernameOrEmail.setError("Username not found");
                    usernameOrEmail.requestFocus();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                hideProgressDialog();
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                Toast.makeText(Login.this,
                        "Database error. Please try again.",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void sendPasswordResetEmail(String email) {
        // Show loading state
        showProgressDialog("Sending reset email...");

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    hideProgressDialog();

                    if (task.isSuccessful()) {
                        Toast.makeText(Login.this,
                                "Password reset email sent to " + email,
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(Login.this,
                                "Failed to send reset email: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    // Add this to the Login class
    private void checkAndFixDatabaseAccess() {
        // First test if we can access the path that doesn't require auth
        mDatabase.child("usernames").limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "Public path access successful");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Cannot access even public paths. Firebase config issue: " + databaseError.getMessage());
                Toast.makeText(Login.this,
                        "Database configuration error. Please reinstall the app or contact support.",
                        Toast.LENGTH_LONG).show();
            }
        });

        // Call this method from onCreate
    }

    private void showProgressDialog(String message) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(Login.this);
            progressDialog.setCancelable(false);
        }
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void navigateToHome() {
        Intent intent = new Intent(Login.this, Home.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}