package com.example.a431transit.presentation.Dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.a431transit.R;
import com.example.a431transit.objects.bus_stop.BusStop;
import com.example.a431transit.util.storage_managers.CategoriesManager;

import java.util.Collections;
import java.util.List;

public class CategoryDialogs {
    public static void showEditCategoryMenu(Context context, View view,
                                            Runnable onAddCategory, Runnable onRemoveCategory) {
        Context wrapper = new ContextThemeWrapper(context, R.style.MyMenuStyle);
        PopupMenu popupMenu = new PopupMenu(wrapper, view);
        popupMenu.getMenuInflater().inflate(R.menu.saved_stops_popup_menu, popupMenu.getMenu());

        // Set click listener for menu items
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getItemId() == R.id.delete_a_category) {
                onRemoveCategory.run();
            } else if (menuItem.getItemId() == R.id.add_a_category) {
                onAddCategory.run();
            } else {
                return false;
            }
            return true;
        });

        popupMenu.show();
    }

    public static void showAddCategoryDialog(Context context, CategoriesManager categoriesManager,
                                             Runnable onDialogDismiss) {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.text_dialog, null);
        alert.setView(dialogView);
        final AlertDialog alertDialog = alert.create();

        TextView title = dialogView.findViewById(R.id.input_dialog_title);
        title.setText("Enter new category name: ");
        EditText inputText = dialogView.findViewById(R.id.input_dialog_edit_text);
        Button cancelButton = dialogView.findViewById(R.id.input_dialog_cancel_btn);
        Button submitButton = dialogView.findViewById(R.id.input_dialog_submit_btn);

        submitButton.setOnClickListener(v -> {
            //todo communicate with logic layer
            String categoryName = inputText.getText().toString();
            categoriesManager.addCategory(categoryName);

            onDialogDismiss.run();
            alertDialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> alertDialog.dismiss());

        alertDialog.show();
    }

    public static void showRemoveCategoriesDialog(Context context, CategoriesManager categoriesManager,
                                                  Runnable onDialogDismiss) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.multi_choice_dialog, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        List<String> editableCategories = categoriesManager.getUserCreatedCategories();
        Collections.reverse(editableCategories);

        boolean[] checkedItems = new boolean[editableCategories.size()];

        TextView titleTextView = dialogView.findViewById(R.id.input_dialog_title);
        ListView listView = dialogView.findViewById(R.id.input_dialog_multi_choice);
        Button cancelButton = dialogView.findViewById(R.id.input_dialog_cancel_btn);
        Button submitButton = dialogView.findViewById(R.id.input_dialog_submit_btn);

        titleTextView.setText("Select Categories to Delete");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_list_item_multiple_choice, editableCategories);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            CheckedTextView checkedTextView = (CheckedTextView) view;
            checkedItems[position] = !checkedItems[position];
            checkedTextView.setChecked(checkedItems[position]);
        });

        submitButton.setOnClickListener(v -> {
            //todo communicate with logic layer
            for (int i = 0; i < editableCategories.size(); i++) {
                if (checkedItems[i]) {
                    categoriesManager.removeCategory(editableCategories.get(i));
                }
            }

            onDialogDismiss.run();
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // Method to show add or remove from categories dialog with a Runnable callback
    public static void showAddOrRemoveFromCategoriesDialog(Context context, BusStop busStop,
                                                           CategoriesManager categoriesManager) {
        // Create the alert and link it to the custom dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.multi_choice_dialog, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Get the list of editable categories and order them by the earliest creation date
        List<String> editableCategories = categoriesManager.getUserCreatedCategories();
        Collections.reverse(editableCategories);

        // Initialize boolean array to track selected categories
        boolean[] checkedItems = new boolean[editableCategories.size()];

        // If the bus stop is saved in a category already, update the boolean array to match
        for (int i = 0; i < editableCategories.size(); i++) {
            checkedItems[i] = busStop.inCategory(editableCategories.get(i));
        }

        // Get references to our components
        TextView titleTextView = dialogView.findViewById(R.id.input_dialog_title);
        ListView listView = dialogView.findViewById(R.id.input_dialog_multi_choice);
        Button cancelButton = dialogView.findViewById(R.id.input_dialog_cancel_btn);
        Button submitButton = dialogView.findViewById(R.id.input_dialog_submit_btn);

        // Set dialog title
        titleTextView.setText("Assign Bus Stop to Categories");

        // Create an adapter for each category in the list.
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_multiple_choice, editableCategories) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                CheckedTextView checkedTextView = view.findViewById(android.R.id.text1);
                checkedTextView.setChecked(checkedItems[position]);

                return view;
            }
        };

        listView.setAdapter(adapter);

        // Set multi-choice items and listen for changes
        listView.setOnItemClickListener((parent, view, position, id) -> {
            CheckedTextView checkedTextView = (CheckedTextView) view;
            checkedItems[position] = !checkedItems[position];
            checkedTextView.setChecked(checkedItems[position]);
        });

        submitButton.setOnClickListener(v -> {
            //todo communicate with logic layer
            //Remove stop from every unchecked category
            for (int i = 0; i < editableCategories.size(); i++) {
                if (checkedItems[i]) {
                    categoriesManager.addStopToCategory(editableCategories.get(i), busStop);
                } else {
                    categoriesManager.removeStopFromCategory(editableCategories.get(i), busStop);
                }
            }

            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        // Show the dialog
        dialog.show();
    }
}
