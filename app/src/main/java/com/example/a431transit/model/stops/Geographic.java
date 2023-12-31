package com.example.a431transit.model.stops;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class Geographic implements Parcelable {
    @SerializedName("latitude")
    private String latitude;

    @SerializedName("longitude")
    private String longitude;

    public Geographic(String latitude, String longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    protected Geographic(Parcel in) {
        latitude = in.readString();
        longitude = in.readString();
    }

    public static final Creator<Geographic> CREATOR = new Creator<Geographic>() {
        @Override
        public Geographic createFromParcel(Parcel in) {
            return new Geographic(in);
        }

        @Override
        public Geographic[] newArray(int size) {
            return new Geographic[size];
        }
    };

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(latitude);
        dest.writeString(longitude);
    }
}
