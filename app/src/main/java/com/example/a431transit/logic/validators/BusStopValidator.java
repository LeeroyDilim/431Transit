package com.example.a431transit.logic.validators;

import com.example.a431transit.application.AppConstants;
import com.example.a431transit.objects.bus_stop.BusStop;
import com.google.android.gms.maps.model.LatLng;

public class BusStopValidator {
    public static boolean validateBusStop(BusStop busStop){
        //all separated into separate check blocks so its easier for me to change later on.

        if (busStop.getKey() <= 0){
            return false;
        }

        if (busStop.getNumber() <= 0){
            return false;
        }

        if (busStop.getName() == null || busStop.getName().isEmpty()){
            return false;
        }

        if (busStop.getDirection() == null || busStop.getDirection().isEmpty()){
            return false;
        }

        if (busStop.getSide() == null) {
            return false;
        }

        if (busStop.getCentre() == null){
            return false;
        }

        if (busStop.getStreet() == null){
            return false;
        }

        if (busStop.getCrossSteet() == null){
            return false;
        }

        if (busStop.getCategories() == null){
            return false;
        }

        if (busStop.getFilteredRoutes() == null){
            return false;
        }

        return true;
    }

    public static boolean validateBusStopKey(String busStopKey) {
        return busStopKey != null && !busStopKey.isEmpty() && busStopKey.matches("\\d+");
    }

    public static boolean validateNickname(String nickname) {
        return nickname != null && !nickname.isEmpty();
    }

    public static boolean validateImageString(String shape) {
        return shape.equals(AppConstants.RectangleImage.NAME) || shape.equals(AppConstants.CircleImage.NAME);
    }

    public static boolean validateStringQuery(String query) {
        return query != null && !query.isEmpty() && query.matches("\\d+");
    }

    public static boolean validateIntQuery(int query) {
        return query > 0;
    }

    public static boolean validateLocation(LatLng latLng){
        return latLng != null;
    }
}
