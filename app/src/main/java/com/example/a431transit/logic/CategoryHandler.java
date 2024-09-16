package com.example.a431transit.logic;

import com.example.a431transit.application.Services;
import com.example.a431transit.objects.bus_stop.BusStop;
import com.example.a431transit.persistence.ICategoriesPersistence;

import java.util.List;

public class CategoryHandler {
    private static ICategoriesPersistence categoriesPersistence = Services.getCategoryPersistence();

    public static void updateCategories() {
        categoriesPersistence.update();
    }

    public static List<String> getAllCategories() {
        return categoriesPersistence.getAllCategories();
    }

    public static List<String> getUserCreatedCategories() {
        return categoriesPersistence.getUserCreatedCategories();
    }

    public static List<BusStop> getBusStopsFromCategory(String category) {
        return categoriesPersistence.getBusStopsFromCategory(category);
    }

    public static void addStopToCategory(String category, BusStop busStop) {
        categoriesPersistence.addStopToCategory(category, busStop);
    }

    public static void removeStopFromCategory(String category, BusStop busStop) {
        categoriesPersistence.removeStopFromCategory(category, busStop);
    }

    public static void addCategory(String categoryName) {
        categoriesPersistence.addCategory(categoryName);
    }

    public static void removeCategory(String categoryName) {
        categoriesPersistence.removeCategory(categoryName);
    }

    public static boolean isBusStopInCategory(String categoryName, BusStop busStop) {
        return categoriesPersistence.busStopInCategory(categoryName, busStop);
    }
}
