package com.wdidy.app;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import java.util.List;
import java.util.Locale;

/**
 * Created by Rascafr on 20/06/2015.
 */
public class LocationUtils {

    private Context context;

    public LocationUtils (Context context) {
        this.context = context;
    }

    /**
     * Functions
     */
    public String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                //Log.w("My Current location address", "" + strReturnedAddress.toString());
            } else {
                //Log.w("My Current location address", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            //Log.w("My Current location address", "Cannot get Address!");
        }
        return strAdd;
    }
}
