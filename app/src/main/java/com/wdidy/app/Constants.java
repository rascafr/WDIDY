package com.wdidy.app;

/**
 * Created by Rascafr on 23/10/2015.
 */
public class Constants {

    // Preferences URI
    public final static String PREFS_ACCOUNT_KEY = "com.wdidy.app.profile.key";
    public final static String PREFS_ACCOUNT_EMAIL = "com.wdidy.app.profile.email";
    public final static String PREFS_ACCOUNT_NAME = "com.wdidy.app.profile.name";
    public final static String PREFS_ACCOUNT_IDUSER = "com.wdidy.app.profile.iduser";
    public final static String PREFS_ACCOUNT_AGE = "com.wdidy.app.profile.age";
    public final static String PREFS_ACCOUNT_CITY = "com.wdidy.app.profile.city";
    public final static String PREFS_ACCOUNT_COUNTRY = "com.wdidy.app.profile.country";
    public final static String PREFS_ACCOUNT_PASSWORD = "com.wdidy.app.profile.password";
    public final static String PREFS_ACCOUNT_CREATED= "com.wdidy.app.profile.created";
    public final static String PREFS_GPS_PROVIDER = "com.wdidy.app.providermode";

    // Intents
    public final static String INTENT_TRACK_ID = "intent.com.wdidy.app.trackid";
    public final static String INTENT_TRACK_NAME = "intent.com.wdidy.app.trackname";

    // URL
    private static final String URL_SERVER = "http://217.199.187.59/francoisle.fr/wdidy/";
    public static final String URL_SERVER_CONNECT_CLIENT = URL_SERVER + "apps/connectClient.php";
    public static final String URL_SERVER_READ_POINTS = URL_SERVER + "apps/readPoints.php?";
    public static final String URL_PROFILE_PICTS = "http://217.199.187.59/francoisle.fr/wdidy/picts/";

    // API
    public static final String API_KEY = "47856230";
    private static final String URL_API = URL_SERVER + "api/";
    public static final String API_LIST_TRACKS = URL_API + "user/list_tracks";
    public static final String API_LIST_POINTS = URL_API + "track/list_points";
    public static final String API_NEW_TRACK = URL_API + "track/create";
    public static final String API_ADD_POINT = URL_API + "point/add";

}
