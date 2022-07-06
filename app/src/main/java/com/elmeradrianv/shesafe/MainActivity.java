package com.elmeradrianv.shesafe;


import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

import android.Manifest;
import android.location.Location;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.elmeradrianv.shesafe.database.EmergencyContacts;
import com.elmeradrianv.shesafe.database.User;
import com.elmeradrianv.shesafe.fragments.MapFragment;
import com.elmeradrianv.shesafe.fragments.ProfileUserFragment;
import com.elmeradrianv.shesafe.fragments.TableViewFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import permissions.dispatcher.NeedsPermission;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupBottomNavigationView();
        ((ImageButton) findViewById(R.id.btnSos)).setOnClickListener(v -> setupSOSButton());
    }

    @SuppressWarnings({"MissingPermission"})
    @NeedsPermission({android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    private void setupSOSButton() {

        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);
        locationClient.getLastLocation().addOnSuccessListener(location -> {
                    if (location != null) {
                        sendEmergencyMessages(location);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d("SOS button", "Error trying to get last GPS location");
                    e.printStackTrace();
                });


    }
    public void sendEmergencyMessages(Location location) {
        ParseQuery<EmergencyContacts> query = ParseQuery.getQuery(EmergencyContacts.class);
        query.include(EmergencyContacts.USER_KEY);
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.addDescendingOrder("createdAt");
        query.findInBackground((contactsList, e) -> {
            if (e != null) {
                Log.e(TAG, "Issue with sending messages", e);
                return;
            }
            SmsManager smsManager = SmsManager.getDefault();
            String message = ParseUser.getCurrentUser().get(User.EMERGENCY_MESSAGE_KEY).toString() +
                    " I'm in https://google.com/maps?q="+location.getLatitude()+","+location.getLongitude();
            for(EmergencyContacts contact: contactsList){
                smsManager.sendTextMessage(contact.getNumber().toString(),
                        null,
                        message,
                        null, null);
            }

        });
    }

    private void setupBottomNavigationView() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        // handle navigation selection
        bottomNavigationView.setOnItemSelectedListener(
                item -> {
                    Fragment fragment = null;
                    switch (item.getItemId()) {
                        case R.id.action_map:
                            fragment = new MapFragment();
                            break;
                        case R.id.action_profile:
                            fragment = new ProfileUserFragment();
                            break;
                        case R.id.action_table:
                            fragment = new TableViewFragment();
                        default:
                            break;
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, fragment).commit();
                    return true;
                });
        // Set default selection
        bottomNavigationView.setSelectedItemId(R.id.action_map);
    }
}