package com.example.a431transit.presentation.front_end_objects;

import android.content.Context;
import android.widget.ExpandableListView;

import com.example.a431transit.R;
import com.example.a431transit.application.AppConstants;
import com.example.a431transit.util.bus_stop_expandable_list.CategoryExpandableListAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class CategoryList {
    private Context context;
    private ExpandableListView categoryListView;
    private CategoryExpandableListAdapter categoryExpandableListAdapter;

    public CategoryList(ExpandableListView categoryListView, CategoryExpandableListAdapter categoryExpandableListAdapter, Context context){
        this.categoryListView = categoryListView;
        this.categoryExpandableListAdapter = categoryExpandableListAdapter;
        this.context = context;

        categoryListView.setAdapter(categoryExpandableListAdapter);
    }

    //Save the expanded state of each category and put them into external storage
    public void saveExpandedState() {
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

        File file = new File(context.getExternalFilesDir(null), AppConstants.getExpandedListFileName());
        try (OutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(json.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadExpandedState() {
        try {
            File file = new File(context.getExternalFilesDir(null), AppConstants.getExpandedListFileName());

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
}
