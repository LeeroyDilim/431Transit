package com.example.a431transit.objects.stops.street;

import com.google.gson.annotations.SerializedName;

public class Street {
    @SerializedName("key")
    private int key;

    @SerializedName("name")
    private String name;

    @SerializedName("type")
    private String type;

    public Street(int k, String n, String t) {
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
