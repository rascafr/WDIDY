package com.wdidy.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.wdidy.app.account.UserAccount;

/**
 * Created by Rascafr on 23/10/2015.
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        final SharedPreferences prefs_Read;
        final SharedPreferences.Editor prefs_Write;
        ((TextView) findViewById(R.id.tvVersion)).setText("Version " + BuildConfig.VERSION_NAME);

        prefs_Read = getSharedPreferences(Constants.PREFS_ACCOUNT_KEY, 0);
        prefs_Write = prefs_Read.edit();

        ProgressBar progressLoading = (ProgressBar) findViewById(R.id.progressSplash);
        progressLoading.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                UserAccount userAccount = new UserAccount();

                // Si la version est différente et nécessite une suppression des paramètres account
                if (!BuildConfig.VERSION_NAME.equals(prefs_Read.getString(Constants.PREFS_APP_VERSION, ""))) {
                    userAccount.removeAccount(SplashActivity.this);
                    prefs_Write.putString(Constants.PREFS_APP_VERSION, BuildConfig.VERSION_NAME);
                    prefs_Write.apply();
                    Toast.makeText(SplashActivity.this, "Mise à jour effectuée. Veuillez vous reconnecter.", Toast.LENGTH_SHORT).show();
                }

                // Si profil non crée -> login
                // Sinon -> activité principale
                userAccount.readAccountPromPrefs(SplashActivity.this);

                Intent i;
                if (userAccount.isCreated()) {
                    i = new Intent(SplashActivity.this, MainActivityTab.class);
                } else {
                    i = new Intent(SplashActivity.this, LoginActivity.class);
                }
                startActivity(i);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();

            }
        }, 1000);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }
}