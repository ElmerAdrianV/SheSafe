package com.elmeradrianv.shesafe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class LaunchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        getSupportActionBar().hide();

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(LaunchActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        },2000);
    }
}