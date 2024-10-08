package com.example.a431transit.presentation.front_end_objects;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.a431transit.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private final View viewWindow;

    public CustomInfoWindowAdapter(Context context) {
        viewWindow = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null);
    }

    //Display a bus stop's information onto its corresponding info window
    private void renderWindowText(Marker marker, View view) {
        TextView busName = view.findViewById(R.id.info_window_bus_name);
        TextView busKey = view.findViewById(R.id.info_window_bus_key);

        busName.setText(marker.getTitle());
        busKey.setText(marker.getSnippet());
    }

    @Nullable
    @Override
    public View getInfoContents(@NonNull Marker marker) {
        renderWindowText(marker, viewWindow);
        return viewWindow;
    }

    @Nullable
    @Override
    public View getInfoWindow(@NonNull Marker marker) {
        renderWindowText(marker, viewWindow);
        return viewWindow;
    }
}
