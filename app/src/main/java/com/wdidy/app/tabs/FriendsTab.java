package com.wdidy.app.tabs;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.wdidy.app.Constants;
import com.wdidy.app.MessageActivity;
import com.wdidy.app.R;
import com.wdidy.app.account.UserAccount;
import com.wdidy.app.friend.FriendItem;
import com.wdidy.app.listeners.RecyclerItemClickListener;
import com.wdidy.app.utils.ConnexionUtils;
import com.wdidy.app.utils.Utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Rascafr on 13/02/2016.
 */
public class FriendsTab extends Fragment {

    // Adapter
    private FriendAdapter mAdapter;

    // UI Layout
    private ProgressBar pgLoading;
    private RecyclerView recyList;

    // Model
    private ArrayList<FriendItem> friendItems;

    // User profile
    UserAccount userAccount;

    // Android
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_friends, container, false);
        context = getActivity();

        // Init model
        friendItems = new ArrayList<>();

        // Init profile
        userAccount = new UserAccount();
        userAccount.readAccountPromPrefs(context);

        // Init RecyclerView
        recyList = (RecyclerView) rootView.findViewById(R.id.recyList);
        recyList.setHasFixedSize(true);

        // Init layout
        pgLoading = (ProgressBar) rootView.findViewById(R.id.progressLoading);
        pgLoading.setVisibility(View.INVISIBLE);

        // Create adapter and assign it to RecyclerView
        mAdapter = new FriendAdapter();
        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyList.setLayoutManager(llm);
        recyList.setAdapter(mAdapter);

        // Fetch data from server
        AsyncListFriends asyncListFriends = new AsyncListFriends();
        asyncListFriends.execute();

        // On click listener → messages
        recyList.addOnItemTouchListener(new RecyclerItemClickListener(context, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent i = new Intent(context, MessageActivity.class);
                i.putExtra(Constants.INTENT_CONV_FRIEND_ID, friendItems.get(position).getFriendID());
                i.putExtra(Constants.INTENT_CONV_FRIEND_NAME, friendItems.get(position).getName());
                startActivity(i);
            }
        }));

        return rootView;
    }

    /**
     * Custom adapter for friend item
     */
    private class FriendAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        public int getItemCount() {
            return friendItems == null ? 0 : friendItems.size();
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            FriendItem fi = friendItems.get(position);

            FriendViewHolder fvh = (FriendViewHolder) viewHolder;
            fvh.vName.setText(fi.getName());
            fvh.vCity.setText(fi.getCity());
            ImageLoader.getInstance().displayImage(fi.getImageLink(), fvh.imgFriend);
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

            return new FriendViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_friend, viewGroup, false));
        }

        // Classic View Holder for history item
        public class FriendViewHolder extends RecyclerView.ViewHolder {

            protected TextView vName, vCity;
            protected CircleImageView imgFriend;

            public FriendViewHolder(View v) {
                super(v);
                vName = (TextView) v.findViewById(R.id.tvFriendName);
                vCity = (TextView) v.findViewById(R.id.tvFriendCity);
                imgFriend = (CircleImageView) v.findViewById(R.id.friendCircleView);
            }
        }
    }

    /**
     * Async task to get user's friends list
     */
    private class AsyncListFriends extends AsyncTask<String, String, String> {

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
                            friendItems.add(new FriendItem(array.getJSONObject(i)));
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
            pairs.put("user_id", userAccount.getUserID());
            return ConnexionUtils.postServerData(Constants.API_LIST_FRIENDS, pairs);
        }
    }
}