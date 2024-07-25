package com.example.a431transit.objects.bus_stop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.a431transit.BuildConfig;
import com.example.a431transit.R;
import com.example.a431transit.objects.TransitResponse;
import com.example.a431transit.objects.bus_route.BusRoute;
import com.example.a431transit.objects.bus_stop.centre.Centre;
import com.example.a431transit.objects.bus_stop.cross_street.CrossStreet;
import com.example.a431transit.objects.bus_stop.street.Street;
import com.example.a431transit.util.api_communication.TransitAPIService;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BusStop implements Serializable {
    @SerializedName("key")
    private int key;

    @SerializedName("number")
    private int number;

    @SerializedName("name")
    private String name;

    @SerializedName("nickname")
    private String nickname;

    @SerializedName("direction")
    private String direction;

    @SerializedName("side")
    private String side;

    @SerializedName("isSaved")
    private boolean isSaved;

    @SerializedName("centre")
    private Centre centre;

    @SerializedName("street")
    private Street street;

    @SerializedName("cross-street")
    private CrossStreet crossStreet;

    @SerializedName("in-categories")
    private List<String> inCategories;

    @SerializedName("filteredRoutes")
    private List<String> filteredRoutes;

    //store routes both in memory and in a cache
    private static final LruCache<String, List<BusRoute>> routeCache = new LruCache<>(10 * 1024 * 1024);
    private List<BusRoute> busRoutes;

    //store bus stop images in a cache
    private static final LruCache<String, Bitmap> imageCache = new LruCache<>(10 * 1024 * 1024);

    //Make an API call to get the current routes that visit the stop
    public void loadBusRoutes(final Context context, TransitAPIService transitService, ViewGroup layout) {
        // Add a counter for retries
        final int[] retryCount = {0};

        //Check if the bus routes are stored in memory
        if (busRoutes != null) {
            updateRouteView(context, busRoutes, layout);
            return;
        }

        //check if the bus routes are stored in the cache
        List<BusRoute> cachedRoutes = routeCache.get(key + "route");

        if (cachedRoutes != null) {
            busRoutes = cachedRoutes;
            updateRouteView(context, busRoutes, layout);
        } else {
            //make the api call
            Call<TransitResponse> call = transitService.getBusStopRoutes(key, BuildConfig.TRANSIT_API_KEY);

            call.enqueue(new Callback<TransitResponse>() {
                @Override
                public void onResponse(Call<TransitResponse> call, Response<TransitResponse> response) {
                    if (response.isSuccessful()) {
                        //extract the data from the api
                        TransitResponse transitResponse = response.body();

                        if (transitResponse == null) {
                            return;
                        }

                        busRoutes = transitResponse.getBusRoutes();

                        if (busRoutes == null) {
                            return;
                        }

                        //render each route on to the given layout
                        updateRouteView(context, busRoutes, layout);
                    } else {
                        Log.e("transitService", "Error: " + response.code() + " - " + response.message());
                    }
                }

                @Override
                public void onFailure(Call<TransitResponse> call, Throwable t) {
                    Log.e("transitService", "Network request failed", t);
                    t.printStackTrace();

                    // Retry the request up to three times
                    if (retryCount[0] < 3) {
                        retryCount[0]++;
                        Log.i("transitService", "Retrying network request (Retry " + retryCount[0] + ")");
                        call.clone().enqueue(this);
                    } else {
                        Log.e("transitService", "Network request failed after three retries");
                    }
                }
            });
        }
    }

    //For each bus route, create a text view and insert it into the given layout
    private void updateRouteView(Context context, List<BusRoute> busRoutes, ViewGroup layout) {
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

    //Communicate with the Google Static Maps API to fetch an image of the bus stop in google maps
    public void loadImage(final Context context, final ImageView imageView, String shape) {
        //dimensions of image requested
        int width, height, zoom;

        //set the appropriate dimensions for this method call
        if (shape.equals("circle")) {
            width = 300;
            height = 300;
            zoom = 18;
        } else if (shape.equals("square")) {
            width = 900;
            height = 400;
            zoom = 17;
        } else {
            return;
        }

        //create the url to send to the google static map api
        String imageUrl = "https://maps.googleapis.com/maps/api/staticmap?center=" + centre.getGeographic().getLatitude() + "," + centre.getGeographic().getLongitude() +
                "&zoom=" + zoom + "&size=" + width + "x" + height + "&markers=" + centre.getGeographic().getLatitude() + "," + centre.getGeographic().getLongitude() +
                "&key=" + BuildConfig.GOOGLE_API_KEY;

        // Check if the image is already in the cache
        String cacheKey = key + shape + "image";
        Bitmap cachedImage = imageCache.get(cacheKey);

        if (cachedImage != null) {
            // If the image is in the cache, use it directly
            imageView.setImageBitmap(cachedImage);
        } else {
            // If the image is not in the cache, load it using Glide
            Glide.with(context)
                    .asBitmap()
                    .load(imageUrl)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                            // Save the image to the cache
                            imageCache.put(cacheKey, resource);

                            // Set the image to the ImageView
                            imageView.setImageBitmap(resource);
                        }
                    });
        }
    }

    public int getKey() {
        return key;
    }

    public int getNumber() {
        return number;
    }

    public String getName() {
        if (nickname != null) {
            return nickname;
        }

        return getOriginalName();
    }

    public String getOriginalName() {
        //Shorten direction strings for readability
        String output = name;

        output = output.replace("Northbound", "NB");
        output = output.replace("Southbound", "SB");
        output = output.replace("Eastbound", "EB");
        output = output.replace("Westbound", "WB");

        return output;
    }

    public String getDirection() {
        return direction;
    }

    public String getSide() {
        return side;
    }

    public Street getStreet() {
        return street;
    }

    public CrossStreet getCrossSteet() {
        return crossStreet;
    }

    public Centre getCentre() {
        return centre;
    }

    public List<BusRoute> getBusRoutes() {
        return busRoutes;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setSaved(boolean saved) {
        isSaved = saved;
    }

    public Boolean inCategory(String category) {
        if (inCategories != null) {
            return inCategories.contains(category);
        }

        return false;
    }

    public Boolean notInAnyCategory() {
        if (inCategories != null) {
            return inCategories.isEmpty();
        }

        return false;
    }

    public void addCategory(String category) {
        if (inCategories == null) {
            inCategories = new ArrayList<>();
        }

        inCategories.add(category);
    }

    public List<String> getCategories() {
        return inCategories;
    }

    public List<String> getUserCategories() {
        List<String> output = null;

        if (inCategories != null) {
            output = new ArrayList<>(inCategories);
            output.remove("Saved");
        }

        return output;
    }

    public void removeCategory(String category) {
        if (inCategories != null) {
            inCategories.remove(category);
        }
    }

    public List<String> getFilteredRoutes() {
        return filteredRoutes;
    }

    public void setFilteredRoutes(List<String> filteredRoutes) {
        this.filteredRoutes = filteredRoutes;
    }

    @NonNull
    @Override
    public String toString() {
        return String.valueOf(key);
    }
}

