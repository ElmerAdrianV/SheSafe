package com.elmeradrianv.shesafe.database;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

import java.util.Date;

@ParseClassName("Report")
public class Report extends ParseObject {
    public static final String DESCRIPTION_KEY = "description";
    public static final String DATE_KEY = "date";
    public static final String USER_KEY = "user";
    public static final String LOCATION_KEY = "location";
    public static final String TYPE_OF_CRIME_KEY = "typeOfCrime";

    public String getDescription() {
        return getString(DESCRIPTION_KEY);
    }

    public Date getDate() {
        return getDate(DATE_KEY);
    }

    public User getUser() {
        return (User) get(USER_KEY);
    }

    public TypeOfCrime getTypeOfCrime() {
        return (TypeOfCrime) get(TYPE_OF_CRIME_KEY);
    }

    public ParseGeoPoint getLocation() {
        return (ParseGeoPoint) get(LOCATION_KEY);
    }

    public void setDescription() {
        getString(DESCRIPTION_KEY);
    }

    public void setDate() {
        getDate(DATE_KEY);
    }

    public void setUser() {
        get(USER_KEY);
    }

    public void setTypeOfCrime() {
        get(TYPE_OF_CRIME_KEY);
    }

    public void setLocation() {
        get(LOCATION_KEY);
    }
}
