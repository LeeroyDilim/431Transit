package com.example.a431transit.objects.bus_arrivals.route_schedules.scheduled_stops;

import com.example.a431transit.objects.bus_arrivals.route_schedules.scheduled_stops.bus.Bus;
import com.example.a431transit.objects.bus_arrivals.route_schedules.scheduled_stops.times.Times;
import com.example.a431transit.objects.bus_arrivals.route_schedules.scheduled_stops.variant.Variant;
import com.google.gson.annotations.SerializedName;

public class ScheduledStop {
    @SerializedName("key")
    private String key;

    @SerializedName("cancelled")
    private boolean cancelled;

    @SerializedName("times")
    private Times times;

    @SerializedName("variant")
    private Variant variant;

    @SerializedName("bus")
    private Bus bus;

    public String getKey() {
        return key;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public Times getTimes() {
        return times;
    }

    public Variant getVariant() {
        return variant;
    }

    public Bus getBus() {
        return bus;
    }

}
