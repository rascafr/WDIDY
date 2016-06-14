package com.wdidy.app.gps;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by François L. on 14/06/16.
 */
public class GPSTrack {

    private ArrayList<GPSPoint> gpsPoints;
    private int trackID;

    public GPSTrack(int trackID) {
        this.trackID = trackID;
        initGPSPoints();
        Log.d("TRK", "Track initiated with identifier " + trackID);
    }

    public ArrayList<GPSPoint> getGpsPoints() {
        return gpsPoints;
    }

    public int getTrackID() {
        return trackID;
    }

    public void initGPSPoints () {
        if (gpsPoints == null)
            gpsPoints = new ArrayList<>();
        gpsPoints.clear();
    }
}
