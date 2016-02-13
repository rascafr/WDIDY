package com.wdidy.app;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.wdidy.app.account.UserAccount;
import com.wdidy.app.slidingtab.SlidingTabLayout;
import com.wdidy.app.slidingtab.ViewPagerAdapter;

/**
 * Created by Rascafr on 13/02/2016.
 */
public class MainActivityTab extends AppCompatActivity {

    // Navigation Sliding Tabs
    private ViewPager mPager;
    private SlidingTabLayout mTabs;
    private ViewPagerAdapter mAdapter;

    // Others / Android
    private Context context;
    private UserAccount userAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = MainActivityTab.this;
        userAccount = new UserAccount();
        userAccount.readAccountPromPrefs(context);

        // Set UI Main Layout
        setContentView(R.layout.activity_main);

        // Set sliding tabs objects
        CharSequence mTitles[] = getResources().getStringArray(R.array.tab_names);

        mPager = (ViewPager) findViewById(R.id.home_fragment_pager);
        mAdapter = new ViewPagerAdapter(getSupportFragmentManager(), mTitles, mTitles.length);
        mPager.setAdapter(mAdapter);
        mTabs = (SlidingTabLayout) findViewById(R.id.home_fragment_tabs);
        mTabs.setDistributeEvenly(true);
        mTabs.setViewPager(mPager);

        // Verify GPS permissions
        verifyGPSPermissions(MainActivityTab.this);
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
            userAccount.removeAccount(context);
            Intent i = new Intent(context, LoginActivity.class);
            startActivity(i);
            finish();
            return true;
        } else if (id == R.id.action_settings) {
            Intent i = new Intent(context, SettingsActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // GPS Permissions
    private final static int PERMISSION_REQUEST_CODE = 42;
    private static String[] PERMISSIONS_LOCATION = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    /**
     * Checks if the app has permission to use GPS
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyGPSPermissions(Activity activity) {
        // Check if we have write permission
        int permission_fine = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);
        int permission_coarse = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (permission_fine != PackageManager.PERMISSION_GRANTED || permission_coarse != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_LOCATION,
                    PERMISSION_REQUEST_CODE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay!
                    Toast.makeText(context, "Permission GPS accordée ! :)", Toast.LENGTH_SHORT).show();

                } else {

                    // permission denied, boo!
                    new MaterialDialog.Builder(context)
                            .title("Faites-nous confiance")
                            .content("WDIDY a besoin d'utiliser le GPS pour enregistrer vos soirées.\nVous devez accepter la demande de permission ...")
                            .cancelable(false)
                            .negativeText("Refuser")
                            .positiveText("Réessayer")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                                    verifyGPSPermissions(MainActivityTab.this);
                                }
                            })
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                                    MainActivityTab.this.finish();
                                }
                            })
                            .show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}