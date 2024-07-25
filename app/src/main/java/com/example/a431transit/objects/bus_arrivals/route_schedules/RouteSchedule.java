package com.example.a431transit.objects.bus_arrivals.route_schedules;

import com.example.a431transit.objects.bus_arrivals.route_schedules.scheduled_stops.ScheduledStop;
import com.example.a431transit.objects.bus_route.BusRoute;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RouteSchedule {
    @SerializedName("route")
    private BusRoute route;

    @SerializedName("scheduled-stops")
    private List<ScheduledStop> scheduledStops;

    public BusRoute getRoute() {
        return route;
    }

    public List<ScheduledStop> getScheduledStops() {
        return scheduledStops;
    }

}
