package com.example.a431transit.presentation;

import android.app.AlertDialog;
import android.content.Context;

public class Dialogs {
    private void showAlert(Context context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // Set the title and message for the alert dialog
        builder.setTitle(title)
                .setMessage(message);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void addCategoryDialog(){

    }

    public void removeCategoriesDialog(){

    }
}
