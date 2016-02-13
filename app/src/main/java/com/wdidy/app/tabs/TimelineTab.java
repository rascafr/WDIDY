package com.wdidy.app.tabs;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.melnykov.fab.FloatingActionButton;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.wdidy.app.Constants;
import com.wdidy.app.LogGPSActivity;
import com.wdidy.app.MapActivity;
import com.wdidy.app.R;
import com.wdidy.app.account.UserAccount;
import com.wdidy.app.listeners.RecyclerItemClickListener;
import com.wdidy.app.track.DataManager;
import com.wdidy.app.track.TrackItem;
import com.wdidy.app.utils.ConnexionUtils;
import com.wdidy.app.utils.Utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Rascafr on 13/02/2016.
 */
public class TimelineTab extends Fragment {

    // Adapter - RecyclerView
    private TrackAdapter mAdapter;
    private RecyclerView recyList;

    // UI Layout
    private ProgressBar progressNewTrack;
    private ProgressBar pgLoading;
    private View viewNewTrack;
    private FloatingActionButton fabNewTrack;
    private CircleImageView profileCircleView;

    // Model
    private ArrayList<TrackItem> trackItems;

    // User profile
    UserAccount userAccount;

    // Android
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_tracks, container, false);

        context = getActivity();

        // Init model
        DataManager.getInstance().initTracksItems();
        trackItems = DataManager.getInstance().getTrackItems();

        // Init profile
        userAccount = new UserAccount();
        userAccount.readAccountPromPrefs(context);

        // Init RecyclerView
        recyList = (RecyclerView) rootView.findViewById(R.id.recyList);
        recyList.setHasFixedSize(true);

        // Create adapter and assign it to RecyclerView
        mAdapter = new TrackAdapter();
        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyList.setLayoutManager(llm);
        recyList.setAdapter(mAdapter);

        // Init UI
        pgLoading = (ProgressBar) rootView.findViewById(R.id.progressLoading);
        pgLoading.setVisibility(View.INVISIBLE);
        progressNewTrack = (ProgressBar) rootView.findViewById(R.id.progressCreate);
        viewNewTrack = rootView.findViewById(R.id.viewCircle);
        fabNewTrack = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fabNewTrack.attachToRecyclerView(recyList);
        progressNewTrack.setVisibility(View.INVISIBLE);
        progressNewTrack.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
        viewNewTrack.setVisibility(View.INVISIBLE);
        profileCircleView = (CircleImageView) rootView.findViewById(R.id.profileCircleView);

        // UNIVERSAL IMAGE LOADER SETUP
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisc(true).cacheInMemory(true)
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                .displayer(new FadeInBitmapDisplayer(300)).build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                getActivity())
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .discCacheSize(100 * 1024 * 1024).build();

        ImageLoader.getInstance().init(config);
        // END - UNIVERSAL IMAGE LOADER SETUP

        // Set profile image
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .resetViewBeforeLoading(true)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .build();
        ImageLoader imgLoad = ImageLoader.getInstance();
        imgLoad.displayImage(Constants.URL_PROFILE_PICTS + userAccount.getUserID() + ".jpg", profileCircleView, options);

        // Download data from server
        AsyncTracks asyncTracks = new AsyncTracks();
        asyncTracks.execute(userAccount.getUserID());

        // On click listener → see Map
        recyList.addOnItemTouchListener(new RecyclerItemClickListener(context, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent i = new Intent(context, MapActivity.class);
                i.putExtra(Constants.INTENT_TRACK_ID, position);
                startActivity(i);
            }
        }));

        // On long clic listener → edit map
        //recyList.

        // On floating button click listener → new track
        fabNewTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(context)
                        .title("Nouveau trajet")
                        .content("Entrez un nom pour votre nouveau trajet (vous pourrez le modifier plus tard)")
                        .input("Soirée, balade, ...", "", false, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog materialDialog, CharSequence trackName) {
                                Utilities.hideSoftKeyboard(getActivity());
                                AsyncNewTrack asyncNewTrack = new AsyncNewTrack();
                                asyncNewTrack.execute(trackName.toString());
                            }
                        })
                        .show();
            }
        });

        // Verify GPS permissions
        verifyGPSPermissions(getActivity());

        return rootView;
    }

    /**
     * Custom adapter for track item
     */
    private class TrackAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        public int getItemCount() {
            return trackItems == null ? 0 : trackItems.size();
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            TrackItem ti = trackItems.get(position);

            TrackViewHolder hvh = (TrackViewHolder) viewHolder;
            hvh.vTitle.setText(ti.getName());
            hvh.vInfo.setText(ti.getStart() + " ↔ " + ti.getEnd());
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

            return new TrackViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_track_item, viewGroup, false));
        }

        // Classic View Holder for history item
        public class TrackViewHolder extends RecyclerView.ViewHolder {

            protected TextView vTitle, vInfo;

            public TrackViewHolder(View v) {
                super(v);
                vTitle = (TextView) v.findViewById(R.id.tvTrackName);
                vInfo = (TextView) v.findViewById(R.id.tvTrackInfo);
            }
        }

    }

    /**
     * Async task to download data from server
     */
    private class AsyncTracks extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pgLoading.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);
            pgLoading.setVisibility(View.INVISIBLE);

            if (Utilities.isNetworkDataValid(data)) {

                try {
                    JSONObject jsonObject = new JSONObject(data);
                    if (jsonObject.getInt("error") == 0) {
                        JSONArray array = jsonObject.getJSONArray("data");

                        for (int i=0;i<array.length();i++) {
                            trackItems.add(new TrackItem(array.getJSONObject(i)));
                        }

                        mAdapter.notifyDataSetChanged();

                    } else {

                        new MaterialDialog.Builder(context)
                                .title("Erreur")
                                .content("Cause : " + jsonObject.getString("cause"))
                                .negativeText("Fermer")
                                .show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Erreur serveur", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Erreur réseau", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected String doInBackground(String... param) {
            HashMap<String, String> pairs = new HashMap<>();
            pairs.put("api_id", Constants.API_KEY);
            pairs.put("user_id", param[0]);
            return ConnexionUtils.postServerData(Constants.API_LIST_TRACKS, pairs);
        }
    }

    /**
     * Async task to create a new track
     */
    private class AsyncNewTrack extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressNewTrack.setVisibility(View.VISIBLE);
            fabNewTrack.setVisibility(View.INVISIBLE);
            viewNewTrack.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);

            progressNewTrack.setVisibility(View.INVISIBLE);
            fabNewTrack.setVisibility(View.VISIBLE);
            viewNewTrack.setVisibility(View.INVISIBLE);

            if (Utilities.isNetworkDataValid(data)) {

                try {
                    JSONObject jsonObject = new JSONObject(data);
                    if (jsonObject.getInt("error") == 0) {
                        int track_id = jsonObject.getJSONObject("data").getInt("track_id");
                        Toast.makeText(context, "Current track ID : " + track_id, Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(context, LogGPSActivity.class);
                        i.putExtra(Constants.INTENT_TRACK_ID, track_id);
                        startActivity(i);
                    } else {
                        new MaterialDialog.Builder(context)
                                .title("Erreur")
                                .content("Cause : " + jsonObject.getString("cause"))
                                .negativeText("Fermer")
                                .show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Erreur serveur", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Erreur réseau", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected String doInBackground(String... param) {
            HashMap<String, String> pairs = new HashMap<>();
            try {
                pairs.put("api_id", Constants.API_KEY);
                pairs.put("user_id", userAccount.getUserID());
                pairs.put("track_name", URLEncoder.encode(param[0], "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                // On fail ?... Possible ?
            }
            return ConnexionUtils.postServerData(Constants.API_NEW_TRACK, pairs);
        }
    }

    // TODO following goes in MainActivity

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
                                    verifyGPSPermissions(getActivity());
                                }
                            })
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                                    getActivity().finish();
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