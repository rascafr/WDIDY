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

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.wdidy.app.Constants;
import com.wdidy.app.R;
import com.wdidy.app.account.UserAccount;
import com.wdidy.app.utils.ConnexionUtils;
import com.wdidy.app.utils.Utilities;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";
    private static final String[] TOPICS = {"global"};
    private SharedPreferences sharedPreferences;

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            // [START register_for_gcm]
            // Initially this call goes out to the network to retrieve the token, subsequent calls
            // are local.
            // [START get_token]
            InstanceID instanceID = InstanceID.getInstance(this);
            //Log.i(TAG, "Sender ID : " + getString(R.string.play_api_push));
            String token = instanceID.getToken(getString(R.string.play_api_push), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            // [END get_token]
            //Log.i(TAG, "GCM Registration Token: " + token);

            // TODO: Implement this method to send any registration to your app's servers.
            sendRegistrationToServer(token, this);

            // Subscribe to topic channels
            subscribeTopics(token);

            // You should store a boolean that indicates whether the generated token has been
            // sent to your server. If the boolean is false, send the token to your server,
            // otherwise your server should have already received the token.
            //sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, true).apply();
            // [END register_for_gcm]
        } catch (Exception e) {
            //Log.d(TAG, "Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false).apply();
        }

    }

    /**
     * Persist registration to third-party servers.
     *
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token, Context ctx) {
        // Add custom implementation, as needed.
        GcmPushToken asyncPushToken = new GcmPushToken(ctx);
        asyncPushToken.execute(token);
    }

    // Remove this asynctask because of GcmPushToken class ?..
    @Deprecated
    private class AsyncPushToken extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            return null;
        }
    }

    /**
     * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
     *
     * @param token GCM token
     * @throws IOException if unable to reach the GCM PubSub service
     */
    // [START subscribe_topics]
    private void subscribeTopics(String token) throws IOException {
        for (String topic : TOPICS) {
            GcmPubSub pubSub = GcmPubSub.getInstance(this);
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }
    // [END subscribe_topics]

    /**
     * Send push token to server WDIDY
     */
    private class GcmPushToken extends AsyncTask <String,String,String> {

        private Context ctx;

        public GcmPushToken (Context ctx) {
            this.ctx = ctx;
        }

        @Override
        protected void onPreExecute() {
            Log.d("GcmPushToken", "[OK] Preparing to register ...");
        }

        @Override
        protected void onPostExecute(String data) {

            Log.d("NOTIF", "API returns : " + data);

            boolean success = false;

            if (Utilities.isNetworkDataValid(data)) {
                try {
                    JSONObject jsonObject = new JSONObject(data);
                    if (jsonObject.getInt("error") == 0) {
                        success = true;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (success) {
                // Notify UI that registration has completed, so the progress indicator can be hidden.
                Log.d("NOTIF", "Registration complete");
                sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, true).apply();
            } else {
                Log.d("NOTIF", "Registration failed");
                sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false).apply();
            }

            Intent registrationStatus = new Intent(QuickstartPreferences.REGISTRATION_COMPLETE);
            LocalBroadcastManager.getInstance(RegistrationIntentService.this).sendBroadcast(registrationStatus);

        }

        @Override
        protected String doInBackground(String... params) {

            String pushResult = null;
            UserAccount profile = new UserAccount();
            profile.readAccountPromPrefs(RegistrationIntentService.this);

            if (profile.isCreated()) {
                HashMap<String, String> pairs = new HashMap<>();
                pairs.put("api_id", Constants.API_KEY);
                pairs.put("user_id", profile.getUserID());
                pairs.put("device_id", params[0]);
                pushResult = ConnexionUtils.postServerData(Constants.API_REGISTER_DEVICE, pairs);

                // Save token into profile
                profile.setDevice_id(params[0]);
                profile.registerAccountInPrefs(ctx);
            }

            return pushResult;
        }
    }
}
