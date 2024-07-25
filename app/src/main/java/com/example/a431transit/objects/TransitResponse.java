package com.example.a431transit.objects;

import com.example.a431transit.objects.bus_arrivals.StopSchedule;
import com.example.a431transit.objects.bus_route.BusRoute;
import com.example.a431transit.objects.bus_stop.BusStop;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TransitResponse {
    @SerializedName("stops")
    private List<BusStop> stops;

    @SerializedName("stop")
    private BusStop stop;

    @SerializedName("routes")
    private List<BusRoute> busRoutes;

    @SerializedName("route")
    private BusRoute busRoute;

    @SerializedName("stop-schedule")
    private StopSchedule stopSchedule;

    @SerializedName("query-time")
    private String queryTime;

    // Getters and setters
    public List<BusStop> getStops() {
        return stops;
    }

    public String getQueryTime() {
        return queryTime;
    }

    public BusStop getStop() {
        return stop;
    }

    public List<BusRoute> getBusRoutes() {
        return busRoutes;
    }

    public BusRoute getBusRoute() {
        return busRoute;
    }

    public StopSchedule getStopSchedule() {
        return stopSchedule;
    }
}
