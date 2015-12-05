package com.wdidy.app;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;

import com.wdidy.app.account.UserAccount;

/**
 * Created by Rascafr on 23/10/2015.
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ProgressBar progressLoading = (ProgressBar) findViewById(R.id.progressSplash);
        progressLoading.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                // Si profil non crée -> login
                // Sinon -> activité principale
                UserAccount userAccount = new UserAccount();
                userAccount.readAccountPromPrefs(SplashActivity.this);

                Intent i;
                if (userAccount.isCreated()) {
                    i = new Intent(SplashActivity.this, TracksListActivity.class);
                } else {
                    i = new Intent(SplashActivity.this, LoginActivity.class);
                }
                startActivity(i);
                finish();

            }
        }, 750);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }
}