package com.example.a431transit.application;

import android.content.Context;

import com.example.a431transit.objects.bus_stop.BusStop;

public class Conversion {
    public static int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dp * density);
    }

    public static String busKeyToRouteCacheKey(BusStop busStop){
        return busStop.getKey() + "route";
    }

    public static String busKeyToImageKey(BusStop busStop, String shape){
        return busStop.getKey() + shape;
    }
}
