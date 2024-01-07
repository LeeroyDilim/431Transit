
package com.example.a431transit.util.storage_managers;

import android.content.Context;
import android.util.Log;

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
import java.util.HashMap;
import java.util.Map;

public class SavedBusStopManager {

    private static final String FILE_NAME = "busStops.json";
    private Map<String, BusStop> busStops; // Updated data structure
    private Context context;

    public SavedBusStopManager(Context context) {
        this.context = context;
        busStops = loadBusStopsFromJson();
    }

    //if json is changed, then make sure this is reading the most recent json version
    public void update() {
        busStops = loadBusStopsFromJson();
    }

    //get a bus stop that is stored in storage
    public BusStop getBusStop(String busStopKey) {
        if (busStops.containsKey(busStopKey)) {
            return busStops.get(String.valueOf(busStopKey));
        } else {
            return null;
        }
    }

    public void addBusStop(BusStop busStop) {
        busStops.put(String.valueOf(busStop.getKey()), busStop);
        saveBusStopsToJson();
    }

    public void removeBusStop(BusStop busStop) {
        busStops.remove(String.valueOf(busStop.getKey()));
        saveBusStopsToJson();
    }

    public boolean isBusStopSaved(BusStop busStop) {
        return busStops.containsKey(String.valueOf(busStop.getKey()));
    }

    //load saved bus stops from external storage
    private Map<String, BusStop> loadBusStopsFromJson() {
        Map<String, BusStop> stopsMap = new HashMap<>();

        try {
            File file = new File(context.getExternalFilesDir(null), FILE_NAME);

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
                Type mapType = new TypeToken<Map<String, BusStop>>() {
                }.getType();
                Gson gson = new GsonBuilder().setLenient().create();
                stopsMap = gson.fromJson(jsonString.toString(), mapType);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stopsMap != null ? stopsMap : new HashMap<>();
    }

    //save saved bus stops to external storage
    private void saveBusStopsToJson() {
        Gson gson = new Gson();
        String json = gson.toJson(busStops);

        File file = new File(context.getExternalFilesDir(null), FILE_NAME);
        try (OutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(json.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
