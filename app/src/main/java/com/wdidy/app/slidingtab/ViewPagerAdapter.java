package com.wdidy.app.slidingtab;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.wdidy.app.tabs.TimelineTab;
import com.wdidy.app.tabs.MessagesTab;
import com.wdidy.app.tabs.FriendsTab;


public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    private CharSequence titles[]; // This will Store the titles of the Tabs which are Going to be passed when ViewPagerAdapter is created
    private int numbOfTabs; // Store the number of tabs, this will also be passed when the ViewPagerAdapter is created

    // Build a Constructor and assign the passed Values to appropriate values in the class
    public ViewPagerAdapter(FragmentManager fm, CharSequence titles[], int mNumbOfTabsumb) {
        super(fm);

        this.titles = titles;
        this.numbOfTabs = mNumbOfTabsumb;
    }

    //This method return the fragment for the every position in the View Pager
    @Override
    public Fragment getItem(int position) {

        switch (position) {

            case 0:
                return new TimelineTab();

            case 1:
                return new MessagesTab();

            case 2:
                return new FriendsTab();
        }
        return null;
    }

    // This method return the titles for the Tabs in the Tab Strip
    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];

    }

    // This method return the Number of tabs for the tabs Strip
    @Override
    public int getCount() {
        return numbOfTabs;
    }
}