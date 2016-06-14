package com.wdidy.app.gps;

import android.location.Location;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by François L. on 14/06/16.
 */
public class GPSPoint {

    private String latitude, longitude, date, address;

    public GPSPoint(Location location, String address) {
        this.latitude = String.valueOf(location.getLatitude());
        this.longitude = String.valueOf(location.getLongitude());
        this.address = address;

        // get current date
        SimpleDateFormat dfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar c = Calendar.getInstance();
        this.date = dfDate.format(c.getTime());
    }

    public HashMap<String,String> getAPIformatted (String apiKey, int trackID) {

        HashMap<String, String> pairs = new HashMap<>();

        pairs.put("api_id", apiKey);
        pairs.put("track_id", String.valueOf(trackID));
        pairs.put("latitude", latitude);
        pairs.put("longitude", longitude);
        pairs.put("date_point", date);
        pairs.put("address", address);

        return pairs;
    }
}
