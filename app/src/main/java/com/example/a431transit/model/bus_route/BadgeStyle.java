package com.example.a431transit.model.bus_route;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

public class BadgeStyle implements Parcelable {
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

    // Parcelable implementation
    protected BadgeStyle(Parcel in) {
        backgroundColor = in.readString();
        borderColor = in.readString();
        textColor = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(backgroundColor);
        dest.writeString(borderColor);
        dest.writeString(textColor);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BadgeStyle> CREATOR = new Creator<BadgeStyle>() {
        @Override
        public BadgeStyle createFromParcel(Parcel in) {
            return new BadgeStyle(in);
        }

        @Override
        public BadgeStyle[] newArray(int size) {
            return new BadgeStyle[size];
        }
    };
}