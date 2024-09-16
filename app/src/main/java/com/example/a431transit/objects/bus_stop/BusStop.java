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
import com.example.a431transit.api.transit_api.TransitAPIService;
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

