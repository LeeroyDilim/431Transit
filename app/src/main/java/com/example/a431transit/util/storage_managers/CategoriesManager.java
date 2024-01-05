package com.example.a431transit.util.storage_managers;

import android.content.Context;

import com.example.a431transit.model.arrivals.Bus;
import com.example.a431transit.model.stops.BusStop;
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
import java.util.List;
import java.util.Map;

public class CategoriesManager {

    private static final String CATEGORIES_FILE_NAME = "categories.json";
    private SavedBusStopManager savedBusStopManager;
    private Map<String, List<String>> categories; // Updated data structure
    private Context context;

    public CategoriesManager(Context context) {
        this.context = context;
        categories = loadCategoriesFromJson();
        savedBusStopManager = new SavedBusStopManager(context);
    }

    //if json is changed, then make sure this is reading the most recent json version
    public void update()
    {
        categories = loadCategoriesFromJson();
        savedBusStopManager.update();
    }

    public List<String> getCategories() {
        return new ArrayList<>(categories.keySet());
    }

    public List<BusStop> getBusStopsFromCategory(String category)
    {
        if(!isCategoryExists(category))
        {
            return null;
        }

        List<BusStop> busStops = new ArrayList<>();
        List<String> listOfBusStopKeys = categories.get(category);

        for(String busStopKey : listOfBusStopKeys)
        {
            busStops.add(savedBusStopManager.getBusStop(busStopKey));
        }

        return busStops;
    }

    public void addStopToCategory(String category, BusStop busStop)
    {
        if(!savedBusStopManager.isBusStopSaved(busStop))
        {
            savedBusStopManager.addBusStop(busStop);
        }

        if(!isCategoryExists(category))
        {
            addCategory(category);
        }

        categories.get(category).add(String.valueOf(busStop.getKey()));

        saveCategoriesToJson();
    }

    public void removeStopFromCategory(String category, BusStop busStop)
    {
        if(!isCategoryExists(category))
        {
            return;
        }

        categories.get(category).remove(String.valueOf(busStop.getKey()));

        saveCategoriesToJson();
    }

    public BusStop getBusStop(BusStop busStop)
    {
        return savedBusStopManager.getBusStop(String.valueOf(busStop.getKey()));
    }

    public void addCategory(String categoryName) {
        categories.put(categoryName, new ArrayList<>());
        saveCategoriesToJson();
    }

    public void removeCategory(String categoryName) {
        categories.remove(categoryName);
        saveCategoriesToJson();
    }

    public boolean isCategoryExists(String categoryName) {
        return categories.containsKey(categoryName);
    }

    public boolean isBusStopSaved(BusStop busStop) {
        return savedBusStopManager.isBusStopSaved(busStop);
    }

    private Map<String, List<String>> loadCategoriesFromJson() {
        Map<String, List<String>> categoriesMap = new HashMap<>();

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
                Type mapType = new TypeToken<Map<String, List<String>>>() {}.getType();
                Gson gson = new GsonBuilder().setLenient().create();
                categoriesMap = gson.fromJson(jsonString.toString(), mapType);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return categoriesMap != null ? categoriesMap : new HashMap<>();
    }

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