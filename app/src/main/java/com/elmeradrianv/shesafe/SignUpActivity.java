package com.elmeradrianv.shesafe;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;


public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = SignUpActivity.class.getSimpleName();
    // PICK_PHOTO_CODE is a constant integer
    public final static int PICK_PHOTO_CODE = 1046;
    private static boolean PERSONALIZED_PHOTO_PICKED = false;
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
        Button btnAddProfilePhoto = findViewById(R.id.btnAddProfilePhoto);


        setImage("", ivProfilePhoto);
        setupBtnSignUp(btnSignUp, etUsername, etFirstName, etLastName, etEmail, etPersonalDescription, etPassword, etPasswordConfirm);
        setBtnAddProfilePhoto(btnAddProfilePhoto);



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
    // Returns the File for a photo stored on disk given the fileName
    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        return new File(mediaStorageDir.getPath() + File.separator + fileName);


    }

    private void signupNewUser(String username, String firstName, String lastName, String email, String personalDescription, String password) {
        ParseUser user = new ParseUser();

        if(PERSONALIZED_PHOTO_PICKED){
            ImageView ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
            File profilePhoto = getPhotoFileUri(ivProfilePhoto.getTag().toString());
            ParseFile file = new ParseFile(profilePhoto);

            file.saveInBackground((SaveCallback) e -> {
                // If successful add file to user and signUpInBackground
                if(e != null)
                    Log.e(TAG, "done save profile image: ",e);
                else
                    user.put(User.PROFILE_PHOTO_KEY,file);
            });

        }
        user.setUsername(username);
        user.put(User.FIRST_NAME_KEY, firstName);
        user.put(User.LAST_NAME_KEY, lastName);
        user.setEmail(email);
        user.put(User.PERSONAL_DESCRIPTION_KEY, personalDescription);
        user.setPassword(password);
        user.signUpInBackground(e -> {
            if (e != null) {
                Toast.makeText(this, "Couldn't sign up", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "signupNewUser: Signup error", e);
                return;
            }
            Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show();
            goMainActivity();
        });
    }

    private void setBtnAddProfilePhoto(Button btnAddProfilePhoto) {
        btnAddProfilePhoto.setOnClickListener(v -> {
            onPickPhoto();
        });
    }


    // Trigger gallery selection for a photo
    public void onPickPhoto() {
        // Create intent for picking a photo from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getPackageManager()) != null) {
            // Bring up gallery to select a photo
            startActivityForResult(intent, PICK_PHOTO_CODE);
            PERSONALIZED_PHOTO_PICKED=true;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((data != null) && requestCode == PICK_PHOTO_CODE) {
            Uri photoUri = data.getData();
            // Load the selected image into a preview
            ImageView ivProfilePhoto =  findViewById(R.id.ivProfilePhoto);
            ivProfilePhoto.setTag(photoUri.toString());
            setImage(photoUri.toString(),ivProfilePhoto);
        }
    }

    private void setImage(String uri, ImageView imageView) {
        int radius = 100000;
        Glide.with(this).load(uri)
                .apply(new RequestOptions()
                        .centerCrop()
                        .placeholder(R.drawable.ssplaceholder)
                        .transform(new RoundedCorners(radius))
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