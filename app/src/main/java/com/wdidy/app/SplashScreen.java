package com.wdidy.app;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;

import com.wdidy.app.R;

/**
 * Created by Rascafr on 23/10/2015.
 */
public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ProgressBar progressLoading = (ProgressBar) findViewById(R.id.progressSplash);
        progressLoading.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                // This method will be executed once the timer is over
                Intent i = new Intent(SplashScreen.this, LoginActivity.class);
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