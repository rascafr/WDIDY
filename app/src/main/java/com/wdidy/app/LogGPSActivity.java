package com.wdidy.app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.wdidy.app.account.UserAccount;
import com.wdidy.app.utils.ConnexionUtils;
import com.wdidy.app.utils.Utilities;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class LogGPSActivity extends AppCompatActivity implements LocationListener, GpsStatus.Listener {

    private double latitude, longitude;
    private String address;
    private TextView tvTemp, tvUserInfo;
    private LocationUtils locationUtils;
    private LocationManager mLocationManager;
    private ProgressBar progressLocalisation;
    private String provider;
    private boolean fromWhat;
    private SharedPreferences prefs_Read;
    private SharedPreferences.Editor prefs_Write;
    private CheckBox checkGPS;
    private int trackID;
    private Vibrator v;

    // User account
    private UserAccount userAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get preferences
        prefs_Read = getSharedPreferences(Constants.PREFS_ACCOUNT_KEY, 0);
        prefs_Write = prefs_Read.edit();

        // Get vibrator object
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // UI Objects
        tvTemp = (TextView) findViewById(R.id.tvTemp);
        tvUserInfo = (TextView) findViewById(R.id.tvUserInfo);
        tvTemp.setText("Localisation ...");
        progressLocalisation = (ProgressBar) findViewById(R.id.progressWait);
        progressLocalisation.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
        checkGPS = (CheckBox) findViewById(R.id.chkGps);
        checkGPS.setChecked(prefs_Read.getBoolean(Constants.PREFS_GPS_PROVIDER, false));
        checkGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefs_Write.putBoolean(Constants.PREFS_GPS_PROVIDER, !prefs_Read.getBoolean(Constants.PREFS_GPS_PROVIDER, false));
                prefs_Write.apply();
                //Toast.makeText(MainActivity.this, "Preference saved !", Toast.LENGTH_SHORT).show();
                Snackbar.make(v, "Preference saved !", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                mLocationManager.removeUpdates(LogGPSActivity.this);
                mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for Activity#requestPermissions for more details.
                        return;
                    }
                }
                if (!prefs_Read.getBoolean(Constants.PREFS_GPS_PROVIDER, false)) {
                    mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, LogGPSActivity.this);
                    Toast.makeText(LogGPSActivity.this, "Mode : NETWORK", Toast.LENGTH_SHORT).show();
                } else {
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, LogGPSActivity.this);
                    Toast.makeText(LogGPSActivity.this, "Mode : GPS", Toast.LENGTH_SHORT).show();
                }
                progressLocalisation.setVisibility(View.VISIBLE);
            }
        });

        // GPS objects
        locationUtils = new LocationUtils(getApplicationContext());
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        provider = mLocationManager.getBestProvider(criteria, false);
        Location location = mLocationManager.getLastKnownLocation(provider);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
        }
        /*mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);*/
        onLocationChanged(location);

        // Display user informations
        userAccount = new UserAccount();
        userAccount.readAccountPromPrefs(this);
        tvUserInfo.setText(userAccount.getName() + "\n" + userAccount.getCity() + " - " + userAccount.getCountry() + "\n" + userAccount.getUserID());

        // Get parameters
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                Toast.makeText(LogGPSActivity.this, "Erreur de l'application (c'est pas normal)", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                trackID = extras.getInt(Constants.INTENT_TRACK_ID);
            }
        } else {
            trackID = (int) savedInstanceState.getSerializable(Constants.INTENT_TRACK_ID);
        }

        Toast.makeText(LogGPSActivity.this, "Receive : " + trackID, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
        }
        mLocationManager.removeUpdates(this);
        progressLocalisation.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
        }
        if (!prefs_Read.getBoolean(Constants.PREFS_GPS_PROVIDER, false)) {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
            Toast.makeText(LogGPSActivity.this, "Mode : NETWORK", Toast.LENGTH_SHORT).show();
        } else {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            Toast.makeText(LogGPSActivity.this, "Mode : GPS", Toast.LENGTH_SHORT).show();
        }
        progressLocalisation.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            userAccount.removeAccount(LogGPSActivity.this);
            Intent i = new Intent(LogGPSActivity.this, LoginActivity.class);
            LogGPSActivity.this.startActivity(i);
            LogGPSActivity.this.finish();
            return true;
        } else if (id == R.id.action_tracks_list) {
            Intent i = new Intent(LogGPSActivity.this, TracksListActivity.class);
            LogGPSActivity.this.startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * To handle location's changes
     */
    @Override
    public void onLocationChanged(Location location) {

        if (location != null) {

            // Set location's values
            this.latitude = location.getLatitude();
            this.longitude = location.getLongitude();
            this.address = locationUtils.getCompleteAddressString(latitude, longitude);

            // display infos
            tvTemp.setText("Location :\nLat = " + this.latitude + ", Lon = " + this.longitude +
                    "\nAltitude : " + location.getAltitude() + " m" +
                    "\nAddress : " + this.address + "\nSpeed : " + location.getSpeed() * 3.6 + " km/h" +
                    "\nAccuracy : " + location.getAccuracy() + " m" +
                    "\nProvider : " + location.getProvider());
            progressLocalisation.setVisibility(View.GONE);

            // get current date
            SimpleDateFormat dfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar c = Calendar.getInstance();
            String date = dfDate.format(c.getTime());

            // notify the user
            v.vibrate(150);

            // send point to server
            AsyncPoint asyncPoint = new AsyncPoint();
            asyncPoint.execute(this.latitude + "", this.longitude + "", date, this.address);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "Enabled new provider " + provider,
                Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Disabled provider " + provider,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onGpsStatusChanged(int event) {

    }

    /**
     * Async task to download data from server
     */
    private class AsyncPoint extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);

            if (Utilities.isNetworkDataValid(data)) {

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
            }
        }

        @Override
        protected String doInBackground(String... param) {
            HashMap<String, String> pairs = new HashMap<>();
            pairs.put("api_id", Constants.API_KEY);
            pairs.put("track_id", "" + trackID);
            pairs.put("latitude", param[0]);
            pairs.put("longitude", param[1]);
            pairs.put("date_point", param[2]);
            pairs.put("address", param[3]);
            return ConnexionUtils.postServerData(Constants.API_ADD_POINT, pairs);
        }
    }
}
