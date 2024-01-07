package com.example.a431transit.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import com.google.gson.Gson;

import android.util.Log;
import android.util.SparseArray;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.example.a431transit.R;
import com.example.a431transit.model.stops.BusStop;
import com.example.a431transit.util.api_communication.TransitAPIService;
import com.example.a431transit.util.bus_stop_expandable_list.BusStopGridViewItemClickInterface;
import com.example.a431transit.util.bus_stop_expandable_list.CategoryExpandableListAdapter;
import com.example.a431transit.util.storage_managers.CategoriesManager;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SavedStopsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SavedStopsFragment extends Fragment implements BusStopGridViewItemClickInterface {
    private static final String EXPANDED_LIST_FILE_NAME = "expanded_list_state.json";
    private static final int REQUEST_CODE = 1;
    TransitAPIService transitService;
    private List<String> categoryNames;
    private HashMap<String, List<BusStop>> categoryChildren;
    private CategoriesManager categoriesManager;
    private ExpandableListView categoryListView;
    private CategoryExpandableListAdapter categoryExpandableListAdapter;

    public SavedStopsFragment() {
        // Required empty public constructor
    }

    public SavedStopsFragment(TransitAPIService transitService) {
        this.transitService = transitService;
    }

    public static SavedStopsFragment newInstance() {
        return new SavedStopsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_saved_stops, container, false);

        categoriesManager = new CategoriesManager(getContext());

        categoryListView = rootView.findViewById(R.id.SavedStopsExpandableList);
        ImageButton editCategoryBtn = rootView.findViewById(R.id.SavedEditCategoriesBtn);

        prepareListData();

        categoryExpandableListAdapter = new CategoryExpandableListAdapter(getActivity(), categoryNames, categoryChildren, this, transitService);
        categoryListView.setAdapter(categoryExpandableListAdapter);

        // Load the expanded state from SharedPreferences and apply it
        loadExpandedState();

        categoryListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                // This way the expander cannot be collapsed
                // on click event of group item
                return false;
            }
        });

        editCategoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context wrapper = new ContextThemeWrapper(getContext(), R.style.MyMenuStyle);
                PopupMenu popupMenu = new PopupMenu(wrapper, v);
                popupMenu.getMenuInflater().inflate(R.menu.saved_stops_popup_menu, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (menuItem.getItemId() == R.id.delete_a_category) {
                            removeCategoriesDialog();
                        } else if (menuItem.getItemId() == R.id.add_a_category) {
                            addCategoryDialog();
                        }
                        return true;
                    }
                });

                popupMenu.show();
            }
        });

        // Set an OnGroupExpandListener to save the expanded state when a group is expanded
        categoryListView.setOnGroupExpandListener((groupPosition) -> saveExpandedState());

        // Set an OnGroupCollapseListener to save the expanded state when a group is collapsed
        categoryListView.setOnGroupCollapseListener((groupPosition) -> saveExpandedState());


        return rootView;
    }

    private void prepareListData() {
        categoriesManager.update();

        categoryNames = categoriesManager.getAllCategories();
        Collections.reverse(categoryNames);

        categoryChildren = new HashMap<>();

        for (String category : categoryNames) {
            categoryChildren.put(category, categoriesManager.getBusStopsFromCategory(category));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                prepareListData();
                categoryExpandableListAdapter.setData(categoryNames, categoryChildren);
            }
        }
    }

    @Override
    public void onGridViewBusStopClick(BusStop busStop) {
        Intent intent = new Intent(getContext(), BusArrivals.class);

        intent.putExtra("BUS_STOP", busStop);

        startActivityForResult(intent, REQUEST_CODE);
    }

    private void addCategoryDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.text_dialog, null);

        //get and init components
        TextView title = dialogView.findViewById(R.id.input_dialog_title);
        title.setText("Enter new category name: ");
        EditText inputText = dialogView.findViewById(R.id.input_dialog_edit_text);
        Button cancelButton = dialogView.findViewById(R.id.input_dialog_cancel_btn);
        Button submitButton = dialogView.findViewById(R.id.input_dialog_submit_btn);

        alert.setView(dialogView);
        final AlertDialog alertDialog = alert.create();

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String categoryName = inputText.getText().toString();
                categoriesManager.addCategory(categoryName);

                saveExpandedState();
                prepareListData();
                categoryExpandableListAdapter.setData(categoryNames, categoryChildren);

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

    private void removeCategoriesDialog() {
        List<String> editableCategories = categoriesManager.getEditableCategories();

        // Initialize boolean array to track selected categories
        boolean[] checkedItems = new boolean[editableCategories.size()];

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        View dialogView = getLayoutInflater().inflate(R.layout.multi_choice_dialog, null);
        builder.setView(dialogView);

        // Create and show the AlertDialog
        AlertDialog dialog = builder.create();

        // Get references to custom views
        TextView titleTextView = dialogView.findViewById(R.id.input_dialog_title);
        ListView listView = dialogView.findViewById(R.id.input_dialog_multi_choice);
        Button cancelButton = dialogView.findViewById(R.id.input_dialog_cancel_btn);
        Button submitButton = dialogView.findViewById(R.id.input_dialog_submit_btn);

        // Set dialog title
        titleTextView.setText("Select Categories to Delete");

        // Create ArrayAdapter for the ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_multiple_choice, editableCategories);
        listView.setAdapter(adapter);

        // Set multi-choice items and listen for changes
        listView.setOnItemClickListener((parent, view, position, id) -> {
            CheckedTextView checkedTextView = (CheckedTextView) view;
            checkedItems[position] = !checkedItems[position];
            checkedTextView.setChecked(checkedItems[position]);
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < editableCategories.size(); i++) {
                    if (checkedItems[i]) {
                        categoriesManager.removeCategory(editableCategories.get(i));
                        saveExpandedState();
                    }
                }

                prepareListData();
                categoryExpandableListAdapter.setData(categoryNames, categoryChildren);

                dialog.dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void saveExpandedState() {
        Map<Integer, Boolean> expandedMap = new HashMap<>();
        Gson gson = new Gson();
        String json;

        int groupCount = categoryExpandableListAdapter.getGroupCount();
        for (int i = 0; i < groupCount; i++) {
            expandedMap.put(i, categoryListView.isGroupExpanded(i));
        }

        json = gson.toJson(expandedMap, new TypeToken<Map<Integer, Boolean>>() {}.getType());

        Log.i("a", json);

        File file = new File(getContext().getExternalFilesDir(null), EXPANDED_LIST_FILE_NAME);
        try (OutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(json.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load the expanded state from SharedPreferences and apply it to the ExpandableListView
    private void loadExpandedState() {
        try {
            File file = new File(getContext().getExternalFilesDir(null), EXPANDED_LIST_FILE_NAME);

            if (file.exists()) {
                // Read the file as a string
                StringBuilder jsonString = new StringBuilder();
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonString.append(line);
                }
                reader.close();

                Log.i("aasd", jsonString.toString());
                Gson gson = new Gson();
                Map<Integer, Boolean> expandedGroups = gson.fromJson(jsonString.toString(), new TypeToken<Map<Integer, Boolean>>() {}.getType());

                for (int i = 0; i < categoryExpandableListAdapter.getGroupCount(); i++) {
                    if(expandedGroups.get(i))
                    {
                        categoryListView.expandGroup(i);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}