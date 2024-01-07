package com.example.a431transit.model.arrivals;

import com.google.gson.annotations.SerializedName;

public class Variant {

    @SerializedName("key")
    private String key;

    @SerializedName("name")
    private String name;

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }
}
