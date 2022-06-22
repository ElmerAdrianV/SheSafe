package com.elmeradrianv.shesafe;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.elmeradrianv.shesafe.database.User;
import com.parse.ParseUser;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = SignUpActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        //To hide the action bar in appcompact activity
        getSupportActionBar().hide();

        ImageView ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        EditText etUsername = findViewById(R.id.etUsername);
        EditText etFirstName = findViewById(R.id.etFirstName);
        EditText etLastName = findViewById(R.id.etLastName);
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPersonalDescription = findViewById(R.id.etPersonalDescription);
        EditText etPassword = findViewById(R.id.etPassword);
        EditText etPasswordConfirm = findViewById(R.id.etPasswordConfirm);
        Button btnSignUp = findViewById(R.id.btnSignUp);
        setImage("", ivProfilePhoto);
        setupBtnSignUp(btnSignUp, etUsername, etFirstName, etLastName, etEmail, etPersonalDescription, etPassword, etPasswordConfirm);


    }

    private void setupBtnSignUp(Button btnSignUp, EditText etUsername, EditText etFirstName, EditText etLastName, EditText etEmail, EditText etPersonalDescription, EditText etPassword, EditText etPasswordConfirm) {
        btnSignUp.setOnClickListener(v -> {
            String username = etUsername.getText().toString();
            String firstName = etFirstName.getText().toString();
            String lastName = etLastName.getText().toString();
            String email = etEmail.getText().toString();
            String personalDescription = etPersonalDescription.getText().toString();
            String password = etPassword.getText().toString();
            String passwordConfirm = etPasswordConfirm.getText().toString();
            if (username.isEmpty() && firstName.isEmpty() && lastName.isEmpty() && email.isEmpty() && personalDescription.isEmpty() && password.isEmpty() && passwordConfirm.isEmpty()) {
                Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
            } else {
                if (password.equals(passwordConfirm)) {
                    signupNewUser(username, firstName, lastName, email, personalDescription, password);
                } else {
                    Toast.makeText(this, "Password doesn't matched", Toast.LENGTH_SHORT).show();
                }
            }

        });
    }

    private void signupNewUser(String username, String firstName, String lastName, String email, String personalDescription, String password) {
        User user = new User();
        user.setUsername(username);

        user.setPassword(password);

        user.signUpInBackground(e -> {
            if (e != null) {
                Toast.makeText(this, "Couldn't sign up", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show();
            goMainActivity();
        });
    }

    private void setImage(String url, ImageView imageView) {
        int radiusIP = 100;
        Glide.with(this).load(url)
                .apply(new RequestOptions()
                        .centerCrop()
                        .placeholder(R.drawable.ssplaceholder)
                        .transform(new RoundedCorners(radiusIP))
                )
                .into(imageView);
    }
    private void goMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        Log.i(TAG, "User signup successfully");
        finish();
    }
}