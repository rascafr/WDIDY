package com.wdidy.app;

import android.app.Application;

/**
 * Created by Rascafr on 18/12/2015.
 */
public class AppVisibility extends Application {

    public static boolean isActivityVisible() {
        return messageActivityVisible;
    }

    public static void activityResumed() {
        messageActivityVisible = true;
    }

    public static void activityPaused() {
        messageActivityVisible = false;
    }

    private static boolean messageActivityVisible;
}
