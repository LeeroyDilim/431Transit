package com.example.a431transit.logic;

import android.graphics.Bitmap;

import com.example.a431transit.api.transit_api.TransitAPIClient;
import com.example.a431transit.application.Conversion;
import com.example.a431transit.application.Services;
import com.example.a431transit.objects.bus_arrivals.route_schedules.RouteSchedule;
import com.example.a431transit.objects.bus_route.BusRoute;
import com.example.a431transit.objects.bus_stop.BusStop;
import com.example.a431transit.persistence.IBusCache;
import com.example.a431transit.logic.Validator;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.function.Consumer;

public class BusStopHandler {
    private static final IBusCache BUS_CACHE = Services.getBusCache();

    public static void setBusStopNickname(BusStop busStop, String nickname) {
        Validator.validateString(nickname, "Nickname");

        busStop.setNickname(nickname);
        SavedBusStopHandler.updateBusStop(busStop);
    }

    public static void setBusStopFilteredRoutes(BusStop busStop, List<String> filteredRoutes) {
        Validator.validateBusStop(busStop);
        busStop.setFilteredRoutes(filteredRoutes);
        SavedBusStopHandler.updateBusStop(busStop);
    }

    public static void fetchBusStopImage(BusStop busStop, String shape, Consumer<Bitmap> cacheOperation, Runnable apiCall) {
        Validator.validateBusStop(busStop);
        Validator.validateString(shape, "Shape");

        //check cache for image
        Bitmap cachedImage = BUS_CACHE.getImage(Conversion.busKeyToImageKey(busStop, shape));

        if (cachedImage != null) {
            cacheOperation.accept(cachedImage);
        } else {
            apiCall.run();
        }
    }

    public static void fetchBusStopByLocation(LatLng location, Consumer<List<BusStop>> onSuccess, Consumer<String> onError) {
        TransitAPIClient.fetchBusStopsByLocation(location, onSuccess, onError);
    }

    public static void fetchBusStopsByName(String query, Consumer<List<BusStop>> onSuccess, Consumer<String> onError) {
        Validator.validateString(query, "Query");
        TransitAPIClient.fetchBusStopsByName(query, onSuccess, onError);
    }

    public static void fetchBusStopsByKey(int query, Consumer<List<BusStop>> onSuccess, Consumer<String> onError) {
        TransitAPIClient.fetchBusStopsByKey(query, onSuccess, onError);
    }

    public static void fetchBusRoutes(BusStop busStop, Consumer<List<BusRoute>> onSuccess, Consumer<String> onError) {
        Validator.validateBusStop(busStop);

        //check cache
        List<BusRoute> routeCache = BUS_CACHE.getRoutes(Conversion.busKeyToRouteCacheKey(busStop));

        if (routeCache != null) {
            onSuccess.accept(routeCache);
            return;
        }

        // If not in cache, run API call
        TransitAPIClient.fetchBusStopRoutes(busStop, onSuccess, onError);
    }

    public static void fetchBusStopSchedule(BusStop busStop, Consumer<List<RouteSchedule>> onSuccess, Consumer<String> onError) {
        Validator.validateBusStop(busStop);
        TransitAPIClient.fetchBusStopSchedule(busStop, onSuccess, onError);
    }
}
