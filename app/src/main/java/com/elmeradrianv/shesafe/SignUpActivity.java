package com.elmeradrianv.shesafe;

import androidx.appcompat.app.AppCompatActivity;

import android.media.Image;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        //To hide the action bar in appcompact activity
        getSupportActionBar().hide();
        EditText etUsername = findViewById(R.id.etUsername);
        EditText etFirstName = findViewById(R.id.etFirstName);
        EditText etLastName = findViewById(R.id.etLastName);
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPersonalDescription = findViewById(R.id.etPersonalDescription);
        EditText etPassword = findViewById(R.id.etPassword);
        EditText etPassConfirm = findViewById(R.id.etPasswordConfirm);
        ImageView ivProfilePhoto = findViewById(R.id.ivProfilePhoto);

        setImage("",ivProfilePhoto);




    }
    private void setImage(String url, ImageView imageView){
        int radiusIP = 100;
        Glide.with(this).load(url)
                .apply(new RequestOptions()
                        .centerCrop()
                        .placeholder(R.drawable.ssplaceholder)
                        .transform(new RoundedCorners(radiusIP))
                )
                .into(imageView);
    }
}