package com.example.a431transit.util.storage_managers;

import android.content.Context;

import com.example.a431transit.objects.bus_stop.BusStop;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CategoriesManager {

    private static final String CATEGORIES_FILE_NAME = "categories.json";
    //track the actual instances of bus stops stored in external storage
    private SavedBusStopManager savedBusStopManager;
    private Map<String, List<String>> categories;
    private Context context;

    public CategoriesManager(Context context) {
        this.context = context;
        categories = loadCategoriesFromJson();
        savedBusStopManager = new SavedBusStopManager(context);
    }

    //if json is changed, then make sure this is reading the most recent json version
    public void update() {
        categories = loadCategoriesFromJson();
        savedBusStopManager.update();
    }

    public List<String> getAllCategories() {
        return new ArrayList<>(categories.keySet());
    }

    public List<String> getUserCreatedCategories() {
        ArrayList<String> output = new ArrayList<>(categories.keySet());

        output.remove("Saved");

        return output;
    }

    //given a category, return a list of bus stops that are in that category
    public List<BusStop> getBusStopsFromCategory(String category) {
        if (!categoryExists(category)) {
            return null;
        }

        List<BusStop> busStops = new ArrayList<>();
        List<String> listOfBusStopKeys = categories.get(category);

        for (String busStopKey : listOfBusStopKeys) {
            busStops.add(savedBusStopManager.getBusStop(busStopKey));
        }

        return busStops;
    }

    //add a given stop to a given category
    public void addStopToCategory(String category, BusStop busStop) {
        if (busStopInCategory(category, busStop)) {
            return;
        }

        //save category in bus stop, then save it onto storage
        busStop.addCategory(category);
        savedBusStopManager.addBusStop(busStop);

        //check if category exists, if not add it
        if (!categoryExists(category)) {
            addCategory(category);
        }

        //save bus stop pointer in category
        categories.get(category).add(String.valueOf(busStop.getKey()));

        //update json file
        saveCategoriesToJson();
    }

    //remove a given stop from a given category
    public void removeStopFromCategory(String category, BusStop busStop) {
        if (!categoryExists(category)) {
            return;
        }

        //remove category in bus stop's category list and update the json file
        busStop.removeCategory(category);

        //if bus stop is not in any category, remove it from storage
        if (busStop.notInAnyCategory()) {
            savedBusStopManager.removeBusStop(busStop);
        } else {
            savedBusStopManager.addBusStop(busStop);
        }

        //remove bus stop pointer from category
        categories.get(category).remove(String.valueOf(busStop.getKey()));

        //update json file
        saveCategoriesToJson();
    }

    public BusStop getBusStop(BusStop busStop) {
        return savedBusStopManager.getBusStop(String.valueOf(busStop.getKey()));
    }

    //replaces a bus stop in storage with an updated version
    public void updateBusStop(BusStop busStop) {
        if (isBusStopSaved(busStop)) {
            savedBusStopManager.addBusStop(busStop);
        }
    }

    public void addCategory(String categoryName) {
        if (!categoryExists(categoryName)) {
            categories.put(categoryName, new ArrayList<>());
            saveCategoriesToJson();
        }
    }

    public void removeCategory(String categoryName) {
        if (categories.containsKey(categoryName)) {
            //for each bus stop in the category, remove that category in the bus stop's category list
            for (String busKey : categories.get(categoryName)) {
                BusStop currentBusStop = savedBusStopManager.getBusStop(busKey);
                currentBusStop.removeCategory(categoryName);

                //remove bus stop from storage if not in any category
                if (currentBusStop.notInAnyCategory()) {
                    savedBusStopManager.removeBusStop(currentBusStop);
                }
            }
        }

        categories.remove(categoryName);

        saveCategoriesToJson();
    }

    public boolean categoryExists(String categoryName) {
        return categories.containsKey(categoryName);
    }

    public boolean busStopInCategory(String categoryName, BusStop busStop) {
        if (!categoryExists(categoryName)) {
            return false;
        }

        return categories.get(categoryName).contains(String.valueOf(busStop.getKey()));
    }

    public boolean isBusStopSaved(BusStop busStop) {
        return savedBusStopManager.isBusStopSaved(busStop);
    }


    //TODO: decouple
    //Load Categories from external storage
    private Map<String, List<String>> loadCategoriesFromJson() {
        Map<String, List<String>> categoriesMap = new LinkedHashMap<>();

        try {
            File file = new File(context.getExternalFilesDir(null), CATEGORIES_FILE_NAME);

            if (file.exists()) {
                // Read the file as a string
                StringBuilder jsonString = new StringBuilder();
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonString.append(line);
                }
                reader.close();

                // Use Gson.fromJson with the read string
                Type mapType = new TypeToken<Map<String, List<String>>>() {
                }.getType();
                Gson gson = new GsonBuilder().setLenient().create();
                categoriesMap = gson.fromJson(jsonString.toString(), mapType);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return categoriesMap != null ? categoriesMap : new HashMap<>();
    }

    //Save Categories into external storage
    private void saveCategoriesToJson() {
        Gson gson = new Gson();
        String json = gson.toJson(categories);

        File file = new File(context.getExternalFilesDir(null), CATEGORIES_FILE_NAME);

        try (OutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(json.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}