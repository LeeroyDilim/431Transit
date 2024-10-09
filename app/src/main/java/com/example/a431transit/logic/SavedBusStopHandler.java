package com.example.a431transit.logic;

import android.util.Log;

import com.example.a431transit.application.Services;
import com.example.a431transit.logic.validators.BusStopValidator;
import com.example.a431transit.objects.bus_stop.BusStop;
import com.example.a431transit.persistence.ISavedStopPersistence;

import java.util.Map;

public class SavedBusStopHandler {
    private static Map<String, BusStop> busStops;
    private static ISavedStopPersistence savedStopPersistence = Services.getSavedStopPersistence();

    // if JSON is changed, then make sure this is reading the most recent JSON version
    public static void update() {
        busStops = savedStopPersistence.loadBusStops();
    }

    // get a bus stop that is stored in storage
    public static BusStop getBusStop(String busStopKey) {
        if(!BusStopValidator.validateBusStopKey(busStopKey)){
            Log.e("BusStopHandler","Invalid key passed!");
        }

        return busStops.getOrDefault(busStopKey, null);
    }

    public static void addBusStop(BusStop busStop) {
        if(!BusStopValidator.validateBusStop(busStop)){
            Log.e("BusStopHandler","Invalid bus stop passed!");
        }

        busStops.put(Integer.toString(busStop.getKey()), busStop);
        savedStopPersistence.saveBusStops(busStops);
    }

    public static void removeBusStop(BusStop busStop) {
        if(!BusStopValidator.validateBusStop(busStop)){
            Log.e("BusStopHandler","Invalid bus stop passed!");
        }

        busStops.remove(Integer.toString(busStop.getKey()));
        savedStopPersistence.saveBusStops(busStops);
    }

    // Update an existing bus stop
    public static void updateBusStop(BusStop busStop) {
        if(!BusStopValidator.validateBusStop(busStop)){
            Log.e("BusStopHandler","Invalid bus stop passed!");
        }

        if (busStops.containsKey(Integer.toString(busStop.getKey()))) {
            busStops.put(Integer.toString(busStop.getKey()), busStop);
            savedStopPersistence.saveBusStops(busStops);
        }
    }

    public static boolean isBusStopSaved(BusStop busStop) {
        if(!BusStopValidator.validateBusStop(busStop)){
            Log.e("BusStopHandler","Invalid bus stop passed!");
        }

        return busStops.containsKey(Integer.toString(busStop.getKey()));
    }
}
