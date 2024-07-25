package com.example.a431transit.objects.bus_stop.centre;

import com.example.a431transit.objects.bus_stop.centre.geographic.Geographic;
import com.example.a431transit.objects.bus_stop.centre.utm.UTM;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Centre implements Serializable {

    @SerializedName("geographic")
    private Geographic geographic;

    @SerializedName("utm")
    private UTM utm;

    public Centre(String zone, int x, int y, String latitude, String longitude) {
        utm = new UTM(zone, x, y);
        geographic = new Geographic(latitude, longitude);
    }

    public UTM getUtm() {
        return utm;
    }

    public Geographic getGeographic() {
        return geographic;
    }

}
