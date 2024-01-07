package com.example.a431transit.util.bus_stop_list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a431transit.R;
import com.example.a431transit.model.stops.BusStop;
import com.example.a431transit.util.api_communication.TransitAPIService;
import com.google.android.flexbox.FlexboxLayout;

import java.util.List;

public class BusStopAdapter extends RecyclerView.Adapter<BusStopAdapter.BusStopViewHolder> {
    private final BusStopViewInterface busStopViewInterface;

    Context context;
    List<BusStop> busStops;
    FlexboxLayout flexboxLayout;
    TransitAPIService transitService;

    public BusStopAdapter(BusStopViewInterface busStopViewInterface, Context context, List<BusStop> busStops, TransitAPIService transitService) {
        this.busStopViewInterface = busStopViewInterface;
        this.context = context;
        this.busStops = busStops;
        this.transitService = transitService;
        setHasStableIds(true);
    }

    public void updateData(List<BusStop> newBusStops) {
        this.busStops = newBusStops;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public BusStopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BusStopViewHolder(LayoutInflater.from(context).inflate(R.layout.bus_stop_view, parent, false), busStopViewInterface);
    }

    @Override
    //Display the bus stop's information with it's corresponding view holder
    public void onBindViewHolder(@NonNull BusStopViewHolder holder, int position) {
        BusStop currentBusStop = busStops.get(position);

        String busName = currentBusStop.getName();
        int busKey = currentBusStop.getKey(); // Assuming busKey is an int

        holder.busNameView.setText(busName);

        // Convert busKey to String before setting it to TextView
        holder.busKeyView.setText("#" + busKey);

        //load bus stop image and display it
        currentBusStop.loadImage(context, holder.imageView, "circle");

        //display the routes that stop at this stop
        flexboxLayout = holder.busRouteView;
        currentBusStop.loadBusRoutes(context, transitService, flexboxLayout);

        // Add a ViewTreeObserver to get the dimensions after the layout pass
        holder.itemView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                // Remove the listener to avoid continuous callbacks
                holder.itemView.getViewTreeObserver().removeOnPreDrawListener(this);

                //set min height so that it fills the rest of the space available
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) holder.constraintLayout.getLayoutParams();
                holder.constraintLayout.setMinHeight(holder.imageView.getHeight() - holder.busNameView.getHeight() - holder.busKeyView.getHeight() - layoutParams.topMargin);

                return true;
            }
        });
    }


    @Override
    public int getItemCount() {
        if (busStops != null)
            return busStops.size();
        else {
            return 0;
        }

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public static class BusStopViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView busNameView, busKeyView;
        FlexboxLayout busRouteView;
        RelativeLayout relativeLayout;
        ConstraintLayout constraintLayout;

        public BusStopViewHolder(@NonNull View itemView, BusStopViewInterface busStopViewInterface) {
            super(itemView);
            relativeLayout = itemView.findViewById(R.id.bus_stop_view_layout);
            imageView = itemView.findViewById(R.id.bus_stop_image_view);
            busNameView = itemView.findViewById(R.id.bus_stop_text_view);
            busKeyView = itemView.findViewById(R.id.bus_stop_key_view);
            busRouteView = itemView.findViewById(R.id.bus_stop_routes_view);
            constraintLayout = itemView.findViewById(R.id.bus_stop_routes_constraint_view);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (busStopViewInterface != null) {
                        int pos = getAdapterPosition();

                        if (pos != RecyclerView.NO_POSITION) {
                            busStopViewInterface.onItemClick(pos);
                        }
                    }
                }
            });
        }
    }
}


