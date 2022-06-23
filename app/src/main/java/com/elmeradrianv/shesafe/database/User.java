package com.elmeradrianv.shesafe.database;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;

@ParseClassName("User")
public class User extends ParseUser {
    public static final String PERSONAL_DESCRIPTION_KEY = "personalDescription";
    public static final String PROFILE_PHOTO_KEY = "profilePhoto";
    public static final String FIRST_NAME_KEY = "firstName";
    public static final String LAST_NAME_KEY = "lastName";
    public static final String TAG = User.class.getSimpleName();

    public static void saveWithoutImage(Context context, String username, String firstName, String lastName, String email, String personalDescription, String password) {
        ParseUser user = new ParseUser();
        user.setUsername(username);
        user.put(User.FIRST_NAME_KEY, firstName);
        user.put(User.LAST_NAME_KEY, lastName);
        user.setEmail(email);
        user.put(User.PERSONAL_DESCRIPTION_KEY, personalDescription);
        user.setPassword(password);
        user.signUpInBackground(e -> {
            if (e != null) {
                Toast.makeText(context, "Couldn't sign up", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "signupNewUser: Signup error", e);
                return;
            }
            Toast.makeText(context, "Success!", Toast.LENGTH_SHORT).show();
        });
    }

    public static void saveWithImage(Context context, File profilePhoto, String username, String firstName, String lastName, String email, String personalDescription, String password) {
        ParseUser user = new ParseUser();
        ParseFile file = new ParseFile(profilePhoto);
        file.saveInBackground((SaveCallback) e -> {
            // If successful add file to user and signUpInBackground
            if (e != null)
                Log.e(TAG, "done save profile image: ", e);
            else {
                user.put(User.PROFILE_PHOTO_KEY, file);
                user.setUsername(username);
                user.put(User.FIRST_NAME_KEY, firstName);
                user.put(User.LAST_NAME_KEY, lastName);
                user.setEmail(email);
                user.put(User.PERSONAL_DESCRIPTION_KEY, personalDescription);
                user.setPassword(password);
                user.signUpInBackground(e2 -> {
                    if (e != null) {
                        Toast.makeText(context, "Couldn't sign up", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "signupNewUser: Signup error", e2);
                        return;
                    }
                    Toast.makeText(context, "Success!", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

}
