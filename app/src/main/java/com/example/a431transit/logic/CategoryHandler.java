package com.example.a431transit.logic;

import android.util.Log;

import com.example.a431transit.application.Services;
import com.example.a431transit.logic.validators.BusStopValidator;
import com.example.a431transit.logic.validators.CategoryValidator;
import com.example.a431transit.objects.bus_stop.BusStop;
import com.example.a431transit.objects.exceptions.BadRequestException;
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
        if(!CategoryValidator.validateCategoryName(category)){
            Log.e("CategoryHandler","getBusStopsFromCategory Invalid parameters passed!");
            throw new BadRequestException("Invalid Parameters Passed");
        }
        return categoriesPersistence.getBusStopsFromCategory(category);
    }

    public static void addStopToCategory(String category, BusStop busStop) {
        if(!CategoryValidator.validateCategoryName(category) || !BusStopValidator.validateBusStop(busStop)){
            Log.e("CategoryHandler","addStopToCategory Invalid parameters passed!");
            throw new BadRequestException("Invalid Parameters Passed");
        }

        categoriesPersistence.addStopToCategory(category, busStop);
    }

    public static void removeStopFromCategory(String category, BusStop busStop) {
        if(!CategoryValidator.validateCategoryName(category) || !BusStopValidator.validateBusStop(busStop)){
            Log.e("CategoryHandler","removeStopFromCategory Invalid parameters passed!");
            throw new BadRequestException("Invalid Parameters Passed");
        }
        categoriesPersistence.removeStopFromCategory(category, busStop);
    }

    public static void addCategory(String category) {
        if(!CategoryValidator.validateCategoryName(category)){
            Log.e("CategoryHandler","addCategory Invalid parameters passed!");
            throw new BadRequestException("Invalid Parameters Passed");

        }
        categoriesPersistence.addCategory(category);
    }

    public static void removeCategory(String category) {
        if(!CategoryValidator.validateCategoryName(category)){
            Log.e("CategoryHandler","removeCategory Invalid parameters passed!");
            throw new BadRequestException("Invalid Parameters Passed");
        }
        categoriesPersistence.removeCategory(category);
    }

    public static boolean isBusStopInCategory(String category, BusStop busStop) {
        if(!CategoryValidator.validateCategoryName(category) || !BusStopValidator.validateBusStop(busStop)){
            Log.e("CategoryHandler","isBusStopInCategory Invalid parameters passed!");
            throw new BadRequestException("Invalid Parameters Passed");
        }

        return categoriesPersistence.busStopInCategory(category, busStop);
    }
}
