package com.example.a431transit.persistence.json;

import android.content.Context;

import com.example.a431transit.objects.bus_stop.BusStop;
import com.example.a431transit.persistence.ISavedStopPersistence;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;import java.util.Map;

public class SavedStopPersistenceJSON implements ISavedStopPersistence {
    private static final String FILE_NAME = "busStops.json";
    private final File FILE_DIR;

    public SavedStopPersistenceJSON(File fileDir) {
        this.FILE_DIR = fileDir;
    }

    @Override
    public Map<String, BusStop> loadBusStops() {
        Map<String, BusStop> stopsMap = new HashMap<>();

        try {
            File file = new File(FILE_DIR, FILE_NAME);

            if (file.exists()) {
                StringBuilder jsonString = new StringBuilder();
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonString.append(line);
                }
                reader.close();

                Type mapType = new TypeToken<Map<String, BusStop>>() {}.getType();
                Gson gson = new GsonBuilder().setLenient().create();
                stopsMap = gson.fromJson(jsonString.toString(), mapType);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load bus stops: " + e.toString());
        }

        return stopsMap != null ? stopsMap : new HashMap<>();
    }

    @Override
    public void saveBusStops(Map<String, BusStop> busStops) {
        Gson gson = new Gson();
        String json = gson.toJson(busStops);

        File file = new File(FILE_DIR, FILE_NAME);
        try (OutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(json.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to save bus stops: " + e.toString());
        }
    }
}
