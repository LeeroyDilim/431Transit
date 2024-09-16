package com.example.a431transit.logic;

import com.example.a431transit.objects.bus_stop.BusStop;

public class Validator {
    public static void validateString(String str, String paramName) {
        if (str == null || str.isEmpty()) {
            throw new IllegalArgumentException(paramName + " cannot be null or empty");
        }
    }

    public static void validateBusStop(BusStop busStop) {
        if (busStop == null) {
            throw new IllegalArgumentException("BusStop cannot be null");
        }
    }
}