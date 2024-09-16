package com.example.a431transit.persistence;

import com.example.a431transit.objects.bus_stop.BusStop;

import java.util.List;

public interface ICategoriesPersistence {

    void update();

    List<String> getAllCategories();

    List<String> getUserCreatedCategories();

    List<BusStop> getBusStopsFromCategory(String category);

    void addStopToCategory(String category, BusStop busStop);

    void removeStopFromCategory(String category, BusStop busStop);

    void updateBusStop(BusStop busStop);

    void addCategory(String categoryName);

    void removeCategory(String categoryName);

    boolean categoryExists(String categoryName);

    boolean busStopInCategory(String categoryName, BusStop busStop);

}
