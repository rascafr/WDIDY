package com.wdidy.app.tabs;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wdidy.app.R;

/**
 * Created by Rascafr on 13/02/2016.
 */
public class MessagesTab extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_empty, container, false);

        return rootView;
    }
}