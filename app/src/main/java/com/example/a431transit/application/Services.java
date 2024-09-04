package com.example.a431transit.application;

import com.example.a431transit.persistence.CategoryPersistence;
import com.example.a431transit.persistence.SavedStopPersistence;
import com.example.a431transit.persistence.TransitAPI;
import com.example.a431transit.persistence.json.CategoryPersistenceJSON;
import com.example.a431transit.persistence.json.SavedStopPersistenceJSON;
import com.example.a431transit.persistence.transit_api_v3.TransitAPIV3;

public class Services {
    private static CategoryPersistence categoryPersistence = null;
    private static SavedStopPersistence savedStopPersistence = null;
    private static TransitAPI transitAPI = null;

    public static CategoryPersistence getCategoryPersistence() {
        if (categoryPersistence == null) {
            categoryPersistence = new CategoryPersistenceJSON();
        }

        return categoryPersistence;
    }

    public static SavedStopPersistence getSavedStopPersistence() {
        if (savedStopPersistence == null) {
            savedStopPersistence = new SavedStopPersistenceJSON();
        }
        return savedStopPersistence;
    }

    public static TransitAPI getTransitAPI() {
        if (transitAPI == null) {
            transitAPI = new TransitAPIV3();
        }

        return transitAPI;
    }
}
