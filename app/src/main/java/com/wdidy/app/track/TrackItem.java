package com.wdidy.app.track;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Rascafr on 06/11/2015.
 */
public class TrackItem {

    private int IDtrack;
    private String name, start, end;

    public TrackItem (JSONObject obj) throws JSONException {
        this.IDtrack = obj.getInt("IDtrack");
        this.name = obj.getString("name");
        this.start = obj.getString("start");
        this.end = obj.getString("end");
    }

    public int getIDtrack() {
        return IDtrack;
    }

    public String getName() {
        return name;
    }

    public String getStart() {
        return start;
    }

    public String getEnd() {
        return end;
    }
}
