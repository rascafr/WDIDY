package com.wdidy.app;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.melnykov.fab.FloatingActionButton;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
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
 * Created by Rascafr on 06/11/2015.
 */
public class TracksListActivity extends AppCompatActivity {

    // Adapter - RecyclerView
    private TrackAdapter mAdapter;
    private RecyclerView recyList;

    // UI Layout
    private ProgressBar progressNewTrack;
    private View viewNewTrack;
    private FloatingActionButton fabNewTrack;
    private CircleImageView profileCircleView;

    // Model
    private ArrayList<TrackItem> trackItems;

    // User profile
    UserAccount userAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracks);

        // Init model
        DataManager.getInstance().initTracksItems();
        trackItems = DataManager.getInstance().getTrackItems();

        // Init profile
        userAccount = new UserAccount();
        userAccount.readAccountPromPrefs(this);

        // Init RecyclerView
        recyList = (RecyclerView) findViewById(R.id.recyList);
        recyList.setHasFixedSize(true);

        // Create adapter and assign it to RecyclerView
        mAdapter = new TrackAdapter();
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyList.setLayoutManager(llm);
        recyList.setAdapter(mAdapter);

        // Init UI
        progressNewTrack = (ProgressBar) findViewById(R.id.progressLoading);
        viewNewTrack = findViewById(R.id.viewCircle);
        fabNewTrack = (FloatingActionButton) findViewById(R.id.fab);
        fabNewTrack.attachToRecyclerView(recyList);
        progressNewTrack.setVisibility(View.INVISIBLE);
        progressNewTrack.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
        viewNewTrack.setVisibility(View.INVISIBLE);
        profileCircleView = (CircleImageView) findViewById(R.id.profileCircleView);
        profileCircleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TracksListActivity.this, FriendsActivity.class);
                TracksListActivity.this.startActivity(i);
            }
        });

        // UNIVERSAL IMAGE LOADER SETUP
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisc(true).cacheInMemory(true)
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                .displayer(new FadeInBitmapDisplayer(300)).build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                getApplicationContext())
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
        recyList.addOnItemTouchListener(new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent i = new Intent(TracksListActivity.this, MapActivity.class);
                i.putExtra(Constants.INTENT_TRACK_ID, position);
                TracksListActivity.this.startActivity(i);
            }
        }));

        // On long clic listener → edit map
        //recyList.

        // On floating button click listener → new track
        fabNewTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(TracksListActivity.this)
                        .title("Nouveau trajet")
                        .content("Entrez un nom pour votre nouveau trajet (vous pourrez le modifier plus tard)")
                        .input("Soirée, balade, ...", "", false, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog materialDialog, CharSequence trackName) {
                                Utilities.hideSoftKeyboard(TracksListActivity.this);
                                AsyncNewTrack asyncNewTrack = new AsyncNewTrack();
                                asyncNewTrack.execute(trackName.toString());
                            }
                        })
                        .show();
            }
        });

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
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);

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

                        new MaterialDialog.Builder(TracksListActivity.this)
                                .title("Erreur")
                                .content("Cause : " + jsonObject.getString("cause"))
                                .negativeText("Fermer")
                                .show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(TracksListActivity.this, "Erreur serveur", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(TracksListActivity.this, "Erreur réseau", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(TracksListActivity.this, "Current track ID : " + track_id, Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(TracksListActivity.this, LogGPSActivity.class);
                        i.putExtra(Constants.INTENT_TRACK_ID, track_id);
                        TracksListActivity.this.startActivity(i);
                    } else {
                        new MaterialDialog.Builder(TracksListActivity.this)
                                .title("Erreur")
                                .content("Cause : " + jsonObject.getString("cause"))
                                .negativeText("Fermer")
                                .show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(TracksListActivity.this, "Erreur serveur", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(TracksListActivity.this, "Erreur réseau", Toast.LENGTH_SHORT).show();
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
}
