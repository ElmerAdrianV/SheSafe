package com.elmeradrianv.shesafe;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.parse.ParseUser;

public class LoginActivity extends AppCompatActivity {
    public static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestPermissions(
                new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS,
                        Manifest.permission.READ_SMS, Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CALL_PHONE
                },
                PackageManager.PERMISSION_GRANTED);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        EditText etUsername = findViewById(R.id.etUsername);
        EditText etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnAddProfilePhoto);
        Button btnSignUp = findViewById(R.id.btnSignUp);
        //Need implement the logout button
        if (ParseUser.getCurrentUser() != null) {
            goMainActivity();
        }

        btnLogin.setOnClickListener(v -> {
            Log.i(TAG, "onClick: I want login");
            String username = etUsername.getText().toString();
            String password = etPassword.getText().toString();
            loginUser(username, password);
        });

        btnSignUp.setOnClickListener(v -> {
            goSignUpActivity();
        });

    }

    private void loginUser(String username, String password) {
        Log.i(TAG, "loginUser: Attempting to login user: " + username);
        ParseUser.logInInBackground(username, password, (user, e) -> {
            if (e != null) {
                Log.e(TAG, "Issue with login", e);
                return;
            }
            goMainActivity();
        });
    }

    private void goMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        Log.i(TAG, "User login successfully");
        finish();
    }

    private void goSignUpActivity() {
        startActivity(new Intent(this, SignUpActivity.class));
    }
}
