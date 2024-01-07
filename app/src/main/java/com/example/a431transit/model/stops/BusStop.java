package com.example.a431transit.model.stops;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
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
import com.example.a431transit.model.TransitResponse;
import com.example.a431transit.model.bus_route.BusRoute;
import com.example.a431transit.util.api_communication.TransitAPIService;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BusStop implements Parcelable {
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
    private boolean isSaved = false;

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

    //store routes both in memory and in the cache
    private static final LruCache<String, List<BusRoute>> routeCache = new LruCache<>(10 * 1024 * 1024);
    private List<BusRoute> busRoutes;

    //store bus stop images both in memory and in the cache
    private static final LruCache<String, Bitmap> imageCache = new LruCache<>(10 * 1024 * 1024);
    private Bitmap busImage;

    protected BusStop(Parcel in) {
        key = in.readInt();
        number = in.readInt();
        name = in.readString();
        nickname = in.readString();
        direction = in.readString();
        side = in.readString();
        isSaved = in.readBoolean();
        centre = in.readParcelable(Centre.class.getClassLoader());
        busRoutes = in.createTypedArrayList(BusRoute.CREATOR); // Read the list
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(key);
        dest.writeInt(number);
        dest.writeString(name);
        dest.writeString(nickname);
        dest.writeString(direction);
        dest.writeString(side);
        dest.writeBoolean(isSaved);
        dest.writeParcelable(centre, flags);
        dest.writeTypedList(busRoutes);
    }

    public static final Creator<BusStop> CREATOR = new Creator<BusStop>() {
        @Override
        public BusStop createFromParcel(Parcel in) {
            return new BusStop(in);
        }

        @Override
        public BusStop[] newArray(int size) {
            return new BusStop[size];
        }
    };

    public void loadBusRoutes(final Context context, TransitAPIService transitService, ViewGroup layout) {
        // Add a counter for retries
        final int[] retryCount = {0};

        if (busRoutes != null) {
            updateRouteView(context, busRoutes, layout);
        }

        List<BusRoute> cachedRoutes = routeCache.get(key + "route");

        if (cachedRoutes != null) {
            busRoutes = cachedRoutes;
            updateRouteView(context, busRoutes, layout);
        } else {
            Call<TransitResponse> call = transitService.getBusStopRoutes(key, BuildConfig.TRANSIT_API_KEY);

            call.enqueue(new Callback<TransitResponse>() {
                @Override
                public void onResponse(Call<TransitResponse> call, Response<TransitResponse> response) {
                    if (response.isSuccessful()) {
                        TransitResponse transitResponse = response.body();

                        if (transitResponse == null) {
                            return;
                        }

                        busRoutes = transitResponse.getBusRoutes();

                        if (busRoutes == null) {
                            return;
                        }

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

    private void updateRouteView(Context context, List<BusRoute> busRoutes, ViewGroup layout) {
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

            if(filteredRoutes != null && !filteredRoutes.contains(busRoute.getKey()))
            {
                newText.setAlpha(0.35f);
            }

            layout.addView(newText);
        }
    }

    private static TextView createRouteTextView(Context context, BusRoute busRoute) {
        LayoutInflater inflater = LayoutInflater.from(context);
        TextView output = (TextView) inflater.inflate(R.layout.bus_route_text_view, null);

        int backgroundColor = android.graphics.Color.parseColor(busRoute.getBadgeStyle().getBackgroundColor());
        int textColor = android.graphics.Color.parseColor(busRoute.getBadgeStyle().getTextColor());

        //replace standard badge-style colours with colours that are easier on the eyes
        if (backgroundColor == Color.WHITE) {
            backgroundColor = Color.rgb(230, 230, 230);
        }

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

        output.setId(View.generateViewId());

        output.setText(busRoute.getNumber());
        output.setTextColor(textColor);

        output.setBackgroundColor(backgroundColor);

        return output;
    }

    public void loadImage(final Context context, final ImageView imageView, String shape) {
        //dimensions of image requested
        int width, height, zoom;

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

                            busImage = resource;

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

    public Boolean inCategory(String category)
    {
        return inCategories.contains(category);
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

    public List<String> getEditableCategories() {
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

