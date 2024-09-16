package com.example.a431transit.persistence;

import android.graphics.Bitmap;
import android.os.Parcelable;

import com.example.a431transit.objects.bus_route.BusRoute;

import java.util.List;

public interface IBusCache {
    void putRoutes(String key, List<BusRoute> busRoutes);
    List<BusRoute> getRoutes(String key);

    void putImage(String key, Bitmap resource);

    Bitmap getImage(String key);
}
