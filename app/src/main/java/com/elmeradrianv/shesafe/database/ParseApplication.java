package com.elmeradrianv.shesafe.database;

import android.app.Application;

import com.elmeradrianv.shesafe.BuildConfig;
import com.parse.Parse;
import com.parse.ParseObject;


public class ParseApplication extends Application {

    private static final String APPLICATION_ID = BuildConfig.PARSE_APP_ID;
    private static final String CLIENT_KEY = BuildConfig.PARSE_CLIENT_KEY;
    private static final String SERVER = "https://parseapi.back4app.com/";

    @Override
    public void onCreate() {
        super.onCreate();
        ParseObject.registerSubclass(Report.class);
        ParseObject.registerSubclass(EmergencyContacts.class);
        ParseObject.registerSubclass(TypeOfCrime.class);
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(APPLICATION_ID)
                .clientKey(CLIENT_KEY)
                .server(SERVER).build());

    }
}
