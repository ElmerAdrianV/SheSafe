package com.elmeradrianv.shesafe.database;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import org.json.JSONArray;
@ParseClassName("EmergencyMessage")
public class EmergencyMessage extends ParseObject {
    public static final String MESSAGE_KEY = "message";
    public static final String USER_KEY = "user";
    public static final String EMERGENCY_CONTACTS_KEY = "emergencyContacts";

    public String getMessage() {
        return getString(MESSAGE_KEY);
    }

    public void setMessage(String message) {
        put(MESSAGE_KEY, message);
    }

    public User getUser() {
        return (User) get(USER_KEY);
    }

    public JSONArray getEmergencyContacts() {
        return getJSONArray(EMERGENCY_CONTACTS_KEY);
    }

}
