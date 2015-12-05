package com.wdidy.app;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;

import com.wdidy.app.R;
import com.wdidy.app.drawer.NavDrawerItem;
import com.wdidy.app.drawer.NavDrawerListAdapter;
import com.wdidy.app.fragment.FragmentTest;

import java.util.ArrayList;

/**
 * Created by Rascafr on 06/11/2015.
 */
public class MainActivityDrawer extends AppCompatActivity {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private DrawerLayout mDrawerLayout;
    private RecyclerView recList;
    private ActionBarDrawerToggle mDrawerToggle;

    // Material Toolbar

    // slide menu items
    private String[] navMenuTitles;
    private TypedArray navMenuIcons;
    private ListView mDrawerList;
    private NavDrawerListAdapter navAdapter;
    private ArrayList<NavDrawerItem> navDrawerItems;
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        // Global UI View

        // load slide menu items
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

        // nav drawer icons from resources
        navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

        navDrawerItems = new ArrayList<>();

        // adding nav drawer items to array
        for (int it=0;it<navMenuTitles.length;it++)
            navDrawerItems.add(new NavDrawerItem(navMenuTitles[it], navMenuIcons.getResourceId(it, -1)));

        // Recycle the typed array

        // setting the nav drawer list adapter
        navAdapter = new NavDrawerListAdapter(getApplicationContext(), navDrawerItems);

        // set data adapter to our listview
        mDrawerList.setAdapter(navAdapter);

        displayView(0);
    }

    /**
     * Diplaying fragment view for selected nav drawer list item
     * */
    private void displayView(int position) {
        // update the main content by replacing fragments
        Fragment fragment = null;

        fragment = new FragmentTest();

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.frame_container, fragment, "frag" + position).commit();

            // update selected item and title, then close the drawer
            mDrawerList.setItemChecked(position, true);
            mDrawerList.setSelection(position);
            mDrawerList.setItemsCanFocus(true);
            setTitle(navMenuTitles[position]);
/*
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDrawerLayout.closeDrawer(mDrawerList);
                }
            }, 100);*/


        } else {
            // error in creating fragment
            //Log.e("ESEOmega", "Error in creating fragment");
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }
}
