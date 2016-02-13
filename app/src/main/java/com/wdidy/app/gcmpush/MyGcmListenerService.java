/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wdidy.app.gcmpush;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GcmListenerService;
import com.wdidy.app.AppVisibility;
import com.wdidy.app.Constants;
import com.wdidy.app.MessageActivity;
import com.wdidy.app.R;

import org.json.JSONException;
import org.json.JSONObject;

public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {

        //Log.d("NOTIF", "Receive " + data + ", from " + from);

        if (data != null) {
            String title = data.getString("title");
            String message = data.getString("message");
            String jsonStringIntent = data.getString("intent");

            // [START_EXCLUDE]
            /**
             * Production applications would usually process the message here.
             * Eg: - Syncing with server.
             *     - Store message in local database.
             *     - Update UI.
             */

            /**
             * In some cases it may be useful to show a notification indicating to the user
             * that a message was received.
             */
            // Notify the user if the app is not currently showing message's activity
            // Also, check the service sender (unuseful ... right ?)
            if (!AppVisibility.isActivityVisible() && from.equals(this.getResources().getString(R.string.play_api_push))) {
                try {
                    sendNotification(title, message, jsonStringIntent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        // [END_EXCLUDE]
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     * Last update : 18/12/2015 by Rascafr
     * @param title The notification's title
     * @param message The notification's message
     * @param jsonStringIntent The intent object (as JSON string). See server and associated documention.
     */
    private void sendNotification(String title, String message, String jsonStringIntent) throws JSONException {

        // Prepare intent
        Intent appIntent = null;
        JSONObject jsonIntent = new JSONObject(jsonStringIntent);
        String app_action = jsonIntent.getString("app_action");
        JSONObject jsonExtra = jsonIntent.getJSONObject("extra");

        // Decide app intent (message, new track, etc ...).
        switch (app_action) {

            // New message received
            case Constants.INTENT_PUSH_NEW_MESSAGE:
                appIntent = new Intent(this, MessageActivity.class);
                appIntent.putExtra(Constants.INTENT_CONV_FRIEND_ID, jsonExtra.getString("friend_id"));
                appIntent.putExtra(Constants.INTENT_CONV_FRIEND_NAME, jsonExtra.getString("friend_name"));
                break;
        }

        // Attach intent to notification
        if (appIntent != null) {
            appIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), appIntent, PendingIntent.FLAG_ONE_SHOT);

            long[] pattern = {0, 100, 250, 100, 250, 100};
            int color = this.getResources().getColor(R.color.colorPrimary);

            Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_notif) // Only small icon for Lollipop
                    .setContentTitle(title)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setLights(color, 1000, 3000)
                    .setColor(color)
                    .setVibrate(pattern)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent);

            // Big icon for previous version (older than Lollipop)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_notif));
            }

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // Random notification ID
            notificationManager.notify(0, notificationBuilder.build()); // Only one notification item in status bar
        }
    }
}
