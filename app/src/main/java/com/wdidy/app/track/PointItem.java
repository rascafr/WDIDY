package com.wdidy.app.track;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Rascafr on 06/11/2015.
 */
public class PointItem {

    private int IDpoint;
    private double lat, lon;
    private String datetime, address;

    public PointItem (JSONObject obj) throws JSONException {
        this.IDpoint = obj.getInt("IDpoint");
        this.lat = obj.getDouble("lat");
        this.lon = obj.getDouble("lon");
        this.datetime = obj.getString("datetime");
        this.address = obj.getString("address");
    }

    public int getIDpoint() {
        return IDpoint;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public String getDatetime() {
        return datetime;
    }

    public String getAddress() {
        return address;
    }
}
