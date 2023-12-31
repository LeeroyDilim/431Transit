package com.example.a431transit.model.arrivals;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ArrivalDeparture {
    @SerializedName("scheduled")
    private String scheduled;

    @SerializedName("estimated")
    private String estimated;

    public LocalTime getScheduled() {
        return getLocalTime(scheduled);
    }

    public LocalTime getEstimated() {
        return getLocalTime(estimated);
    }

    private LocalTime getLocalTime(String dateTimeString)
    {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, formatter);

        LocalTime time = dateTime.toLocalTime();

        return time;
    }

}
