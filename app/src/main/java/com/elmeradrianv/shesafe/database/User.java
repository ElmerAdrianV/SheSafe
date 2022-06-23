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

}
