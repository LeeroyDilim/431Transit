package com.example.a431transit.objects.bus_route.badge_style;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class BadgeStyle implements Serializable {
    @SerializedName("background-color")
    private String backgroundColor;

    @SerializedName("border-color")
    private String borderColor;

    @SerializedName("color")
    private String textColor;

    public String getTextColor() {
        return textColor;
    }

    public String getBorderColor() {
        return borderColor;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

}