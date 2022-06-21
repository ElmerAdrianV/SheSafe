package com.elmeradrianv.shesafe.database;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseUser;

@ParseClassName("User")
public class User extends ParseUser {
    public static final String PERSONAL_DESCRIPTION_KEY="personalDescription";
    public static final String PROFILE_PHOTO_KEY="profilePhoto";

    public String getPersonalDescription(){
        return getString(PERSONAL_DESCRIPTION_KEY);
    }
    void setPersonalDescription(String personalDescription){
        put(PERSONAL_DESCRIPTION_KEY,personalDescription);
    }
    public ParseFile getProfilePhoto(){
        return getParseFile(PROFILE_PHOTO_KEY);
    }
    void setPersonalDescriptionKey(ParseFile photo){
        put(PROFILE_PHOTO_KEY,photo);
    }

}
