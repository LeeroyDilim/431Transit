package com.example.a431transit.model.stops;

import com.google.gson.annotations.SerializedName;

public class UTM {
    @SerializedName("zone")
    private String zone;

    @SerializedName("x")
    private int x;

    @SerializedName("y")
    private int y;

    public UTM(String z, int x, int y) {
        zone = z;
        this.x = x;
        this.y = y;
    }

    public String getZone() {
        return zone;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

}
