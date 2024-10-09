package com.example.a431transit.logic;

import android.graphics.Bitmap;
import android.util.Log;

import com.example.a431transit.api.transit_api.TransitAPIClient;
import com.example.a431transit.application.Conversion;
import com.example.a431transit.application.Services;
import com.example.a431transit.logic.validators.BusStopValidator;
import com.example.a431transit.objects.bus_arrivals.route_schedules.RouteSchedule;
import com.example.a431transit.objects.bus_route.BusRoute;
import com.example.a431transit.objects.bus_stop.BusStop;
import com.example.a431transit.objects.exceptions.BadRequestException;
import com.example.a431transit.persistence.IBusCache;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.function.Consumer;

public class BusStopHandler {
    private static final IBusCache BUS_CACHE = Services.getBusCache();

    public static void setBusStopNickname(BusStop busStop, String nickname) {
        if(!BusStopValidator.validateNickname(nickname)){
            throw new BadRequestException("Invalid Bus Stop Nickname!");
        }

        busStop.setNickname(nickname);
        SavedBusStopHandler.updateBusStop(busStop);
    }

    public static void setBusStopFilteredRoutes(BusStop busStop, List<String> filteredRoutes) {
        if(!BusStopValidator.validateBusStop(busStop)){
            Log.e("BusStopHandler","setBusStopFilteredRoutes Invalid BusStop passed!");
            throw new Error("Invalid Parameters Passed");
        }

        busStop.setFilteredRoutes(filteredRoutes);
        SavedBusStopHandler.updateBusStop(busStop);
    }

    public static void fetchBusStopImage(BusStop busStop, String shape, Consumer<Bitmap> cacheOperation, Runnable apiCall) {
        if(!BusStopValidator.validateBusStop(busStop) || !BusStopValidator.validateImageString(shape)){
            Log.e("BusStopHandler","fetchBusStopImage Invalid parameters passed!");
            throw new Error("Invalid Parameters Passed");
        }

        //check cache for image
        Bitmap cachedImage = BUS_CACHE.getImage(Conversion.busKeyToImageKey(busStop, shape));

        if (cachedImage != null) {
            cacheOperation.accept(cachedImage);
        } else {
            apiCall.run();
        }
    }

    public static void fetchBusStopByLocation(LatLng location, Consumer<List<BusStop>> onSuccess) {
        if(!BusStopValidator.validateLocation(location)){
            Log.e("BusStopHandler","fetchBusStopByLocation Invalid parameters passed!");
            throw new Error("Invalid Parameters Passed");
        }

        TransitAPIClient.fetchBusStopsByLocation(location, onSuccess);
    }

    public static void fetchBusStopsByName(String query, Consumer<List<BusStop>> onSuccess) {
        if(!BusStopValidator.validateStringQuery(query)){
            throw new BadRequestException("Invalid Search Query");
        }

        TransitAPIClient.fetchBusStopsByName(query, onSuccess);
    }

    public static void fetchBusStopsByKey(int query, Consumer<List<BusStop>> onSuccess) {
        if(!BusStopValidator.validateKeyQuery(query)){
            throw new BadRequestException("Invalid Search Query");
        }

        TransitAPIClient.fetchBusStopsByKey(query, onSuccess);
    }

    public static void fetchBusRoutes(BusStop busStop, Consumer<List<BusRoute>> onSuccess) {
        if(!BusStopValidator.validateBusStop(busStop)){
            Log.e("BusStopHandler","fetchBusRoutes Invalid parameters passed!");
            throw new Error("Invalid Parameters Passed");
        }

        //check cache
        List<BusRoute> routeCache = BUS_CACHE.getRoutes(Conversion.busKeyToRouteCacheKey(busStop));

        if (routeCache != null) {
            onSuccess.accept(routeCache);
            return;
        }

        // If not in cache, run API call
        TransitAPIClient.fetchBusStopRoutes(busStop, onSuccess);
    }

    public static void fetchBusStopSchedule(BusStop busStop, Consumer<List<RouteSchedule>> onSuccess) {
        if(!BusStopValidator.validateBusStop(busStop)){
            Log.e("BusStopHandler","fetchBusStopSchedule Invalid parameters passed!");
            throw new Error("Invalid Parameters Passed");
        }

        TransitAPIClient.fetchBusStopSchedule(busStop, onSuccess);
    }
}
