package com.example.a431transit.logic;

import com.example.a431transit.application.Services;
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
        return busStops.getOrDefault(busStopKey, null);
    }

    public static void addBusStop(BusStop busStop) {
        busStops.put(Integer.toString(busStop.getKey()), busStop);
        savedStopPersistence.saveBusStops(busStops);
    }

    public static void removeBusStop(BusStop busStop) {
        busStops.remove(Integer.toString(busStop.getKey()));
        savedStopPersistence.saveBusStops(busStops);
    }

    // Update an existing bus stop
    public static void updateBusStop(BusStop busStop) {
        if (busStops.containsKey(Integer.toString(busStop.getKey()))) {
            busStops.put(Integer.toString(busStop.getKey()), busStop);
            savedStopPersistence.saveBusStops(busStops);
        }
    }

    public static boolean isBusStopSaved(BusStop busStop) {
        return busStops.containsKey(Integer.toString(busStop.getKey()));
    }
}
