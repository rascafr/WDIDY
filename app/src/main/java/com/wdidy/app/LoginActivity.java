package com.wdidy.app;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.wdidy.app.account.UserAccount;
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
                AsyncLogin asyncLogin = new AsyncLogin();
                asyncLogin.execute(Constants.URL_SERVER_CONNECT_CLIENT);

            }
        });
    }

    /**
     * AsyncTask to send / fetch data with server
     */
    private class AsyncLogin extends AsyncTask<String, String, String> {

        private String email, passwordSha256;
        private MaterialDialog dialog;

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
            dialog.hide();

            if (Utilities.isNetworkDataValid(data)) {

                try {
                    JSONObject dataJSON = new JSONObject(data);
                    int result = dataJSON.getInt("result");
                    if (result == 1) {
                        UserAccount userAccount = new UserAccount(dataJSON.getJSONObject("data"));
                        userAccount.registerAccountInPrefs(LoginActivity.this);
                        Intent i = new Intent(LoginActivity.this, TracksListActivity.class);
                        LoginActivity.this.startActivity(i);
                        LoginActivity.this.finish();
                    } else {
                        new MaterialDialog.Builder(LoginActivity.this)
                                .title("Oups !")
                                .content("Adresse email / mot de passe incorrect. Veuillez vérifier vos informations puis réessayer.")
                                .negativeText("Fermer")
                                .cancelable(false)
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(LoginActivity.this, "Erreur serveur", Toast.LENGTH_SHORT).show();
                }

            } else {
                new MaterialDialog.Builder(LoginActivity.this)
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
                pairs.put("email", URLEncoder.encode(email, "UTF-8"));
                pairs.put("password", passwordSha256);
                pairs.put("hash", EncryptUtils.sha256(URLEncoder.encode(email, "UTF-8") + passwordSha256 + getResources().getString(R.string.salt_login)));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return null;
            }

            if (Utilities.isOnline(LoginActivity.this)) {
                return ConnexionUtils.postServerData(urls[0], pairs);
            } else{
                return null;
            }
        }
    }
}
