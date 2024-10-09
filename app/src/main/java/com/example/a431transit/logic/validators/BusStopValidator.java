package com.example.a431transit.logic.validators;

import com.example.a431transit.application.AppConstants;
import com.example.a431transit.objects.bus_stop.BusStop;
import com.google.android.gms.maps.model.LatLng;

public class BusStopValidator {
    public static boolean validateBusStop(BusStop busStop){
        if (busStop.getKey() <= 0){
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
        return query != null && !query.isEmpty();
    }

    public static boolean validateKeyQuery(int query) {
        return query > 0;
    }

    public static boolean validateLocation(LatLng latLng){
        return latLng != null;
    }
}
