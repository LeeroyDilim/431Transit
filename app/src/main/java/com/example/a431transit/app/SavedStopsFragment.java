package com.example.a431transit.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

import androidx.fragment.app.Fragment;

import com.example.a431transit.R;
import com.example.a431transit.objects.bus_stop.BusStop;
import com.example.a431transit.util.api_communication.TransitAPIService;
import com.example.a431transit.util.bus_stop_expandable_list.BusStopGridViewItemClickInterface;
import com.example.a431transit.util.bus_stop_expandable_list.CategoryExpandableListAdapter;
import com.example.a431transit.util.storage_managers.CategoriesManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SavedStopsFragment extends Fragment implements BusStopGridViewItemClickInterface {
    //todo move to constants class
    private static final String EXPANDED_LIST_FILE_NAME = "expanded_list_state.json";
    private static final int REQUEST_CODE = 1;
    private TransitAPIService transitService;
    private List<String> categoryNames;
    private HashMap<String, List<BusStop>> categoryChildren;
    //todo communicate to logic layer instead
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

        //get user categories and saved stops from storage
        categoriesManager = new CategoriesManager(getContext());
        prepareListData();

        initComponents(rootView);

        //Expand the categories in which the user has left expanded from the previous usage of this app
        loadExpandedState();

        return rootView;
    }

    //Load categories and bus stops from storage
    private void prepareListData() {
        //get most recent version of the json file
        categoriesManager.update();

        //get category list and order them by the earliest creation date
        categoryNames = categoriesManager.getAllCategories();
        Collections.reverse(categoryNames);

        categoryChildren = new HashMap<>();

        //Since the category json stores pointers of the bus stop data, get actual bus stop instances
        for (String category : categoryNames) {
            categoryChildren.put(category, categoriesManager.getBusStopsFromCategory(category));
        }
    }

    @Override
    //Once the user has left an Arrivals activity, update the expandable list and its components
    //This is because a user can modify categories or unsave a bus stop
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
    //If a user has clicked on a bus stop, show them its arrivals screen
    public void onGridViewBusStopClick(BusStop busStop) {
        //todo move to util class
        Intent intent = new Intent(getContext(), BusArrivals.class);

        intent.putExtra("BUS_STOP", busStop);

        startActivityForResult(intent, REQUEST_CODE);
    }

    //todo move to dialog class
    //If user chooses to create a new category, show an alert dialog with an edit text component
    private void addCategoryDialog() {
        //Create alert and link it to our custom dialog
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.text_dialog, null);
        alert.setView(dialogView);
        final AlertDialog alertDialog = alert.create();

        //get and init components
        TextView title = dialogView.findViewById(R.id.input_dialog_title);
        title.setText("Enter new category name: ");
        EditText inputText = dialogView.findViewById(R.id.input_dialog_edit_text);
        Button cancelButton = dialogView.findViewById(R.id.input_dialog_cancel_btn);
        Button submitButton = dialogView.findViewById(R.id.input_dialog_submit_btn);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add new category with the user chosen name
                String categoryName = inputText.getText().toString();
                categoriesManager.addCategory(categoryName);

                //update the expandable list
                prepareListData();
                categoryExpandableListAdapter.setData(categoryNames, categoryChildren);

                //Since the addition of a new category messes with which list is expanded,
                //restore the correct expanded state for each category,
                //then save the state with the new category involved
                loadExpandedState();
                saveExpandedState();

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

    //todo move to dialog class
    //If user chooses to delete a category, show a dialog with all user made categories displayed
    private void removeCategoriesDialog() {
        //Create alert and link it to our custom dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.multi_choice_dialog, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        //get all user made categories and order them by the earliest creation date
        List<String> editableCategories = categoriesManager.getUserCreatedCategories();
        Collections.reverse(editableCategories);

        // Initialize boolean array to track selected categories
        boolean[] checkedItems = new boolean[editableCategories.size()];

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
                //remove each selected category
                for (int i = 0; i < editableCategories.size(); i++) {
                    if (checkedItems[i]) {
                        categoriesManager.removeCategory(editableCategories.get(i));
                    }
                }

                //update the list
                prepareListData();
                categoryExpandableListAdapter.setData(categoryNames, categoryChildren);

                //Since the addition of a new category messes with which list is expanded,
                //restore the correct expanded state for each category,
                //then save the state with the new category involved
                loadExpandedState();
                saveExpandedState();

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

    //todo move to frontend object
    //Save the expanded state of each category and put them into external storage
    private void saveExpandedState() {
        Map<String, Boolean> expandedMap = new HashMap<>();
        Gson gson = new Gson();
        String json;

        //iterate through the categories and record if they are expanded or collapsed
        for (int i = 0; i < categoryExpandableListAdapter.getGroupCount(); i++) {
            expandedMap.put((String) categoryExpandableListAdapter.getGroup(i), categoryListView.isGroupExpanded(i));
        }

        //convert to json and write to the designated file
        json = gson.toJson(expandedMap, new TypeToken<Map<String, Boolean>>() {
        }.getType());

        File file = new File(getContext().getExternalFilesDir(null), EXPANDED_LIST_FILE_NAME);
        try (OutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(json.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //todo move to frontend object
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

                Gson gson = new Gson();
                Map<String, Boolean> expandedGroups = gson.fromJson(jsonString.toString(), new TypeToken<Map<String, Boolean>>() {
                }.getType());

                //now that we have the state, expand/collapse a category according to its state
                for (int i = 0; i < categoryExpandableListAdapter.getGroupCount(); i++) {
                    String categoryName = (String) categoryExpandableListAdapter.getGroup(i);

                    if (expandedGroups.containsKey(categoryName) && Boolean.TRUE.equals(expandedGroups.get(categoryName))) {
                        categoryListView.expandGroup(i);
                    } else {
                        categoryListView.collapseGroup(i);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //todo move to separate methods
    private void initComponents(View rootView) {
        //Get Components
        categoryListView = rootView.findViewById(R.id.SavedStopsExpandableList);
        ImageButton editCategoryBtn = rootView.findViewById(R.id.SavedEditCategoriesBtn);
        categoryExpandableListAdapter = new CategoryExpandableListAdapter(getActivity(), categoryNames, categoryChildren, this, transitService);
        categoryListView.setAdapter(categoryExpandableListAdapter);

        //Set listeners for the buttons
        categoryListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                // This way the expander cannot be collapsed
                // on click event of group item
                return false;
            }
        });

        //show a popup menu onclick and execute the appropriate method once the user has made their choice
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

        //save the expanded state when a group is expanded
        categoryListView.setOnGroupExpandListener((groupPosition) -> saveExpandedState());

        //save the expanded state when a group is collapsed
        categoryListView.setOnGroupCollapseListener((groupPosition) -> saveExpandedState());
    }

}