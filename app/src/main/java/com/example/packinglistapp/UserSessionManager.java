package com.example.packinglistapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * Class to manage user session and preferences with secure encryption
 */
public class UserSessionManager {

    private static final String TAG = "UserSessionManager";

    // Shared Preferences reference
    private SharedPreferences pref;

    // Editor reference for Shared preferences
    private Editor editor;

    // Context
    private Context context;

    // Shared preferences file name
    private static final String PREFER_NAME = "PackingListAppPref";

    // Shared preferences mode
    private static final int PRIVATE_MODE = 0;

    // Shared preferences keys
    private static final String IS_USER_LOGIN = "IsUserLoggedIn";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_EMAIL_IV = "email_iv";
    private static final String KEY_PASSWORD_IV = "password_iv";

    // Encryption constants
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String ENCRYPTION_KEY_ALIAS = "PackingListAppEncryptionKey";
    private static final int GCM_TAG_LENGTH = 128;

    // Constructor
    public UserSessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREFER_NAME, PRIVATE_MODE);
        editor = pref.edit();
        try {
            createKey();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing encryption key", e);
        }
    }

    /**
     * Create encryption key in the Android Keystore
     */
    private void createKey() throws NoSuchProviderException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException {
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);

            if (!keyStore.containsAlias(ENCRYPTION_KEY_ALIAS)) {
                KeyGenerator keyGenerator = KeyGenerator.getInstance(
                        KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE);
                keyGenerator.init(new KeyGenParameterSpec.Builder(ENCRYPTION_KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setRandomizedEncryptionRequired(true)
                        .build());
                keyGenerator.generateKey();
            }
        } catch (KeyStoreException | CertificateException | IOException e) {
            Log.e(TAG, "Error creating encryption key", e);
            throw new RuntimeException("Failed to create encryption key", e);
        }
    }

    /**
     * Encrypt data
     */
    private String[] encrypt(String plaintext) {
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);
            SecretKey secretKey = ((KeyStore.SecretKeyEntry) keyStore.getEntry(ENCRYPTION_KEY_ALIAS, null)).getSecretKey();

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] iv = cipher.getIV();
            byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes());

            return new String[]{
                    Base64.encodeToString(encryptedBytes, Base64.DEFAULT),
                    Base64.encodeToString(iv, Base64.DEFAULT)
            };
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException |
                 IOException | UnrecoverableEntryException | NoSuchPaddingException |
                 InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            Log.e(TAG, "Encryption error", e);
            return null;
        }
    }

    /**
     * Decrypt data
     */
    private String decrypt(String encryptedData, String ivString) {
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);
            SecretKey secretKey = ((KeyStore.SecretKeyEntry) keyStore.getEntry(ENCRYPTION_KEY_ALIAS, null)).getSecretKey();

            byte[] encryptedBytes = Base64.decode(encryptedData, Base64.DEFAULT);
            byte[] iv = Base64.decode(ivString, Base64.DEFAULT);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));

            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes);
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException |
                 IOException | UnrecoverableEntryException | NoSuchPaddingException |
                 InvalidKeyException | InvalidAlgorithmParameterException |
                 BadPaddingException | IllegalBlockSizeException e) {
            Log.e(TAG, "Decryption error", e);
            return null;
        }
    }

    /**
     * Create login session with encrypted credentials
     */
    public void createLoginSession(String email, String password) {
        // Storing login value as TRUE
        editor.putBoolean(IS_USER_LOGIN, true);

        // Encrypt and store email
        String[] encryptedEmail = encrypt(email);
        if (encryptedEmail != null) {
            editor.putString(KEY_EMAIL, encryptedEmail[0]);
            editor.putString(KEY_EMAIL_IV, encryptedEmail[1]);
        }

        // Encrypt and store password
        String[] encryptedPassword = encrypt(password);
        if (encryptedPassword != null) {
            editor.putString(KEY_PASSWORD, encryptedPassword[0]);
            editor.putString(KEY_PASSWORD_IV, encryptedPassword[1]);
        }

        // Commit changes
        editor.apply();
    }

    /**
     * Get stored session data (decrypted)
     */
    public String[] getUserDetails() {
        String[] userData = new String[2];

        String encryptedEmail = pref.getString(KEY_EMAIL, null);
        String emailIv = pref.getString(KEY_EMAIL_IV, null);

        String encryptedPassword = pref.getString(KEY_PASSWORD, null);
        String passwordIv = pref.getString(KEY_PASSWORD_IV, null);

        if (encryptedEmail != null && emailIv != null) {
            userData[0] = decrypt(encryptedEmail, emailIv);
        }

        if (encryptedPassword != null && passwordIv != null) {
            userData[1] = decrypt(encryptedPassword, passwordIv);
        }

        return userData;
    }

    /**
     * Check login method will check user login status
     */
    public boolean isUserLoggedIn() {
        return pref.getBoolean(IS_USER_LOGIN, false);
    }

    /**
     * Clear session details
     */
    public void logoutUser() {
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.apply();
    }
}