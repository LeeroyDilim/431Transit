package com.example.a431transit.objects.bus_arrivals.route_schedules.scheduled_stops.times;

import com.example.a431transit.objects.bus_arrivals.route_schedules.scheduled_stops.times.arrival_departure.ArrivalDeparture;
import com.google.gson.annotations.SerializedName;

public class Times {
    @SerializedName("arrival")
    private ArrivalDeparture arrival;

    @SerializedName("departure")
    private ArrivalDeparture departure;

    public ArrivalDeparture getArrival() {
        return arrival;
    }

    public ArrivalDeparture getDeparture() {
        return departure;
    }
}
