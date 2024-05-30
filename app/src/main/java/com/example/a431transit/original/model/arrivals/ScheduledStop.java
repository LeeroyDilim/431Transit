package com.example.a431transit.model.arrivals;

import com.google.gson.annotations.SerializedName;

public class ScheduledStop {
    @SerializedName("key")
    private String key;

    @SerializedName("cancelled")
    private boolean cancelled;

    @SerializedName("times")
    private Times times;

    @SerializedName("variant")
    private Variant variant;

    @SerializedName("bus")
    private Bus bus;

    public String getKey() {
        return key;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public Times getTimes() {
        return times;
    }

    public Variant getVariant() {
        return variant;
    }

    public Bus getBus() {
        return bus;
    }

}
