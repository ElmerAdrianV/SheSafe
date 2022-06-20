package com.elmeradrianv.shesafe.database;

import android.app.Application;

import com.elmeradrianv.shesafe.BuildConfig;
import com.parse.Parse;
import com.parse.ParseObject;

public class ParseApplication extends Application {

    private static final String applicationId= BuildConfig.PARSE_APP_ID;
    private static final String clientKey = BuildConfig.PARSE_CLIENT_KEY;
    private static final String server = "https://parseapi.back4app.com/";
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(applicationId)
                .clientKey(clientKey)
                .server(server).build());

    }
}
