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
    public final static String PREFS_APP_VERSION = "com.wdidy.app.version";

    // Activity Intents
    public final static String INTENT_TRACK_ID = "intent.com.wdidy.app.trackid";
    public final static String INTENT_TRACK_NAME = "intent.com.wdidy.app.trackname";
    public final static String INTENT_CONV_FRIEND_ID = "intent.com.wdidy.app.conversation.friendID";
    public final static String INTENT_CONV_FRIEND_NAME = "intent.com.wdidy.app.conversation.friendName";
    public final static String INTENT_CONV_FROM_GCM = "intent.com.wdidy.app.conversation.from_gcm";

    // Push Intents
    public final static String INTENT_PUSH_NEW_MESSAGE = "intent.com.wdidy.app.push.conversation.new";

    // URL
    private static final String URL_SERVER = "http://217.199.187.59/francoisle.fr/wdidy/";
    public static final String URL_SERVER_CONNECT_CLIENT = URL_SERVER + "apps/connectClient.php";
    public static final String URL_SERVER_READ_POINTS = URL_SERVER + "apps/readPoints.php?";
    public static final String URL_PROFILE_PICTS = "http://217.199.187.59/francoisle.fr/wdidy/picts/";

    // API
    public static final String API_KEY = "47856230";
    private static final String URL_API = URL_SERVER + "api/";
    public static final String API_LOGIN_USER = URL_API + "user/login";
    public static final String API_LIST_TRACKS = URL_API + "user/list_tracks";
    public static final String API_LIST_POINTS = URL_API + "track/list_points";
    public static final String API_NEW_TRACK = URL_API + "track/create";
    public static final String API_ADD_POINT = URL_API + "point/add";
    public static final String API_LIST_FRIENDS = URL_API + "friend/list";
    public static final String API_SINGLE_CONVERSATION = URL_API + "message/single";
    public static final String API_POST_MESSAGE = URL_API + "message/post";
    public static final String API_LIST_CONVERSATIONS = URL_API + "message/list";
    public static final String API_REGISTER_DEVICE = URL_API + "push/register";

    // Bus bundle
    public static final String BUNDLE_TRACK_ID = "bundle.track_id";

}
