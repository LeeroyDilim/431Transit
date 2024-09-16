package com.example.a431transit.application;

import com.example.a431transit.persistence.IBusCache;
import com.example.a431transit.persistence.ICategoriesPersistence;
import com.example.a431transit.persistence.ISavedStopPersistence;
import com.example.a431transit.persistence.LRUCache.BusCache;
import com.example.a431transit.persistence.json.CategoryPersistenceJSON;
import com.example.a431transit.persistence.json.SavedStopPersistenceJSON;

public class Services {
    private static ICategoriesPersistence categoryPersistence = null;
    private static ISavedStopPersistence savedStopPersistence = null;
    private static IBusCache busCache = null;

    public static ICategoriesPersistence getCategoryPersistence() {
        if (categoryPersistence == null) {
            categoryPersistence = new CategoryPersistenceJSON(AppConstants.getFileDir());
        }

        return categoryPersistence;
    }

    public static ISavedStopPersistence getSavedStopPersistence() {
        if (savedStopPersistence == null) {
            savedStopPersistence = new SavedStopPersistenceJSON(AppConstants.getFileDir());
        }
        return savedStopPersistence;
    }

    public static IBusCache getBusCache(){
        if(busCache == null){
            busCache = new BusCache();
        }
        return busCache;
    }
}
