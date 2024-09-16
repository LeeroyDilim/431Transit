package com.example.a431transit.persistence;

import com.example.a431transit.objects.bus_stop.BusStop;

import java.util.Map;

public interface ISavedStopPersistence {
    Map<String, BusStop> loadBusStops();
    void saveBusStops(Map<String, BusStop> busStops);
}