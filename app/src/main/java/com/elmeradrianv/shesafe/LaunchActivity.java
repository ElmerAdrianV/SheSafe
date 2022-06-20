package com.elmeradrianv.shesafe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class LaunchActivity extends AppCompatActivity {
    private static final int SPLASH_SCREEN_DURATION_MS = 2000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        getSupportActionBar().hide();

        //Launch Activity to show the welcome logo of the app
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(LaunchActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        },SPLASH_SCREEN_DURATION_MS);
    }
}
