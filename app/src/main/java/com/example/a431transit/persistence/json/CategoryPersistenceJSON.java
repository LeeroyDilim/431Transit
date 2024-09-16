package com.example.a431transit.persistence.json;
import android.content.Context;

import com.example.a431transit.application.Services;
import com.example.a431transit.logic.BusStopHandler;
import com.example.a431transit.logic.SavedBusStopHandler;
import com.example.a431transit.objects.bus_stop.BusStop;
import com.example.a431transit.persistence.ICategoriesPersistence;
import com.example.a431transit.persistence.ISavedStopPersistence;
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

public class CategoryPersistenceJSON implements ICategoriesPersistence {
    private static final String CATEGORIES_FILE_NAME = "categories.json";
    private final File FILE_DIR;
    private Map<String, List<String>> categories;

    public CategoryPersistenceJSON(File fileDir) {
        this.FILE_DIR =  fileDir;
        categories = loadCategoriesFromJson();
    }

    @Override
    public void update() {
        categories = loadCategoriesFromJson();
        SavedBusStopHandler.update();
    }

    @Override
    public List<String> getAllCategories() {
        return new ArrayList<>(categories.keySet());
    }

    @Override
    public List<String> getUserCreatedCategories() {
        ArrayList<String> output = new ArrayList<>(categories.keySet());
        output.remove("Saved");
        return output;
    }

    @Override
    public List<BusStop> getBusStopsFromCategory(String category) {
        if (!categoryExists(category)) {
            return null;
        }

        List<BusStop> busStops = new ArrayList<>();
        List<String> listOfBusStopKeys = categories.get(category);

        for (String busStopKey : listOfBusStopKeys) {
            busStops.add(SavedBusStopHandler.getBusStop(busStopKey));
        }

        return busStops;
    }

    @Override
    public void addStopToCategory(String category, BusStop busStop) {
        if (busStopInCategory(category, busStop)) {
            return;
        }

        busStop.addCategory(category);
        SavedBusStopHandler.addBusStop(busStop);

        if (!categoryExists(category)) {
            addCategory(category);
        }

        categories.get(category).add(String.valueOf(busStop.getKey()));
        saveCategoriesToJson();
    }

    @Override
    public void removeStopFromCategory(String category, BusStop busStop) {
        if (!categoryExists(category)) {
            return;
        }

        busStop.removeCategory(category);

        if (busStop.notInAnyCategory()) {
            SavedBusStopHandler.removeBusStop(busStop);
        } else {
            SavedBusStopHandler.addBusStop(busStop);
        }

        categories.get(category).remove(String.valueOf(busStop.getKey()));
        saveCategoriesToJson();
    }

    @Override
    public void updateBusStop(BusStop busStop) {
        if (SavedBusStopHandler.isBusStopSaved(busStop)) {
            SavedBusStopHandler.addBusStop(busStop);
        }
    }

    @Override
    public void addCategory(String categoryName) {
        if (!categoryExists(categoryName)) {
            categories.put(categoryName, new ArrayList<>());
            saveCategoriesToJson();
        }
    }

    @Override
    public void removeCategory(String categoryName) {
        if (categories.containsKey(categoryName)) {
            for (String busKey : categories.get(categoryName)) {
                BusStop currentBusStop = SavedBusStopHandler.getBusStop(busKey);
                currentBusStop.removeCategory(categoryName);

                if (currentBusStop.notInAnyCategory()) {
                    SavedBusStopHandler.removeBusStop(currentBusStop);
                }
            }
        }

        categories.remove(categoryName);
        saveCategoriesToJson();
    }

    @Override
    public boolean categoryExists(String categoryName) {
        return categories.containsKey(categoryName);
    }

    @Override
    public boolean busStopInCategory(String categoryName, BusStop busStop) {
        if (!categoryExists(categoryName)) {
            return false;
        }

        return categories.get(categoryName).contains(String.valueOf(busStop.getKey()));
    }

    private Map<String, List<String>> loadCategoriesFromJson() {
        Map<String, List<String>> categoriesMap = new LinkedHashMap<>();

        try {
            File file = new File(FILE_DIR, CATEGORIES_FILE_NAME);

            if (file.exists()) {
                StringBuilder jsonString = new StringBuilder();
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonString.append(line);
                }
                reader.close();

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

        File file = new File(FILE_DIR, CATEGORIES_FILE_NAME);

        try (OutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(json.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
