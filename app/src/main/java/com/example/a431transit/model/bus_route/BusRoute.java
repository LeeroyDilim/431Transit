package com.example.a431transit.model.bus_route;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.a431transit.model.bus_route.BadgeStyle;
import com.google.gson.annotations.SerializedName;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

public class BusRoute implements Parcelable {
    @SerializedName("key")
    private Object key;
    @SerializedName("number")
    private Object number;
    @SerializedName("name")
    private String name;
    @SerializedName("customer-type")
    private String customerType;
    @SerializedName("coverage")
    private String coverage;
    @SerializedName("badge-label")
    private Object badgeLabel;
    @SerializedName("badge-style")
    private BadgeStyle badgeStyle;

    protected BusRoute(Parcel in) {
        key = in.readValue(Object.class.getClassLoader());
        number = in.readValue(Object.class.getClassLoader());
        customerType = in.readString();
        coverage = in.readString();
        badgeLabel = in.readValue(Object.class.getClassLoader());
        badgeStyle = in.readParcelable(BadgeStyle.class.getClassLoader());
    }

    public static final Creator<BusRoute> CREATOR = new Creator<BusRoute>() {
        @Override
        public BusRoute createFromParcel(Parcel in) {
            return new BusRoute(in);
        }

        @Override
        public BusRoute[] newArray(int size) {
            return new BusRoute[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // Write data to the parcel
        dest.writeValue(key);
        dest.writeValue(number);
        dest.writeString(customerType);
        dest.writeString(coverage);
        dest.writeValue(badgeLabel);
        dest.writeParcelable(badgeStyle, flags);
    }

    public static void writeToParcelList(List<BusRoute> busRoutes, Parcel dest, int flags) {
        dest.writeTypedList(busRoutes);
    }

    //Return a string containing the key of this route
    public String getKey() {
        //since the api can return a route with either a string or a float as the key, handle the cases appropriately
        if (key instanceof String) {
            return (String) key;
        } else if (key instanceof Number) {
            //convert the float into a string with no decimals
            DecimalFormat decimalFormat = new DecimalFormat("0");
            return decimalFormat.format(key);
        }

        return null;
    }

    //Return a string containing the number of this route
    public String getNumber() {
        //since the api can return a route with either a string or a float as the number, handle the cases appropriately
        if (number instanceof String) {
            return (String) number;
        } else if (number instanceof Number) {
            //convert the float into a string with no decimals
            DecimalFormat decimalFormat = new DecimalFormat("0");
            return decimalFormat.format(number);
        }

        return null;
    }

    public String getCustomerType() {
        return customerType;
    }

    public String getCoverage() {
        return coverage;
    }

    //Return a string containing the number of this route
    public String getBadgeLabel() {
        //since the api can return a route with either a string or a float as the badge, handle the cases appropriately
        if (badgeLabel instanceof String) {
            return (String) badgeLabel;
        } else if (badgeLabel instanceof Number) {
            //convert the float into a string with no decimals
            DecimalFormat decimalFormat = new DecimalFormat("0");
            return decimalFormat.format(badgeLabel);
        }

        return null;
    }

    public BadgeStyle getBadgeStyle() {
        return badgeStyle;
    }

    public String getName() {
        //BLUE Routes do not have a name :(
        if (name == null) {
            return getKey();
        }

        String[] words = name.split("\\s+");

        //the name of the route usually includes "Route {route number} so remove these first two words in the list
        if (words.length >= 2) {
            return String.join(" ", Arrays.copyOfRange(words, 2, words.length));
        } else {
            return name;
        }
    }

    @NonNull
    @Override
    public String toString() {
        return getKey();
    }
}
