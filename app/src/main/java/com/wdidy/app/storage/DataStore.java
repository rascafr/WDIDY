package com.wdidy.app.storage;

import com.wdidy.app.gps.GPSTrack;
import com.wdidy.app.gps.WDIDYLocationManager;

/**
 * Created by François L. on 13/06/16.
 */
public class DataStore {

    private static DataStore ourInstance = new DataStore();

    public static DataStore getInstance() {
        return ourInstance;
    }

    /**
     * Location manager
     */
    private WDIDYLocationManager locationManager;

    public WDIDYLocationManager getLocationManager() {
        return locationManager;
    }

    public void setLocationManager(WDIDYLocationManager locationManager) {
        this.locationManager = locationManager;
    }

    /**
     * Track and points list
     */
    private GPSTrack gpsTrack;

    public GPSTrack getGpsTrack() {
        return gpsTrack;
    }

    public void setGpsTrack(GPSTrack gpsTrack) {
        this.gpsTrack = gpsTrack;
    }
}
