package com.example.a431transit.model;

import com.example.a431transit.model.arrivals.StopSchedule;
import com.example.a431transit.model.bus_route.BusRoute;
import com.example.a431transit.model.stops.BusStop;
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
