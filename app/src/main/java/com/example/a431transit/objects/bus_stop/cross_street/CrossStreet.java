package com.example.a431transit.objects.bus_stop.cross_street;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class CrossStreet implements Serializable {
    @SerializedName("key")
    private int key;

    @SerializedName("name")
    private String name;

    @SerializedName("type")
    private String type;

    public CrossStreet(int k, String n, String t) {
        key = k;
        name = n;
        type = t;
    }

    public int getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}
