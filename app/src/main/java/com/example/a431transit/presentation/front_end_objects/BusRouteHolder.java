package com.example.a431transit.presentation.front_end_objects;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.a431transit.R;
import com.example.a431transit.objects.bus_arrivals.route_schedules.scheduled_stops.bus.Bus;
import com.example.a431transit.objects.bus_route.BusRoute;
import com.example.a431transit.objects.bus_stop.BusStop;

import java.util.List;

public class BusRouteHolder {
    private final Context context;
    private BusStop busStop;
    private final ViewGroup layout;

    public BusRouteHolder(Context context, BusStop busStop, ViewGroup layout) {
        this.context = context;
        this.busStop = busStop;
        this.layout = layout;
    }

    //For each bus route, create a text view and insert it into the given layout
    public void updateRouteView(List<BusRoute> busRoutes) {
        List<String> filteredRoutes = busStop.getFilteredRoutes();

        if (busRoutes == null) {
            return;
        }

        //remove the previously displayed routes
        layout.removeAllViews();

        for (int i = 0; i < busRoutes.size(); i++) {
            BusRoute busRoute = busRoutes.get(i);

            TextView newText = createRouteTextView(context, busRoute);

            //remove right margin of the last text view so it aligns nicely
            if (i == busRoutes.size() - 1) {
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) newText.getLayoutParams();

                if (layoutParams == null) {
                    layoutParams = new ViewGroup.MarginLayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );
                }

                layoutParams.rightMargin = 0;
                layoutParams.bottomMargin = 0;

                newText.setLayoutParams(layoutParams);
            }

            //If user has chosen to not display a route, make it mostly transparent
            if (filteredRoutes != null && !filteredRoutes.contains(busRoute.getKey())) {
                newText.setAlpha(0.35f);
            }

            layout.addView(newText);
        }
    }

    //Create a text view with the bus route's properties
    private static TextView createRouteTextView(Context context, BusRoute busRoute) {
        LayoutInflater inflater = LayoutInflater.from(context);
        TextView output = (TextView) inflater.inflate(R.layout.bus_route_text_view, null);

        //get the badge style of this route instance
        int backgroundColor = android.graphics.Color.parseColor(busRoute.getBadgeStyle().getBackgroundColor());
        int textColor = android.graphics.Color.parseColor(busRoute.getBadgeStyle().getTextColor());

        //replace standard badge-style colours with colours that are easier on the eyes
        if (backgroundColor == Color.WHITE) {
            backgroundColor = Color.rgb(230, 230, 230);
        }

        //set margins for this text view
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) output.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        layoutParams.rightMargin = 20;
        layoutParams.bottomMargin = 20;
        output.setLayoutParams(layoutParams);

        //set the attributes for this text view
        output.setId(View.generateViewId());
        output.setText(busRoute.getNumber());
        output.setTextColor(textColor);
        output.setBackgroundColor(backgroundColor);

        return output;
    }
}
