package com.wdidy.app.friend;

import com.wdidy.app.Constants;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Rascafr on 16/12/2015.
 */
public class FriendItem {
    private String friendID;
    private String name, city;
    private String imageLink;

    public FriendItem(JSONObject obj) throws JSONException {
        friendID = obj.getString("IDfriend");
        name = obj.getString("firstname") + " " + obj.getString("lastname");
        city = obj.getString("city");
        imageLink = Constants.URL_PROFILE_PICTS + obj.getString("imgLink");
    }

    public FriendItem(String name, String city, String imageLink) {
        this.name = name;
        this.city = city;
        this.imageLink = imageLink;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    public String getImageLink() {
        return imageLink;
    }

    public String getFriendID() {
        return friendID;
    }
}
