package com.example.a431transit.api.google_static_maps_api;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.a431transit.BuildConfig;
import com.example.a431transit.application.AppConstants;
import com.example.a431transit.objects.bus_stop.BusStop;

import java.util.function.Consumer;

public class GoogleStaticMapsClient {
    public static Runnable fetchImageRunnable(BusStop busStop, String shape, Context context, ImageView imageView) {
        int width, height, zoom;

        //set the appropriate dimensions for this method call
        if (shape.equals(AppConstants.CircleImage.NAME)) {
            width = AppConstants.CircleImage.WIDTH;
            height = AppConstants.CircleImage.HEIGHT;
            zoom = AppConstants.CircleImage.ZOOM;
        } else if (shape.equals(AppConstants.RectangleImage.NAME)) {
            width = AppConstants.RectangleImage.WIDTH;
            height = AppConstants.RectangleImage.HEIGHT;
            zoom = AppConstants.RectangleImage.ZOOM;
        } else {
            return null;
        }

        // Create the Google Static Maps API URL
        String imageUrl = "https://maps.googleapis.com/maps/api/staticmap?center="
                + busStop.getCentre().getGeographic().getLatitude() + ","
                + busStop.getCentre().getGeographic().getLongitude()
                + "&zoom=" + zoom
                + "&size=" + width + "x" + height
                + "&markers=" + busStop.getCentre().getGeographic().getLatitude() + ","
                + busStop.getCentre().getGeographic().getLongitude()
                + "&key=" + BuildConfig.GOOGLE_API_KEY;

        return () -> {
            Glide.with(context)
                    .asBitmap()
                    .load(imageUrl)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            imageView.setImageBitmap(resource);
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            Log.e("Google Static Maps API", "Failed to load image from Google Static Maps API");
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }
                    });
        };
    }
}
