package com.wdidy.app.gps;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by root on 05/04/16.
 */
public class WDIDYLocationManager implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    /** Location / timeout constants **/
    public static final int REQUEST_INTERVAL = 3000;
    public static final int REQUEST_FASTEST_INTERVAL = 2000;

    /** Google Location API */
    private GoogleApiClient mGoogleApiClient;

    /** Last location */
    private Location mLastLocation;

    /** Our location request, client and manager */
    private LocationRequest mLocationRequest;
    private LocationManager mLocationManager;

    /** Accuracy of location */
    private int locationMillisInterval;
    private int locationMillisFastInterval;

    /** Timer Handler */
    private Handler mHandler;

    /** Activity's context */
    private Context context;

    /** Location listeners. */
    private Set<WDIDYLocationListener> mListenerList;

    /** Debug */
    private boolean DBG = true;
    private String LOG_TAG = "GPS_DBG";

    /** Current service status. */
    private boolean mServiceEnabled;

    /** Interfaces */
    public static interface WDIDYLocationListener {

        void onConnecting();

        void onLocationChanged(Location location);

        void onDisconnected();

        void onLocationTimeout();
    }

    public GoogleApiClient getmGoogleApiClient() {
        return mGoogleApiClient;
    }

    public LocationRequest getmLocationRequest() {
        return mLocationRequest;
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("DBG", "onConnectionSuspended");
    }

    protected void createLocationRequest() {
        Log.d("DBG", "createLocationRequest");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(REQUEST_INTERVAL);
        mLocationRequest.setFastestInterval(REQUEST_FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        Log.d("DBG", "startLocationUpdates");
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        Log.d("DBG", "stopLocationUpdates");
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    private Runnable mTimeOutRunnable = new Runnable() {
        @Override
        public void run() {
            if (mLastLocation == null) {
                dispatchOnLocationTimeout();
            }
        }
    };

    public WDIDYLocationManager(final Context context) {
        this.context = context;
        Log.d("DBG", "building GoogleApiClient ...");
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        mListenerList = new HashSet<WDIDYLocationListener>();
        //mHandler = new Handler();
        createLocationRequest();
    }

    public void addListener(final WDIDYLocationListener locationListener) {
        if (DBG)
            Log.d(LOG_TAG, "addListener " + locationListener.getClass().getSimpleName());

        mListenerList.add(locationListener);

        if (isEnabled()) {
            if (!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) {
                Log.d(LOG_TAG, "connect() ...");
                connect();
            } else {
                Location lastLocation = getLastLocation();
                if (lastLocation != null) {
                    locationListener.onLocationChanged(lastLocation);
                }
            }
        } else {
            locationListener.onDisconnected();
        }
    }

    public void removeListener(final WDIDYLocationListener locationListener) {
        if (DBG)
            Log.d(LOG_TAG, "removeListener " + locationListener.getClass().getSimpleName());

        mListenerList.remove(locationListener);

        if (mListenerList.isEmpty()) {
            disconnect();
        }
    }

    public void onResume() {
        mHandler.removeCallbacks(mTimeOutRunnable);

        boolean isEnabled = isEnabled();
        if (mServiceEnabled != isEnabled) {
            if (isEnabled) {
                if (!mGoogleApiClient.isConnecting()) {
                    mGoogleApiClient.connect();
                    dispatchOnConnecting();
                }
            } else {
                dispatchOnDisconnect();
            }
            mServiceEnabled = isEnabled;
        }
    }

    public void connect() {
        if (isEnabled()) {
            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
                dispatchOnConnecting();
                mServiceEnabled = true;
            }
        } else {
            mServiceEnabled = false;
        }
    }

    public void disconnect() {
        mGoogleApiClient.disconnect();
    }

    public boolean isEnabled() {
        return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public Location getLastLocation() {
        if (mGoogleApiClient.isConnected()) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return null;
            }
            final Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            return (lastLocation == null) ? mLastLocation : lastLocation;
        } else {
            return mLastLocation;
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (DBG)
            Log.d(LOG_TAG, "onConnected");

        createLocationRequest();

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            Log.d(LOG_TAG, "Last know location : " + mLastLocation.getLatitude() + ", " + mLastLocation.getLongitude());
        }
        startLocationUpdates();
        //mHandler.postDelayed(mTimeOutRunnable, 100000);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (DBG)
            Log.e(LOG_TAG, "onConnectionFailed " + result.getErrorCode());
        dispatchOnDisconnect();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (DBG)
            Log.d(LOG_TAG, "onLocationChanged " + location);
        if (location != null) {
            dispatchOnLocationChanged(location);
            mLastLocation = location;
        }
    }

    /*
    @Override
    public void onDisconnected() {
        dispatchOnDisconnect();
    }*/

    /** Dispatchers */
    private void dispatchOnLocationChanged(Location location) {
        for (WDIDYLocationListener l : mListenerList) {
            l.onLocationChanged(location);
        }
    }

    private void dispatchOnDisconnect() {
        for (WDIDYLocationListener l : mListenerList) {
            l.onDisconnected();
        }
    }

    private void dispatchOnConnecting() {
        for (WDIDYLocationListener l : mListenerList) {
            l.onConnecting();
        }
    }

    private void dispatchOnLocationTimeout() {
        for (WDIDYLocationListener l : mListenerList) {
            l.onLocationTimeout();
        }
    }
}
