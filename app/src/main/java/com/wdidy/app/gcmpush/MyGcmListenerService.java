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
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GcmListenerService;
import com.wdidy.app.R;

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

        Log.d("NOTIF", "Receive " + data + ", from " + from);

        if (data != null) {
            String title = data.getString("title");
            String message = data.getString("message");

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
            // TODO only if app is open !
            if (from.equals(this.getResources().getString(R.string.play_api_push))) {
                sendNotification(title, message);
            }
        }
        // [END_EXCLUDE]
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String title, String message) {

        // Attach intent to notification
        //if (intent != null) {
            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            //PendingIntent pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_ONE_SHOT);

            long[] pattern = {0, 100, 250, 100, 250, 100};

            Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_notif) // Only small icon for Lollipop
                    .setContentTitle(title)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setLights(Color.RED, 1000, 3000)
                    .setColor(this.getResources().getColor(R.color.colorPrimary))
                    .setVibrate(pattern)
                    .setSound(defaultSoundUri);
                    //.setContentIntent(pendingIntent);

            // Big icon for previous version (older than Lollipop)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher));
            }

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // Random notification ID
            notificationManager.notify((int) System.currentTimeMillis(), notificationBuilder.build());
        //}
    }
}
