package com.elmeradrianv.shesafe.database;

import android.app.Application;

import com.elmeradrianv.shesafe.BuildConfig;
import com.parse.Parse;


public class ParseApplication extends Application {

    private static final String APPLICATION_ID= BuildConfig.PARSE_APP_ID;
    private static final String CLIENTE_KEY = BuildConfig.PARSE_CLIENT_KEY;
    private static final String SERVER = "https://parseapi.back4app.com/";
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(APPLICATION_ID)
                .clientKey(CLIENTE_KEY)
                .server(SERVER).build());

    }
}
