package com.wdidy.app;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.wdidy.app.track.DataManager;
import com.wdidy.app.track.PointItem;
import com.wdidy.app.track.TrackItem;
import com.wdidy.app.utils.ConnexionUtils;
import com.wdidy.app.utils.Utilities;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Rascafr on 03/11/2015.
 */

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    // Track ID
    private int trackID;
    private TrackItem trackItem;

    // Layout objects
    private TextView tvResumeName, tvResumeInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Get layout objects
        tvResumeName = (TextView) findViewById(R.id.tvResumeName);
        tvResumeInfo = (TextView) findViewById(R.id.tvResumeInfo);

        // Get parameters
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                Toast.makeText(MapActivity.this, "Erreur de l'application (c'est pas normal)", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                trackID = extras.getInt(Constants.INTENT_TRACK_ID);
            }
        } else {
            trackID = (int) savedInstanceState.getSerializable(Constants.INTENT_TRACK_ID);
        }

        trackItem = DataManager.getInstance().getTrackItems().get(trackID);

        // Set title
        tvResumeName.setText("Trajet \"" + trackItem.getName() + "\"");
        tvResumeInfo.setText("Début : " + trackItem.getStart() + "\nFin : " + trackItem.getEnd());

        // Init map object
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {

        // Download points
        AsyncMap asyncMap = new AsyncMap(map);
        asyncMap.execute(String.valueOf(trackItem.getIDtrack()));

    }

    /**
     * Async task to download data from server
     */
    private class AsyncMap extends AsyncTask<String, String, String> {

        private GoogleMap map;
        private ArrayList<LatLng> points;

        public AsyncMap (GoogleMap map) {
            this.map = map;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Init points
            points = new ArrayList<>();
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);

            if (Utilities.isNetworkDataValid(data)) {

                try {
                    JSONObject jsonObject = new JSONObject(data);
                    if (jsonObject.getInt("error") == 0) {
                        JSONArray array = jsonObject.getJSONArray("data");
                        for (int i = 0; i < array.length(); i++) {
                            PointItem pointItem = new PointItem(array.getJSONObject(i));
                            points.add(new LatLng(pointItem.getLat(), pointItem.getLon()));
                        }

                        map.setMyLocationEnabled(true);

                        if (points.size() > 0) {
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(points.get(0), 17));

                            PolylineOptions polylineOptions = new PolylineOptions();
                            for (int i = 0; i < points.size(); i++) {
                                polylineOptions.add(points.get(i));
                            }
                            map.addPolyline(polylineOptions
                                    .width(5)
                                    .color(Color.RED));

                            // Start
                            map.addMarker(new MarkerOptions()
                                    .position(points.get(0))
                                    .title("Début : " + trackItem.getStart()));

                            // Start
                            map.addMarker(new MarkerOptions()
                                    .position(points.get(points.size() - 1))
                                    .title("Fin : " + trackItem.getEnd()));
                        }
                    } else {

                        new MaterialDialog.Builder(MapActivity.this)
                                .title("Erreur")
                                .content("Cause : " + jsonObject.getString("cause"))
                                .negativeText("Fermer")
                                .show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(MapActivity.this, "Erreur serveur", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MapActivity.this, "Erreur réseau", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected String doInBackground(String... param) {
            HashMap<String, String> pairs = new HashMap<>();
            pairs.put("api_id", Constants.API_KEY);
            pairs.put("track_id", param[0]);
            return ConnexionUtils.postServerData(Constants.API_LIST_POINTS, pairs);
        }
    }


}