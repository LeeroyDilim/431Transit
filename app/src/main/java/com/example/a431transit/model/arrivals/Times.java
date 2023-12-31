package com.example.a431transit.model.arrivals;

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
