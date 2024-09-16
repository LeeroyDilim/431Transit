package com.example.a431transit.presentation.front_end_objects.bus_stop_list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a431transit.R;
import com.example.a431transit.presentation.front_end_objects.ArrivalInstanceView;
import com.example.a431transit.api.transit_api.TransitAPIService;

import java.util.List;

public class BusArrivalAdapter extends RecyclerView.Adapter<BusArrivalAdapter.BusArrivalViewHolder> {

    Context context;
    List<ArrivalInstanceView> arrivalInstanceViews;
    TransitAPIService transitService;

    public BusArrivalAdapter(Context context, List<ArrivalInstanceView> arrivalInstanceViews, TransitAPIService transitService) {
        this.context = context;
        this.arrivalInstanceViews = arrivalInstanceViews;
        this.transitService = transitService;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public BusArrivalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BusArrivalViewHolder(LayoutInflater.from(context).inflate(R.layout.bus_arrival_view, parent, false));
    }

    @Override
    //Display an arrival instance's information with its corresponding view holder
    public void onBindViewHolder(@NonNull BusArrivalViewHolder holder, int position) {
        ArrivalInstanceView currentArrivalInstanceView = arrivalInstanceViews.get(position);

        currentArrivalInstanceView.loadRouteBadge(holder.routeBadgeView);
        currentArrivalInstanceView.loadRouteName(holder.routeNameView);
        currentArrivalInstanceView.loadBusTime(holder.busArrivalTimeView);
        currentArrivalInstanceView.loadBusStatus(context, holder.busStatusView);

        //set a listener so that it shows the full name of a route on click.
        holder.routeNameView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.routeNameView.getMaxLines() == 2) {
                    holder.routeNameView.setMaxLines(Integer.MAX_VALUE);
                } else {
                    holder.routeNameView.setMaxLines(2);
                }
            }
        });
    }

    public void updateData(List<ArrivalInstanceView> arrivalInstanceViews) {
        this.arrivalInstanceViews = arrivalInstanceViews;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (arrivalInstanceViews != null)
            return arrivalInstanceViews.size();
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

    public class BusArrivalViewHolder extends RecyclerView.ViewHolder {

        TextView routeBadgeView;
        TextView routeNameView;
        TextView busArrivalTimeView;
        TextView busStatusView;

        public BusArrivalViewHolder(@NonNull View itemView) {
            super(itemView);
            routeBadgeView = itemView.findViewById(R.id.arrivals_view_route_badge);
            routeNameView = itemView.findViewById(R.id.arrivals_view_route_name);
            busArrivalTimeView = itemView.findViewById(R.id.arrivals_view_bus_arrival);
            busStatusView = itemView.findViewById(R.id.arrivals_view_bus_status);
        }
    }
}
