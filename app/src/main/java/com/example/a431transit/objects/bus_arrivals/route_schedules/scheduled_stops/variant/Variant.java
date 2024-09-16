package com.example.a431transit.objects.bus_arrivals.route_schedules.scheduled_stops.variant;

import com.google.gson.annotations.SerializedName;

public class Variant {

    @SerializedName("key")
    private String key;

    @SerializedName("name")
    private String name;

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }
}
