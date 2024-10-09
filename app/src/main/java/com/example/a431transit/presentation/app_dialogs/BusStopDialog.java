package com.example.a431transit.presentation.app_dialogs;

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
import com.example.a431transit.logic.BusStopHandler;
import com.example.a431transit.objects.bus_route.BusRoute;
import com.example.a431transit.objects.bus_stop.BusStop;
import com.example.a431transit.objects.exceptions.BadRequestException;

import java.util.ArrayList;
import java.util.List;

public class BusStopDialog {
    public static void showEditBusMenu(Context context, View view,
                                            Runnable onAddRemoveCategory, Runnable onRename, Runnable onFilter) {
        Context wrapper = new ContextThemeWrapper(context, R.style.MyMenuStyle);
        PopupMenu popupMenu = new PopupMenu(wrapper, view);
        popupMenu.getMenuInflater().inflate(R.menu.arrivals_popup_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getItemId() == R.id.add_remove_from_collection) {
                onAddRemoveCategory.run();
            } else if (menuItem.getItemId() == R.id.rename_bus_stop) {
                onRename.run();
            } else if (menuItem.getItemId() == R.id.filter_routes) {
                onFilter.run();
            }

            return true;
        });

        popupMenu.show();
    }

    //If user has chosen to rename a bus stop, display a alert dialog with an editable text
    public static void renameBusStopDialog(Context context, BusStop busStop, Runnable onRename) {
        //Initialize the alert and set it to our custom dialog
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.text_dialog, null);
        alert.setView(dialogView);
        final AlertDialog alertDialog = alert.create();

        //get and init components
        EditText inputText = dialogView.findViewById(R.id.input_dialog_edit_text);
        inputText.setHint(busStop.getOriginalName());
        Button cancelButton = dialogView.findViewById(R.id.input_dialog_cancel_btn);
        Button submitButton = dialogView.findViewById(R.id.input_dialog_submit_btn);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nickname = inputText.getText().toString();

                //update and save the bus stop in storage
                try{
                    BusStopHandler.setBusStopNickname(busStop, nickname);
                } catch (BadRequestException e){
                    SystemDialogs.showDefaultAlert(context, "Not a valid nickname!");
                }

                onRename.run();

                alertDialog.dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        // Show the dialog
        alertDialog.show();
    }

    public static void showFilterRoutesDialog(Context context, BusStop busStop, Runnable onFilterApplied) {
        //Fetch bus routes
        BusStopHandler.fetchBusRoutes(busStop, busRoutes -> {
            if (busRoutes == null || busRoutes.isEmpty()) {
                return;
            }

            //create the alert and link it to the custom dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View dialogView = LayoutInflater.from(context).inflate(R.layout.multi_choice_dialog, null);
            builder.setView(dialogView);
            AlertDialog dialog = builder.create();

            List<String> filteredRoutes = busStop.getFilteredRoutes();

            //initialize boolean array to track selected bus routes
            boolean[] checkedItems = new boolean[busRoutes.size()];

            //if user already has a filtered list, update the boolean array to match
            for (int i = 0; i < busRoutes.size(); i++) {
                checkedItems[i] = (filteredRoutes != null && filteredRoutes.contains(busRoutes.get(i).getKey()));
            }

            //get references to components
            TextView titleTextView = dialogView.findViewById(R.id.input_dialog_title);
            ListView listView = dialogView.findViewById(R.id.input_dialog_multi_choice);
            Button cancelButton = dialogView.findViewById(R.id.input_dialog_cancel_btn);
            Button submitButton = dialogView.findViewById(R.id.input_dialog_submit_btn);

            titleTextView.setText("Select Bus Routes to Display");

            //create an adapter for the bus routes list
            ArrayAdapter<BusRoute> adapter = new ArrayAdapter<BusRoute>(context, android.R.layout.simple_list_item_multiple_choice, busRoutes) {
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

            //set multi-choice items and listen for changes
            listView.setOnItemClickListener((parent, view, position, id) -> {
                CheckedTextView checkedTextView = (CheckedTextView) view;
                checkedItems[position] = !checkedItems[position];
                checkedTextView.setChecked(checkedItems[position]);
            });

            submitButton.setOnClickListener(v -> {
                //collect all bus routes that were selected to be displayed
                List<String> newFilteredRoutes = new ArrayList<>();
                for (int i = 0; i < busRoutes.size(); i++) {
                    if (checkedItems[i]) {
                        newFilteredRoutes.add(busRoutes.get(i).getKey());
                    }
                }

                //update the bus stop and save the newest version to storage
                BusStopHandler.setBusStopFilteredRoutes(busStop,newFilteredRoutes);

                onFilterApplied.run();

                dialog.dismiss();
            });

            cancelButton.setOnClickListener(v -> dialog.dismiss());

            dialog.show();

        });
    }
}
