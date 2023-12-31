package com.example.a431transit.model.arrivals;

import com.example.a431transit.model.stops.BusStop;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class StopSchedule {
    @SerializedName("stop")
    private BusStop stop;

    @SerializedName("route-schedules")
    private List<RouteSchedule> routeSchedules;

    public BusStop getStop() {
        return stop;
    }

    public List<RouteSchedule> getRouteSchedules() {
        return routeSchedules;
    }

}
