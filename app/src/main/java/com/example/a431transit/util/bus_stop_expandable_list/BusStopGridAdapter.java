package com.example.a431transit.util.bus_stop_expandable_list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.a431transit.R;
import com.example.a431transit.objects.bus_stop.BusStop;
import com.example.a431transit.api.transit_api.TransitAPIService;

import java.util.List;

public class BusStopGridAdapter extends BaseAdapter {
    private Context context;
    private List<BusStop> busStops;
    private TransitAPIService transitAPIService;

    public BusStopGridAdapter(Context context, List<BusStop> busStops, TransitAPIService transitAPIService) {
        this.context = context;
        this.busStops = busStops;
        this.transitAPIService = transitAPIService;
    }

    @Override
    public int getCount() {
        return busStops.size();
    }

    @Override
    public BusStop getItem(int position) {
        return busStops.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    //Display the bus stop's information with it's corresponding viewholder
    public View getView(int position, View convertView, ViewGroup parent) {
        BusStopGridViewHolder holder = null;
        BusStop busStop = busStops.get(position);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.bus_stop_grid_view, parent, false);

            holder = new BusStopGridViewHolder();
            holder.stopNameView = (TextView) convertView.findViewById(R.id.bus_stop_grid_name_view);
            holder.stopKeyView = (TextView) convertView.findViewById(R.id.bus_stop_grid_key_view);
            holder.stopImageView = (ImageView) convertView.findViewById(R.id.bus_stop_grid_image_view);

            convertView.setTag(holder);
        } else {
            holder = (BusStopGridViewHolder) convertView.getTag();
        }

        //update the view components
        holder.stopNameView.setText(busStop.getName());
        holder.stopKeyView.setText("#" + busStop.getKey());
        busStop.loadImage(context, holder.stopImageView, "circle");

        return convertView;
    }

    private static class BusStopGridViewHolder {
        TextView stopNameView;
        TextView stopKeyView;
        ImageView stopImageView;
    }
}
