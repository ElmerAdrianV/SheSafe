package com.elmeradrianv.shesafe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;


import com.parse.ParseUser;

public class LoginActivity extends AppCompatActivity {
    public static final String TAG="LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EditText etUsername;
        EditText etPassword;
        Button btnLogin;
        //Button btnSignUp;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        etUsername=findViewById(R.id.etUsername);
        etPassword=findViewById(R.id.etPassword);
        btnLogin=findViewById(R.id.btnLogin);
        //btnSignUp=findViewById(R.id.btnSignUp);

        //To hide the action bar in appcompact activity
        getSupportActionBar().hide();

        if(ParseUser.getCurrentUser()!=null){
            goMainActivity();
        }

        btnLogin.setOnClickListener(v -> {
            Log.i(TAG, "onClick: I want login");
            String username = etUsername.getText().toString();
            String password = etPassword.getText().toString();
            loginUser(username,password);
        });

    }
    private void loginUser(String username, String password){
        Log.i(TAG, "loginUser: Attempting to login user: "+username);
        ParseUser.logInInBackground(username, password, (user, e) -> {
            if(e!=null){
                Log.e(TAG, "Issue with login",e );
                return;
            }
            goMainActivity();
        });
    }

    private void goMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}