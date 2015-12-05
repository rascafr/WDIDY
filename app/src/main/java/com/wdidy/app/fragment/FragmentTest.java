package com.wdidy.app.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wdidy.app.R;

/**
 * Created by Rascafr on 06/11/2015.
 */
public class FragmentTest extends Fragment {

    @Override
    public View onCreateView(LayoutInflater rootInfl, ViewGroup container, Bundle savedInstanceState) {

        // UI
        View rootView = rootInfl.inflate(R.layout.fragment_test, container, false);

        return rootView;
    }
}
