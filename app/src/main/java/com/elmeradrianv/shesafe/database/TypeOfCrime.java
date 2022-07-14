package com.elmeradrianv.shesafe.database;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("TypeOfCrime")
public class TypeOfCrime extends ParseObject {
    public static final String TAG_KEY = "tag";
    public static final String LEVEL_OF_RISK_KEY = "levelOfRisk";
    public static final int LOW_RISK = 1;
    public static final int MEDIUM_LOW_RISK = 2;
    public static final int MEDIUM_RISK = 3;
    public static final int MEDIUM_HIGH_RISK = 4;
    public static final int HIGH_RISK = 5;

    public String getTag() {
        return getString(TAG_KEY);
    }

    public int getLevelOfRisk() {
        return getInt(LEVEL_OF_RISK_KEY);
    }
}
