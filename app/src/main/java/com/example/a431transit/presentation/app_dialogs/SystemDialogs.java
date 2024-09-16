package com.example.a431transit.presentation.app_dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;

public class SystemDialogs {
    public static void showDetailedAlert(Context context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        //set the title and message for the alert dialog
        builder.setTitle(title)
                .setMessage(message);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public static void showDefaultAlert(Context context, String error) {
        Log.e("431Transit", error);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        //set the title and message for the alert dialog
        builder.setTitle("Error")
                .setMessage("Could not fulfill your request at this time. Sorry!");

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }
}
