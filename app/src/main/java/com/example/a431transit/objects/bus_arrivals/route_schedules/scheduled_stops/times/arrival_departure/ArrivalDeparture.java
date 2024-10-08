package com.example.a431transit.objects.bus_arrivals.route_schedules.scheduled_stops.times.arrival_departure;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ArrivalDeparture {
    @SerializedName("scheduled")
    private String scheduled;

    @SerializedName("estimated")
    private String estimated;

    public LocalDateTime getScheduled() {
        return getLocalTime(scheduled);
    }

    public LocalDateTime getEstimated() {
        return getLocalTime(estimated);
    }

    //get LocalTime from String
    private LocalDateTime getLocalTime(String dateTimeString) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, formatter);

        return dateTime;
    }

}
