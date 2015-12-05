package com.wdidy.app.track;

import java.util.ArrayList;

/**
 * Created by Rascafr on 06/11/2015.
 */
public class DataManager {

    private static DataManager instance;

    private DataManager() {}

    public static DataManager getInstance() {
        if (instance == null)
            instance = new DataManager();
        return instance;
    }

    // Track array
    private ArrayList<TrackItem> trackItems;

    public void initTracksItems() {
        if (trackItems == null)
            trackItems = new ArrayList<>();
        else
            trackItems.clear();
    }

    public ArrayList<TrackItem> getTrackItems() {
        return trackItems;
    }

    public void setTrackItems(ArrayList<TrackItem> trackItems) {
        this.trackItems = trackItems;
    }
}
