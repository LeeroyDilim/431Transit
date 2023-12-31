package com.example.a431transit.model.arrivals;
import com.example.a431transit.model.bus_route.BusRoute;
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
