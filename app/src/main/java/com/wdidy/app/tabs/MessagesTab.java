package com.wdidy.app.tabs;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Base64;
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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Rascafr on 13/02/2016.
 */
public class MessagesTab extends Fragment {

    // Adapter
    private ConversationAdapter mAdapter;

    // UI Layout
    private ProgressBar pgLoading;
    private RecyclerView recyList;

    // Model
    private ArrayList<ConversationItem> conversationItems;

    // User profile
    UserAccount userAccount;

    // Android
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_empty, container, false);

        context = getActivity();

        // Init model
        conversationItems = new ArrayList<>();

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
        mAdapter = new ConversationAdapter();
        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyList.setLayoutManager(llm);
        recyList.setAdapter(mAdapter);

        // Fetch data from server
        AsyncConversations asyncConversations = new AsyncConversations();
        asyncConversations.execute();

        // On click listener → messages
        recyList.addOnItemTouchListener(new RecyclerItemClickListener(context, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent i = new Intent(context, MessageActivity.class);
                i.putExtra(Constants.INTENT_CONV_FRIEND_ID, conversationItems.get(position).getFriendID());
                i.putExtra(Constants.INTENT_CONV_FRIEND_NAME, conversationItems.get(position).getFriendName());
                startActivity(i);
            }
        }));

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        AsyncConversations asyncConversations = new AsyncConversations();
        asyncConversations.execute();
    }

    /**
     * Custom adapter for conversation item
     */
    private class ConversationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        public int getItemCount() {
            return conversationItems == null ? 0 : conversationItems.size();
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            ConversationItem ci = conversationItems.get(position);

            ConversationViewHolder fvh = (ConversationViewHolder) viewHolder;
            fvh.vName.setText(ci.getFriendName());
            fvh.vDate.setText(ci.getSendDate());
            fvh.vMessage.setText(Html.fromHtml(ci.getResume()));
            ImageLoader.getInstance().displayImage(ci.getFriendPict(), fvh.imgConv);
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

            return new ConversationViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_conversation, viewGroup, false));
        }

        // Classic View Holder for conversation item
        public class ConversationViewHolder extends RecyclerView.ViewHolder {

            protected TextView vName, vMessage, vDate;
            protected CircleImageView imgConv;

            public ConversationViewHolder(View v) {
                super(v);
                vName = (TextView) v.findViewById(R.id.tvConvName);
                vMessage = (TextView) v.findViewById(R.id.tvLastMessage);
                vDate = (TextView) v.findViewById(R.id.tvLastDate);
                imgConv = (CircleImageView) v.findViewById(R.id.convCircleView);
            }
        }
    }

    /**
     * Custom task to fetch conversation data
     */
    private class AsyncConversations extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            conversationItems.clear();
            mAdapter.notifyDataSetChanged();
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
                        for (int i = 0; i < array.length(); i++) {
                            conversationItems.add(new ConversationItem(array.getJSONObject(i)));
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
        protected String doInBackground(String... params) {
            HashMap<String, String> pairs = new HashMap<>();
            pairs.put("api_id", Constants.API_KEY);
            pairs.put("user_id", userAccount.getUserID());
            return ConnexionUtils.postServerData(Constants.API_LIST_CONVERSATIONS, pairs);
        }
    }

    /**
     * Custom definition for conversation item
     */
    private class ConversationItem {
        String senderName, friendName, sendDate, friendID, friendPict, text, resume;

        public ConversationItem(JSONObject obj) throws JSONException {
            this.senderName = obj.getString("last_name");
            this.friendName = obj.getString("firstname") + " " + obj.getString("lastname");
            this.sendDate = obj.getString("date");
            this.friendID = obj.getString("IDfriend");
            this.friendPict = Constants.URL_PROFILE_PICTS + obj.getString("imgLink");
            this.text = obj.getString("message");
            try {
                this.resume = senderName + " : " + new String(Base64.decode(this.text.getBytes("UTF-8"), Base64.DEFAULT));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                this.resume = senderName + " : " + "<i>Message illisible</i>";
            }
        }

        public String getSenderName() {
            return senderName;
        }

        public String getFriendName() {
            return friendName;
        }

        public String getSendDate() {
            return sendDate;
        }

        public String getFriendID() {
            return friendID;
        }

        public String getFriendPict() {
            return friendPict;
        }

        public String getText() {
            return text;
        }

        public String getResume() {
            return resume;
        }
    }
}