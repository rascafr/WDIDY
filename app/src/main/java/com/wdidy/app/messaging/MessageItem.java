package com.wdidy.app.messaging;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Rascafr on 16/12/2015.
 */
public class MessageItem {
    private int IDmessage;
    private String text;
    private String date;
    private String IDsender;
    private long timestamp;
    private boolean showDate;

    public MessageItem(JSONObject obj) throws JSONException {
        text = obj.getString("text");
        date = obj.getString("date");
        IDsender = obj.getString("IDsender");
        IDmessage = obj.getInt("IDmessage");
        timestamp = obj.getLong("timestamp");
        showDate = false;
    }

    public MessageItem(String IDsender, String text) {
        this.text = text;
        this.IDsender = IDsender;
    }

    public String getText() {
        return text;
    }

    public String getDate() {
        return date;
    }

    public String getIDsender() {
        return IDsender;
    }

    public boolean isUser(String IDuser) {
        return IDuser.equals(IDsender);
    }

    public int getIDmessage() {
        return IDmessage;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isShowDate() {
        return showDate;
    }

    public void setShowDate(boolean showDate) {
        this.showDate = showDate;
    }
}
