package com.example.a431transit.objects.arrivals;

import com.example.a431transit.objects.arrivals.route_schedules.RouteSchedule;
import com.example.a431transit.objects.stops.BusStop;
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
