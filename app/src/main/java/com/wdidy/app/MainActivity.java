package com.wdidy.app;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.wdidy.app.R;
import com.wdidy.app.account.UserAccount;

public class MainActivity extends AppCompatActivity implements LocationListener, GpsStatus.Listener {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs_Read = getSharedPreferences(Constants.PREFS_ACCOUNT_KEY, 0);
        prefs_Write = prefs_Read.edit();

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
                prefs_Write.commit();
                //Toast.makeText(MainActivity.this, "Preference saved !", Toast.LENGTH_SHORT).show();
                Snackbar.make(v, "Preference saved !", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
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
        UserAccount userAccount = new UserAccount();
        userAccount.readAccountPromPrefs(this);
        tvUserInfo.setText(userAccount.getName() + "\n" + userAccount.getCity() + " - " + userAccount.getCountry());
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
            Toast.makeText(MainActivity.this, "Mode : NETWORK", Toast.LENGTH_SHORT).show();
        } else {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            Toast.makeText(MainActivity.this, "Mode : GPS", Toast.LENGTH_SHORT).show();
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
        if (id == R.id.action_settings) {
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
                    "\nAddress : " + this.address + "\nSpeed : " + location.getSpeed()*3.6 + " km/h" +
                    "\nAccuracy : " + location.getAccuracy() + " m" +
                    "\nProvider : " + location.getProvider());
            progressLocalisation.setVisibility(View.GONE);
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
}
