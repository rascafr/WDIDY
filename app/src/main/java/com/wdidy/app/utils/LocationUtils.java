package com.wdidy.app.utils;

import android.content.Context;
import android.location.Location;

/**
 * Created by François L. on 13/06/16.
 */
public class LocationUtils {

    public static String locationToString (Context context, Location location) {

        // Set location's values
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        String address = "---";

        /*Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            address = addresses.get(0).getAddressLine(0);
        } catch (IOException e) {
            address = "Exception : Address cannot be processed";
            e.printStackTrace();
        }*/

        // returns parsed information
        return "Position : " + latitude + ", " + longitude +
                "\nAltitude : " + location.getAltitude() + " m" +
                "\nAddress : " + address + "\nSpeed : " + location.getSpeed() * 3.6 + " km/h" +
                "\nAccuracy : " + location.getAccuracy() + " m";
    }
}
