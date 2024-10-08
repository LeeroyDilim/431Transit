package com.example.a431transit.objects.bus_arrivals.route_schedules.scheduled_stops.bus;

import com.google.gson.annotations.SerializedName;

public class Bus {

    @SerializedName("key")
    private int key;

    @SerializedName("bike-rack")
    private String bikeRack;

    @SerializedName("wifi")
    private String wifi;

    public int getKey() {
        return key;
    }

    public String getBikeRack() {
        return bikeRack;
    }

    public String getWifi() {
        return wifi;
    }
}
