package com.wdidy.app.drawer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wdidy.app.R;

import com.wdidy.app.drawer.NavDrawerItem;

import java.util.ArrayList;
import java.util.TreeSet;

public class NavDrawerListAdapter extends BaseAdapter {

    // Custom definitions
    public static final int TYPE_DRAWER_ITEM = 0;
    public static final int TYPE_DRAWER_PROFILE = 1;
    public static final int TYPE_MAX_COUNT = TYPE_DRAWER_ITEM + TYPE_DRAWER_PROFILE + 1; // profile - itemlist

    private Context context;
	private ArrayList<NavDrawerItem> navDrawerItems;
    private Bitmap bmp; // faster

    // Pour garder en mémoire la position du profile
    private TreeSet<Integer> mSeparatorsSet = new TreeSet<Integer>();

    // Custom constructor
    public NavDrawerListAdapter(Context context, Bitmap bmp){
        this.context = context;
        this.navDrawerItems = new ArrayList<>();
        this.bmp = bmp;
    }

    // faster than in listadapter operation process
    public void setBitmap(Bitmap bmp) {
        this.bmp = bmp;
    }

    // Custom adders
    public void addProfileItem(String title, String id) {
        navDrawerItems.add(new NavDrawerItem(title, id));

        // save separator position
        mSeparatorsSet.add(navDrawerItems.size() - 1);
        notifyDataSetChanged();
    }

    public NavDrawerListAdapter(Context context, ArrayList<NavDrawerItem> navDrawerItems){
        this.context = context;
        this.navDrawerItems = navDrawerItems;
    }

    @Override
	public int getCount() {
		return navDrawerItems.size();
	}

    @Override
    public int getViewTypeCount() {
        return TYPE_MAX_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        return navDrawerItems.get(position).isProfile() ? TYPE_DRAWER_PROFILE : TYPE_DRAWER_ITEM;
    }

	@Override
	public Object getItem(int position) {		
		return navDrawerItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

        NavDrawerItem ndi = navDrawerItems.get(position);

		if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

            convertView = mInflater.inflate(R.layout.drawer_list_item, null);
        }

        ImageView imgIcon = (ImageView) convertView.findViewById(R.id.icon);
        TextView txtTitle = (TextView) convertView.findViewById(R.id.title);

        imgIcon.setImageResource(ndi.getIcon());
        txtTitle.setText(ndi.getTitle());

        return convertView;
	}

}
