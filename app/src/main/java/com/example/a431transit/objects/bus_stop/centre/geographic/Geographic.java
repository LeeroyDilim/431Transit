package com.example.a431transit.objects.bus_stop.centre.geographic;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Geographic implements Serializable {
    @SerializedName("latitude")
    private String latitude;

    @SerializedName("longitude")
    private String longitude;

    public Geographic(String latitude, String longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

}
