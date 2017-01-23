package com.pujolsluis.android.hangeo;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Oficina on 23/01/2017.
 */

@IgnoreExtraProperties
public class CustomLatLngPoints {
    private double Lat, Lng;

    CustomLatLngPoints(){

    }

    public double getLat() {
        return Lat;
    }

    public void setLat(double lat) {
        Lat = lat;
    }

    public double getLng() {
        return Lng;
    }

    public void setLng(double lng) {
        Lng = lng;
    }
}
