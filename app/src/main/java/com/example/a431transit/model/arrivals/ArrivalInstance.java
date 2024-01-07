package com.example.a431transit.model.arrivals;

import com.example.a431transit.R;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.a431transit.model.bus_route.BadgeStyle;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;

public class ArrivalInstance {
    private final String ROUTE_BADGE;
    private final BadgeStyle BADGE_STYLE;
    private final String ROUTE_NAME;
    private final LocalDateTime BUS_EXPECTED_ARRIVAL;
    private final LocalDateTime BUS_ACTUAL_ARRIVAL;
    private String BUS_STATUS;
    private final boolean IS_CANCELLED;

    public ArrivalInstance(String busBadge, BadgeStyle badgeStyle, String busName, LocalDateTime busExpectedTime, LocalDateTime busActualTime, boolean isCancelled) {
        this.ROUTE_BADGE = busBadge;
        this.BADGE_STYLE = badgeStyle;
        this.ROUTE_NAME = busName;
        this.BUS_EXPECTED_ARRIVAL = busExpectedTime;
        this.BUS_ACTUAL_ARRIVAL = busActualTime;
        this.IS_CANCELLED = isCancelled;
    }

    public void loadRouteBadge(TextView textView) {
        int backgroundColor = android.graphics.Color.parseColor(BADGE_STYLE.getBackgroundColor());
        int textColor = android.graphics.Color.parseColor(BADGE_STYLE.getTextColor());

        //replace standard badge-style colours with colours that are easier on the eyes
        if (backgroundColor == Color.WHITE) {
            backgroundColor = Color.rgb(230, 230, 230);
        }

        textView.setText(ROUTE_BADGE);
        textView.setTextColor(textColor);

        textView.setBackgroundColor(backgroundColor);
    }

    public void loadRouteName(TextView textView) {
        textView.setText(ROUTE_NAME);
    }

    public void loadBusTime(TextView textView) {
        String timeText;
        LocalDateTime currentTime = LocalDateTime.now();

        Duration duration = Duration.between(currentTime, BUS_ACTUAL_ARRIVAL);

        Log.i("Arrival Instance", "SCHEDULED TIME: " + BUS_ACTUAL_ARRIVAL + "\nCURRENT TIME: " + currentTime + "\nDURATION: " + duration);
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

    public void loadBusStatus(Context context, TextView textView) {
        String status;
        int colorID;
        int textColor;

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
