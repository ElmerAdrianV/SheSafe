package com.elmeradrianv.shesafe.database;

import com.parse.ParseClassName;
import com.parse.ParseUser;

@ParseClassName("User")
public class User extends ParseUser {
    public static final String PERSONAL_DESCRIPTION_KEY = "personalDescription";
    public static final String PROFILE_PHOTO_KEY = "profilePhoto";
    public static final String FIRST_NAME_KEY = "firstName";
    public static final String LAST_NAME_KEY = "lastName";
    public static final String EMAIL_KEY="email";
    public static final String PASSWORD_KEY="password";
    public static final String EMERGENCY_MESSAGE_KEY = "emergencyMessage";
    public static final String TAG = User.class.getSimpleName();

}
