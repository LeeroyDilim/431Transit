package com.example.a431transit.model.stops;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class Centre implements Parcelable {

    @SerializedName("geographic")
    private Geographic geographic;

    @SerializedName("utm")
    private UTM utm;

    public Centre(String zone, int x, int y, String latitude, String longitude) {
        utm = new UTM(zone, x, y);
        geographic = new Geographic(latitude, longitude);
    }

    protected Centre(Parcel in) {
        geographic = in.readParcelable(Geographic.class.getClassLoader());
    }

    public static final Creator<Centre> CREATOR = new Creator<Centre>() {
        @Override
        public Centre createFromParcel(Parcel in) {
            return new Centre(in);
        }

        @Override
        public Centre[] newArray(int size) {
            return new Centre[size];
        }
    };

    public UTM getUtm() {
        return utm;
    }

    public Geographic getGeographic() {
        return geographic;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeParcelable(geographic, flags);
    }
}
