package com.wdidy.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

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
}