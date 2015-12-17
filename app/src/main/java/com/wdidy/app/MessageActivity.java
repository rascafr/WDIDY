package com.wdidy.app;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.wdidy.app.account.UserAccount;
import com.wdidy.app.messaging.MessageItem;
import com.wdidy.app.utils.ConnexionUtils;
import com.wdidy.app.utils.EncryptUtils;
import com.wdidy.app.utils.Utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Rascafr on 16/12/2015.
 * Permet de voir les messages dans une conversation, et d'en envoyer
 */
public class MessageActivity extends AppCompatActivity {

    // Adapter
    private MessagesAdapter mAdapter;
    private RecyclerView recyList;

    // Model
    private ArrayList<MessageItem> messageItems;
    private int lastID = -1;

    // User profile
    private UserAccount userAccount;

    // Friend ID (for conversation)
    private String friendID, senderID, friendName;

    // Auto-update objects
    private static Handler mHandler;
    private static final int RUN_UPDATE = 2000;
    private static final int RUN_START = 500;
    private static boolean run;
    private static boolean firstDisplay = true;
    private ProgressBar progressMessage;

    // Send message button and text input
    private EditText etMessage;
    private RelativeLayout rlSendButton;

    // Others
    private Vibrator v;
    private long[] pattern = {0, 200, 300, 200, 300, 200};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        // Get layout elements
        etMessage = (EditText) findViewById(R.id.etMessage);
        rlSendButton = (RelativeLayout) findViewById(R.id.rlSendButton);
        progressMessage = (ProgressBar) findViewById(R.id.progressMessage);

        // Create vibrator object
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Init model
        messageItems = new ArrayList<>();

        // Init profile
        userAccount = new UserAccount();
        userAccount.readAccountPromPrefs(this);
        senderID = userAccount.getUserID();

