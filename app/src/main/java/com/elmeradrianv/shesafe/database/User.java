package com.elmeradrianv.shesafe.database;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseUser;

@ParseClassName("User")
public class User extends ParseUser {
    public static final String PERSONAL_DESCRIPTION_KEY = "personalDescription";
    public static final String PROFILE_PHOTO_KEY = "profilePhoto";
    public static final String FIRST_NAME_KEY = "firstName";
    public static final String LAST_NAME_KEY="lastName";

    public String getPersonalDescription() {
        return getString(PERSONAL_DESCRIPTION_KEY);
    }

    void setPersonalDescription(String personalDescription) {
        put(PERSONAL_DESCRIPTION_KEY, personalDescription);
    }
    public ParseFile getProfilePhoto() {
        return getParseFile(PROFILE_PHOTO_KEY);
    }

    void setPersonalDescriptionKey(ParseFile photo) {
        put(PROFILE_PHOTO_KEY, photo);
    }
    public String getFirstName(){return getString(FIRST_NAME_KEY);}
    public void setFirstName(String firstName){put(FIRST_NAME_KEY,firstName);}

    public String getLastName(){return getString(LAST_NAME_KEY);}
    public void setLastName(String lastName){put(LAST_NAME_KEY,lastName);}
}
