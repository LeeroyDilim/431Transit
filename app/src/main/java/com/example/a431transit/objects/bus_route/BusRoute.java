package com.example.a431transit.objects.bus_route;

import androidx.annotation.NonNull;

import com.example.a431transit.objects.bus_route.badge_style.BadgeStyle;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Arrays;

public class BusRoute implements Serializable {
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
