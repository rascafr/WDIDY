package com.wdidy.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.wdidy.app.account.UserAccount;
import com.wdidy.app.gcmpush.QuickstartPreferences;
import com.wdidy.app.gcmpush.RegistrationIntentService;
import com.wdidy.app.utils.ConnexionUtils;
import com.wdidy.app.utils.EncryptUtils;
import com.wdidy.app.utils.Utilities;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

/**
 * Created by Rascafr on 01/11/2015.
 */
public class LoginActivity extends AppCompatActivity {

    // UI Layout
    private EditText etLogin, etPassword;
    private Button bpConnect;

    // GCM
    private BroadcastReceiver mRegistrationBroadcastReceiver;

    // Connect
    private AsyncLogin asyncLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Get UI Objects
        etLogin = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
        bpConnect = (Button) findViewById(R.id.bpConnect);

        // Listener on button press
        bpConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Connect to server
                asyncLogin = new AsyncLogin();
                asyncLogin.execute();

            }
        });

        // GCM Receiver
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Log.d("NOTIF", "onReceive !");
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences.getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);

                asyncLogin.getDialog().hide();
                if (sentToken) {
                    Intent i = new Intent(LoginActivity.this, MainActivityTab.class);
                    LoginActivity.this.startActivity(i);
                    LoginActivity.this.finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Erreur d'enregistrement", Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    /**
     * AsyncTask to send / fetch data with server
     */
    private class AsyncLogin extends AsyncTask<String, String, String> {

        private String email, passwordSha256;
        private MaterialDialog dialog;

        public MaterialDialog getDialog() {
            return dialog;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Trim spaces / prepare strings
            email = etLogin.getText().toString().trim();
            passwordSha256 = EncryptUtils.sha256(etPassword.getText().toString().trim() + getResources().getString(R.string.salt_pass));
            etLogin.setText(email);

            // Create dialog
            dialog = new MaterialDialog.Builder(LoginActivity.this)
                    .title("Connexion")
                    .content("Veuillez patienter")
                    .cancelable(false)
                    .progressIndeterminateStyle(false)
                    .progress(true, 0)
                    .show();

        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);

            if (Utilities.isNetworkDataValid(data)) {

                try {
                    JSONObject jsonObject = new JSONObject(data);

                    // User exists ?
                    if (jsonObject.getInt("error") == 0) {

                        UserAccount userAccount = new UserAccount(jsonObject.getJSONObject("data"));
                        userAccount.registerAccountInPrefs(LoginActivity.this);

                        // Check Services, then start Registration class
                        if (Utilities.checkPlayServices(LoginActivity.this)) {
                            dialog.setContent("Enregistrement de l'appareil");

                            // Start IntentService to register this application with GCM.
                            Intent intent = new Intent(LoginActivity.this, RegistrationIntentService.class);
                            LoginActivity.this.startService(intent);
                        }

                    } else {
                        dialog.hide();
                        dialog = new MaterialDialog.Builder(LoginActivity.this)
                                .title("Erreur")
                                .content("Cause : " + jsonObject.getString("cause"))
                                .negativeText("Fermer")
                                .show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(LoginActivity.this, "Erreur serveur", Toast.LENGTH_SHORT).show();
                }
            } else {
                dialog.hide();
                dialog = new MaterialDialog.Builder(LoginActivity.this)
                        .title("Oups !")
                        .content("Impossible d'accéder au réseau. Veuillez vérifier votre connexion internet puis réessayer.")
                        .negativeText("Fermer")
                        .cancelable(false)
                        .show();
            }
        }

        @Override
        protected String doInBackground(String... urls) {

            HashMap<String, String> pairs = new HashMap<>();
            try {
                pairs.put("api_id", Constants.API_KEY);
                pairs.put("email", URLEncoder.encode(email, "UTF-8"));
                pairs.put("password", passwordSha256);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return null;
            }

            if (Utilities.isOnline(LoginActivity.this)) {
                return ConnexionUtils.postServerData(Constants.API_LOGIN_USER, pairs);
            } else{
                return null;
            }
        }
    }
}
