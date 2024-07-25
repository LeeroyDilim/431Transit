package com.example.a431transit.objects.arrivals;

import com.example.a431transit.R;

import android.content.Context;
import android.graphics.Color;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.a431transit.objects.bus_route.badge_style.BadgeStyle;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;

public class ArrivalInstance {
    private final String ROUTE_BADGE;
    private final BadgeStyle BADGE_STYLE;
    private final String ROUTE_NAME;
    private final LocalDateTime BUS_EXPECTED_ARRIVAL;
    private final LocalDateTime BUS_ACTUAL_ARRIVAL;
    private final boolean IS_CANCELLED;

    public ArrivalInstance(String busBadge, BadgeStyle badgeStyle, String busName, LocalDateTime busExpectedTime, LocalDateTime busActualTime, boolean isCancelled) {
        this.ROUTE_BADGE = busBadge;
        this.BADGE_STYLE = badgeStyle;
        this.ROUTE_NAME = busName;
        this.BUS_EXPECTED_ARRIVAL = busExpectedTime;
        this.BUS_ACTUAL_ARRIVAL = busActualTime;
        this.IS_CANCELLED = isCancelled;
    }

    //Given a text view, modify it with the bus routes corresponding badge and font/background colors
    public void loadRouteBadge(TextView textView) {
        int backgroundColor = android.graphics.Color.parseColor(BADGE_STYLE.getBackgroundColor());
        int textColor = android.graphics.Color.parseColor(BADGE_STYLE.getTextColor());

        //replace standard badge-style colours with colours that are more distinguishable on the screen
        if (backgroundColor == Color.WHITE) {
            backgroundColor = Color.rgb(230, 230, 230);
        }

        textView.setText(ROUTE_BADGE);
        textView.setTextColor(textColor);

        textView.setBackgroundColor(backgroundColor);
    }

    //given a text view, modify it with the route name
    public void loadRouteName(TextView textView) {
        textView.setText(ROUTE_NAME);
    }

    //Given a text view, modify it with the scheduled departure time for this bus
    public void loadBusTime(TextView textView) {
        String timeText;
        LocalDateTime currentTime = LocalDateTime.now();

        Duration duration = Duration.between(currentTime, BUS_ACTUAL_ARRIVAL);

        //If departure time is less than 16 minutes from now, display the departure time in minutes
        if (duration.toMinutes() <= 15) {
            if (duration.toMinutes() <= 0) {
                timeText = "Due";
            } else {
                timeText = duration.toMinutes() + " min";
            }
        } else {
            //convert LocalTime variable to a string format: 8:45 pm
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a");

            timeText = BUS_ACTUAL_ARRIVAL.format(formatter);
        }

        textView.setText(timeText);
    }

    //Given a text view, modify it with the current status of the bus
    public void loadBusStatus(Context context, TextView textView) {
        String status;
        int colorID;
        int textColor;

        //Calculate the current status of the bus and display it with the text view
        if (IS_CANCELLED) {
            status = "CANCELLED";
            colorID = R.color.BAD_STATUS;
        } else if (BUS_ACTUAL_ARRIVAL.isAfter(BUS_EXPECTED_ARRIVAL)) {
            status = "Late";
            colorID = R.color.BAD_STATUS;
        } else if (BUS_ACTUAL_ARRIVAL.isBefore(BUS_EXPECTED_ARRIVAL)) {
            status = "Early";
            colorID = R.color.CAUTIONARY_STATUS;
        } else {
            status = "On Time";
            colorID = R.color.GOOD_STATUS;
        }

        textColor = ContextCompat.getColor(context, colorID);

        textView.setText(status);
        textView.setTextColor(textColor);
    }

    public LocalDateTime getBusActualArrival() {
        return BUS_ACTUAL_ARRIVAL;
    }
}
