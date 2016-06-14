package com.wdidy.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.wdidy.app.account.UserAccount;
import com.wdidy.app.bus.BusRequest;
import com.wdidy.app.bus.BusType;
import com.wdidy.app.gps.WDIDYLocationManager;

import org.greenrobot.eventbus.EventBus;

public class LogGPSActivity extends AppCompatActivity {

    private double latitude, longitude;
    private String address;
    private TextView tvTemp, tvUserInfo;
    private Button bpTerminate;
    private ProgressBar progressLocalisation;
    private String provider;
    private boolean fromWhat;
    private SharedPreferences prefs_Read;
    private SharedPreferences.Editor prefs_Write;
    private int trackID;
    private Vibrator v;
    private WDIDYLocationManager locationManager;

    // User account
    private UserAccount userAccount;

    // Bus
    private EventBus bus = EventBus.getDefault();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_gps);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get preferences
        prefs_Read = getSharedPreferences(Constants.PREFS_ACCOUNT_KEY, 0);
        prefs_Write = prefs_Read.edit();

        // Get vibrator object
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // UI Objects
        tvTemp = (TextView) findViewById(R.id.tvTemp);
        tvUserInfo = (TextView) findViewById(R.id.tvUserInfo);
        bpTerminate = (Button) findViewById(R.id.bpGPSTerminate);
        tvTemp.setText("Localisation ...");
        progressLocalisation = (ProgressBar) findViewById(R.id.progressWait);
        progressLocalisation.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);

        bpTerminate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bus.post(new BusRequest(BusType.TERMINATE_GPS_LOG));
                finish();
            }
        });

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
        progressLocalisation.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressLocalisation.setVisibility(View.VISIBLE);
    }


    /**
     * To handle location's changes
     */
    /*@Override
    public void onLocationChanged(Location location) {

        if (location != null) {

            // Set location's values
            this.latitude = location.getLatitude();
            this.longitude = location.getLongitude();

            progressLocalisation.setVisibility(View.GONE);

            Log.d("DBG", "onLocationChanged ! " + latitude + "," + longitude);

            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses;

            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
                this.address = addresses.get(0).getAddressLine(0);
            } catch (IOException e) {
                this.address = "Exception : Address cannot be processed";
                e.printStackTrace();
            }

            // display infos
            tvTemp.setText("Location :\nLat = " + this.latitude + ", Lon = " + this.longitude +
                    "\nAltitude : " + location.getAltitude() + " m" +
                    "\nAddress : " + this.address + "\nSpeed : " + location.getSpeed() * 3.6 + " km/h" +
                    "\nAccuracy : " + location.getAccuracy() + " m" +
                    "\nProvider : " + location.getProvider());


            // get current date
            SimpleDateFormat dfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar c = Calendar.getInstance();
            String date = dfDate.format(c.getTime());

            // notify the user
            //v.vibrate(150);

            // send point to server
            //AsyncPoint asyncPoint = new AsyncPoint();
            //asyncPoint.execute(this.latitude + "", this.longitude + "", date, this.address);
        }
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                LogGPSActivity.this.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
