package com.elmeradrianv.shesafe.fragments;

import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.elmeradrianv.shesafe.R;
import com.elmeradrianv.shesafe.database.Report;
import com.elmeradrianv.shesafe.database.TypeOfCrime;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

//@RuntimePermissions
public class MapFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = MapFragment.class.getSimpleName();

    private GoogleMap map;
    private LocationRequest locationRequest;
    Location currentLocation;
    private long UPDATE_INTERVAL = 60000;  /* 60 secs */
    private long FASTEST_INTERVAL = 5000; /* 5 secs */
    private final static String KEY_LOCATION = "location";

    /*
     * Define a request code to send to Google Play services This code is
     * returned in Activity.onActivityResult
     */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;


    public MapFragment() {
        // Required empty public constructor
    }

    public static MapFragment newInstance() {
        MapFragment fragment = new MapFragment();
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment;
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * <p>
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
//        map.setOnMapLongClickListener(this);
//      //   Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        map.addMarker(new MarkerOptions()
//                .position(sydney)
//                .title("Marker in Sydney"));
//        map.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        queryReports();
    }

    private void dropPinEffect(Marker marker) {
        // Handler allows us to repeat a code block after a specified delay
        final android.os.Handler handler = new android.os.Handler();
        final long start = SystemClock.uptimeMillis();
        final long duration = 1500;

        // Use the bounce interpolator
        final android.view.animation.Interpolator interpolator =
                new BounceInterpolator();

        // Animate marker with a bounce updating its position every 15ms
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                // Calculate t for bounce based on elapsed time
                float t = Math.max(
                        1 - interpolator.getInterpolation((float) elapsed
                                / duration), 0);
                // Set the anchor
                marker.setAnchor(0.5f, 1.0f + 14 * t);

                if (t > 0.0) {
                    // Post this event again 15ms from now.
                    handler.postDelayed(this, 15);
                } else { // done elapsing, show window
                    marker.showInfoWindow();
                }
            }
        });
    }

    private List<Report> queryReports() {
        List<Report> reports = new ArrayList<>();
        // specify what type of data we want to query - Post.class
        ParseQuery<Report> query = ParseQuery.getQuery(Report.class);
        // include data referred by user key
        query.include(Report.TYPE_OF_CRIME_KEY);
        // limit query to latest 50 items
        query.setLimit(50);
//        query.setSkip(currentLimit-NUMBER_POSTS_REQUEST); // skip the first 10 results
        // order posts by creation date (newest first)
        query.addDescendingOrder("createdAt");
        // start an asynchronous call for posts
        query.findInBackground((reportList, e) -> {
            // check for errors
            if (e != null) {
                Log.e(TAG, "Issue with getting posts", e);
                return;
            }
            // save received posts to list and notify adapter of new data
            reports.addAll(reportList);
            showReports(reports);
        });
        return reports;
    }

    private void showReports(List<Report> reports) {

        for (Report report : reports) {
            showMarker(report.getTypeOfCrime().getTag(),
                    report.getLocation().getLatitude(),
                    report.getLocation().getLongitude(),
                    report.getTypeOfCrime().getLevelOfRisk());
        }
    }


    private void showMarker(String title, double latitude, double longitude, int levelOfRisk) {
        LatLng reportLatLng = new LatLng(latitude, longitude);
        Marker marker;
        MarkerOptions markerOptions = new MarkerOptions()
                .position(reportLatLng)
                .title(title);
        switch (levelOfRisk) {
            case TypeOfCrime.LOW_RISK:
                markerOptions.icon(getNewIcon( IconGenerator.STYLE_WHITE));
                break;
            case TypeOfCrime.MEDIUM_LOW_RISK:
                markerOptions.icon(getNewIcon( IconGenerator.STYLE_ORANGE));
                break;
            case TypeOfCrime.MEDIUM_RISK:
                markerOptions.icon(getNewIcon(IconGenerator.STYLE_RED));
                break;
            case TypeOfCrime.MEDIUM_HIGH_RISK:
                markerOptions.icon(getNewIcon( IconGenerator.STYLE_PURPLE));
                break;
            case TypeOfCrime.HIGH_RISK:
                markerOptions.icon(getNewIcon(IconGenerator.STYLE_BLUE));
                break;
        }
        marker = map.addMarker(markerOptions);
        dropPinEffect(marker);
    }

    private BitmapDescriptor getNewIcon(int color) {
        BitmapDescriptor icon=BitmapDescriptorFactory.fromResource(R.drawable.ic_ss_marker_yellow);;
        switch (color) {
            case 1:
                 icon= BitmapDescriptorFactory.fromResource(R.drawable.ic_ss_marker_yellow);
                break;
            case 2:
               icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_ss_marker_orange);
                break;
            case 3:
                 icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_ss_marker_red);
                break;
            case 4:
                 icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_ss_marker_blue);
                break;
            case 5:
                icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_ss_marker_purple);
                break;
        }
        return icon;
    }
}