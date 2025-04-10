package com.example.packinglistapp;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * Utility class for handling progress dialogs throughout the application
 */
public class ProgressDialogUtil {

    private ProgressDialog progressDialog;
    private Context context;

    public ProgressDialogUtil(Context context) {
        this.context = context;
    }

    public void showProgressDialog(String message) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(context);
            progressDialog.setCancelable(false);
        }
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    public void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}