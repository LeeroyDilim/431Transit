package com.example.a431transit.persistence.LRUCache;

import android.graphics.Bitmap;
import android.util.LruCache;

import com.example.a431transit.objects.bus_route.BusRoute;
import com.example.a431transit.persistence.IBusCache;

import java.util.List;

public class BusCache implements IBusCache {
    private static final LruCache<String, List<BusRoute>> routeCache = new LruCache<>(10 * 1024 * 1024);

    //store bus stop images in a cache
    private static final LruCache<String, Bitmap> imageCache = new LruCache<>(10 * 1024 * 1024);

    @Override
    public void putRoutes(String key, List<BusRoute> busRoutes) {
        routeCache.put(key, busRoutes);
    }

    @Override
    public List<BusRoute> getRoutes(String key) {
        return routeCache.get(key);
    }

    @Override
    public void putImage(String key, Bitmap resource) {
        imageCache.put(key,resource);
    }

    @Override
    public Bitmap getImage(String key) {
        return imageCache.get(key);
    }

}