        // Get friend identifier
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                Toast.makeText(MessageActivity.this, "Erreur de l'application (c'est pas normal)", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                friendID = extras.getString(Constants.INTENT_CONV_FRIEND_ID);
                friendName = extras.getString(Constants.INTENT_CONV_FRIEND_NAME);
            }
        } else {
            friendID = (String) savedInstanceState.getSerializable(Constants.INTENT_CONV_FRIEND_ID);
            friendName = (String) savedInstanceState.getSerializable(Constants.INTENT_CONV_FRIEND_NAME);
        }

        // Set title (friend's name)
        getSupportActionBar().setTitle(friendName);

        // Init RecyclerView
        recyList = (RecyclerView) findViewById(R.id.recyList);
        recyList.setHasFixedSize(true);

        // Create adapter and assign it to RecyclerView
        mAdapter = new MessagesAdapter();
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        llm.setReverseLayout(true);
        llm.setStackFromEnd(true);
        recyList.setLayoutManager(llm);
        recyList.setAdapter(mAdapter);

        // Fetch data from server
        /*AsyncListMessages asyncListMessages = new AsyncListMessages();
        asyncListMessages.execute();*/

        // Send button listener
        rlSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // Encode text
                    //String message_encoded = URLEncoder.encode(etMessage.getText().toString(), "UTF-8");
                    String message_encoded = Base64.encodeToString(etMessage.getText().toString().getBytes("UTF-8"), Base64.NO_WRAP);

                    // Clear EditText field
                    etMessage.setText("");

                    // Prevents the user the message is gone
                    Toast.makeText(MessageActivity.this, "Envoi du message ...", Toast.LENGTH_SHORT).show();

                    // Post data to server
                    AsyncListPostMessage asyncListPostMessage = new AsyncListPostMessage();
                    asyncListPostMessage.execute(message_encoded);

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Custom adapter for track item
     */
    private class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final static int TYPE_USER = 0;
        private final static int TYPE_FRIEND = 1;

        @Override
        public int getItemCount() {
            return messageItems == null ? 0 : messageItems.size();
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            MessageItem mi = messageItems.get(position);

            MessagesViewHolder mvh = (MessagesViewHolder) viewHolder;
            try {
                mvh.vText.setText(Html.fromHtml(new String(Base64.decode(mi.getText().getBytes("UTF-8"), Base64.DEFAULT))));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemViewType(int position) {
            return messageItems.get(position).isUser(senderID) ? TYPE_USER : TYPE_FRIEND;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

            if (viewType == TYPE_USER)
                return new MessagesViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_conv_sender, viewGroup, false));
            else
                return new MessagesViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_conv_friend, viewGroup, false));
        }

        // Classic View Holder for message item
        public class MessagesViewHolder extends RecyclerView.ViewHolder {

            protected TextView vText;

            public MessagesViewHolder(View v) {
                super(v);
                vText = (TextView) v.findViewById(R.id.messageText);
            }
        }
    }

    /**
     * Async task to get user's message for a single conversation
     */
    private class AsyncListMessages extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            run = false;
            if (firstDisplay) {
                progressMessage.setVisibility(View.VISIBLE);
                firstDisplay = false;
            }
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);

            progressMessage.setVisibility(View.GONE);

            if (Utilities.isNetworkDataValid(data)) {

                try {
                    JSONObject jsonObject = new JSONObject(data);
                    if (jsonObject.getInt("error") == 0) {
                        JSONArray array = jsonObject.getJSONArray("data");
                        messageItems.clear();
                        for (int i=0;i<array.length();i++) {
                            messageItems.add(new MessageItem(array.getJSONObject(i)));
                        }
                        mAdapter.notifyDataSetChanged();

                        if (messageItems.size() > 0) {
                            recyList.getLayoutManager().scrollToPosition(0);

                            // Notification : if last message is different from last saved and from friend
                            int lpos = 0;
                            MessageItem msg = messageItems.get(lpos);
                            int msgID = msg.getIDmessage();

                            if (!msg.isUser(senderID) && msgID != lastID) {

                                if (lastID != -1) {
                                    // Notify the user about the incoming message
                                    v.vibrate(pattern, -1);
                                }

                                lastID = msgID;
                            }
                        }

                    } else {
                        new MaterialDialog.Builder(MessageActivity.this)
                                .title("Erreur")
                                .content("Cause : " + jsonObject.getString("cause"))
                                .negativeText("Fermer")
                                .show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(MessageActivity.this, "Erreur serveur", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MessageActivity.this, "Erreur réseau", Toast.LENGTH_SHORT).show();
            }

            mHandler.postDelayed(updateTimerThread, RUN_UPDATE);
            run = true;
        }

        @Override
        protected String doInBackground(String... param) {
            HashMap<String, String> pairs = new HashMap<>();
            pairs.put("api_id", Constants.API_KEY);
            pairs.put("user_id", senderID);
            pairs.put("friend_id", friendID);
            return ConnexionUtils.postServerData(Constants.API_SINGLE_CONVERSATION, pairs);
        }
    }

    /**
     * Async task to post a message in a single conversation
     */
    private class AsyncListPostMessage extends AsyncTask<String, String, String> {

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
                        messageItems.clear();
                        for (int i=0;i<array.length();i++) {
                            messageItems.add(new MessageItem(array.getJSONObject(i)));
                        }
                        mAdapter.notifyDataSetChanged();
                        recyList.getLayoutManager().scrollToPosition(0);
                    } else {
                        new MaterialDialog.Builder(MessageActivity.this)
                                .title("Erreur")
                                .content("Cause : " + jsonObject.getString("cause"))
                                .negativeText("Fermer")
                                .show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(MessageActivity.this, "Erreur serveur", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MessageActivity.this, "Erreur réseau", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected String doInBackground(String... param) {
            HashMap<String, String> pairs = new HashMap<>();
            pairs.put("api_id", Constants.API_KEY);
            pairs.put("user_id", senderID);
            pairs.put("friend_id", friendID);
            pairs.put("text", param[0]);
            return ConnexionUtils.postServerData(Constants.API_POST_MESSAGE, pairs);
        }
    }

    /**
     * Android App Lifecycle
     */
    @Override
    public void onResume() {
        super.onResume();
        firstDisplay = true;
        // Delay to update data
        run = true;

        if (progressMessage != null) progressMessage.setVisibility(View.INVISIBLE);

        if (mHandler == null) {
            mHandler = new android.os.Handler();
            mHandler.postDelayed(updateTimerThread, RUN_START);
        } else {
            mHandler.removeCallbacks(updateTimerThread);
            mHandler.postDelayed(updateTimerThread, RUN_START);
        }
    }

    @Override
    public void onPause() {
        if( mHandler != null) {
            mHandler.removeCallbacks(updateTimerThread);
        }
        run = false;
        super.onPause();
    }

    /**
     * Background task to fetch data periodically from server
     */
    private Runnable updateTimerThread = new Runnable() {
        public void run() {

            try {
                if (run) {
                    run = false;
                    AsyncListMessages asyncListMessages = new AsyncListMessages();
                    asyncListMessages.execute();
                }
            } catch (NullPointerException e) { // Stop handler if fragment disappears
                mHandler.removeCallbacks(updateTimerThread);
                run = false;
            }
        }
    };
}
