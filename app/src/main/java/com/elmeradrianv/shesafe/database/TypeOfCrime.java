package com.elmeradrianv.shesafe.database;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("TypeOfCrime")
public class TypeOfCrime extends ParseObject {
    public static final String TAG_KEY = "tag";
    public static final String LEVEL_OF_RISK_KEY = "levelOfRisk";

    public String getTag() {
        return getString(TAG_KEY);
    }

    public int getLevelOfRisk() {
        return getInt(LEVEL_OF_RISK_KEY);
    }

}
