package com.elmeradrianv.shesafe.database;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("EmergencyContacts")
public class EmergencyContacts extends ParseObject {
    public static final String USER_KEY = "user";
    public static final String NUMBER_KEY = "number";
    public static final String NICKNAME_KEY = "nickname";

    public EmergencyContacts() {
        super();
    }

    public ParseUser getUser() {
        return getParseUser(USER_KEY);
    }

    public Long getNumber() {
        return getLong(NUMBER_KEY);
    }

    public String getNickname() {
        return getString(NICKNAME_KEY);
    }

}
