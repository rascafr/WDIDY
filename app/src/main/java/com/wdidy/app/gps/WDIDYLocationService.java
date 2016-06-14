package com.wdidy.app.gps;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.wdidy.app.Constants;
import com.wdidy.app.R;
import com.wdidy.app.bus.BusRequest;
import com.wdidy.app.storage.DataStore;
import com.wdidy.app.utils.ConnexionUtils;
import com.wdidy.app.utils.LocationUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Created by root on 13/06/16.
 */
public class WDIDYLocationService extends Service implements WDIDYLocationManager.WDIDYLocationListener {

    private final IBinder mBinder = new MyBinder();

    private static final int STATUS_IDLE = 0;
    private static final int STATUS_PREPARE = 1;
    private static final int STATUS_RUNNING = 2;
    private static final int STATUS_TERMINATE = 3;

    // Notification
    private String title = "Enregistrement WDIDY";
    private int notifID = 4242;

    private int locationStatus;

    private int track_id;

    private long lastTimestamp = 0;

    private EventBus bus = EventBus.getDefault();

    public class MyBinder extends Binder {
        WDIDYLocationService getService() {
            Log.d("BMAN", "getService");
            return WDIDYLocationService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("BMAN", "onBind");
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d("DBG", "Service started");

        /*PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(
                locationManager.getmGoogleApiClient(),
                new LocationSettingsRequest.Builder().addLocationRequest(locationManager.getmLocationRequest()).build()
        );*/

        locationStatus = STATUS_IDLE;

        // Prevents from multiple registration if the service is recreated
        if (!bus.isRegistered(this)) {
            bus.register(this);
        }

        return Service.START_NOT_STICKY;
    }

    /**
     * Bus event requests management
     */
    @Subscribe
    public void onEvent(BusRequest request) {

        switch (request.getRequestType()) {

            case START_GPS_LOG:

                // Create a location manager instance if it's not already the case
                if (DataStore.getInstance().getLocationManager() == null) {

                    // Init Location API, and save instance
                    DataStore.getInstance().setLocationManager(new WDIDYLocationManager(getApplicationContext()));
                    DataStore.getInstance().getLocationManager().addListener(this);

                    Log.d("DBG", "Location manager created !");

                } else {

                    Log.d("DBG", "Location manager already created ! Connecting ...");
                    DataStore.getInstance().getLocationManager().connect();

                }

                // Set as running
                locationStatus = STATUS_RUNNING;

                // Init points array
                DataStore.getInstance().setGpsTrack(new GPSTrack(request.getData().getInt(Constants.BUNDLE_TRACK_ID)));

                break;

            case TERMINATE_GPS_LOG:

                Log.d("DBG", "Terminating GPS log. Got " + DataStore.getInstance().getGpsTrack().getGpsPoints().size() + " items");

                DataStore.getInstance().getLocationManager() .disconnect(); // terminate API usage for location
                locationStatus = STATUS_IDLE;

                // Hide visible log notification
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(notifID);

                // Send data to server
                new AsyncPoint().execute();

                break;
        }
    }

    /**
     * Below : Listeners called from GPS management class
     */

    @Override
    public void onConnecting() {

    }

    @Override
    public void onLocationChanged(Location location) {

        if (locationStatus == STATUS_RUNNING) {

            Log.d("SVC", "Got location : " + location.getLatitude() + ", " + location.getLongitude());

            // Show notification if running
            if (location != null) {

                if (lastTimestamp == 0) {
                    lastTimestamp = System.currentTimeMillis();
                }

                // Save location
                DataStore.getInstance().getGpsTrack().getGpsPoints().add(new GPSPoint(location, "--"));

                String content = "Δ = " + (System.currentTimeMillis() - lastTimestamp) + " ms \n" + LocationUtils.locationToString(getApplicationContext(), location);

                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_notif) // Only small icon for Lollipop
                        .setContentTitle(title)
                        .setContentText(content)
                        .setAutoCancel(false)
                        .setOngoing(true) // dismiss impossible
                        .addAction(android.R.drawable.stat_notify_sync_noanim, "Terminer", null)
                        .setColor(this.getResources().getColor(R.color.colorPrimary))
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(content));

                // Big icon for previous version (older than Lollipop)
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher));
                }

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                // Not a random notification ID : replace old notification for each update
                notificationManager.notify(notifID, notificationBuilder.build());

                // Get last timestamp
                lastTimestamp = System.currentTimeMillis();
            }

        } else {
            Log.e("SVC", "Not expecting any location ! Location status is not RUNNING !");
        }
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onLocationTimeout() {

    }

    /**
     * Async task to send array list of points to server
     */
    private class AsyncPoint extends AsyncTask<Integer, Integer, String> {

        private NotificationCompat.Builder notificationBuilder;
        private NotificationManager notificationManager;
        private int progress;

        @Override
        protected void onPreExecute() {

            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationBuilder = new NotificationCompat.Builder(WDIDYLocationService.this)
                    .setSmallIcon(R.drawable.ic_notif) // Only small icon for Lollipop
                    .setContentTitle(title)
                    .setContentText("Envoi des données en cours ...")
                    .setAutoCancel(false)
                    .setOngoing(true) // dismiss impossible
                    .setColor(WDIDYLocationService.this.getResources().getColor(R.color.colorPrimary));

            // Big icon for previous version (older than Lollipop)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(WDIDYLocationService.this.getResources(), R.mipmap.ic_launcher));
            }
        }

        @Override
        protected void onPostExecute(String data) {

            notificationManager.cancel(notifID);

            /*if (Utilities.isNetworkDataValid(data)) {

                try {
                    JSONObject jsonObject = new JSONObject(data);
                    if (jsonObject.getInt("error") == 0) {
                        Toast.makeText(LogGPSActivity.this, "Send !", Toast.LENGTH_SHORT).show();
                    } else {
                        new MaterialDialog.Builder(LogGPSActivity.this)
                                .title("Erreur")
                                .content("Cause : " + jsonObject.getString("cause"))
                                .negativeText("Fermer")
                                .show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(LogGPSActivity.this, "Erreur serveur", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(LogGPSActivity.this, "Erreur réseau", Toast.LENGTH_SHORT).show();
            }*/
        }

        @Override
        protected String doInBackground(Integer... param) {

            Log.d("ASY", " :::: Sending track ID : " + DataStore.getInstance().getGpsTrack().getTrackID());

            for (GPSPoint gpsPoint : DataStore.getInstance().getGpsTrack().getGpsPoints()) {

                publishProgress();

                String res = ConnexionUtils.postServerData(Constants.API_ADD_POINT, gpsPoint.getAPIformatted(Constants.API_KEY, DataStore.getInstance().getGpsTrack().getTrackID()));

                Log.d("ASY", " :::: " + progress + " :::: " + res);
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {

            progress++; // TODO check errors

            notificationBuilder.setProgress(DataStore.getInstance().getGpsTrack().getGpsPoints().size()+1, progress, false);
            notificationManager.notify(notifID, notificationBuilder.build());
        }
    }
}
