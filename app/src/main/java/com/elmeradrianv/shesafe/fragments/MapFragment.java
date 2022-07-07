package com.elmeradrianv.shesafe.fragments;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.elmeradrianv.shesafe.R;
import com.elmeradrianv.shesafe.auxiliar.PinAnimation;
import com.elmeradrianv.shesafe.database.Report;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

import permissions.dispatcher.NeedsPermission;

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {
    private static final String TAG = MapFragment.class.getSimpleName();
    private final static String KEY_LOCATION = "location";
    Location currentLocation;
    private GoogleMap map;
    private LocationRequest locationRequest;

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
        if (savedInstanceState != null && savedInstanceState.keySet().contains(KEY_LOCATION)) {
            // Since KEY_LOCATION was found in the Bundle, we can be sure that mCurrentLocation
            // is not null.
            currentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
        }
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
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        getMyLocation();
        queryReports();
        map.setOnMapLongClickListener(this);
    }

    @SuppressWarnings({"MissingPermission"})
    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    void getMyLocation() {
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);

        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(getContext());
        locationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        onLocationChanged(location);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d("MapDemoActivity", "Error trying to get last GPS location");
                    e.printStackTrace();
                });
    }

    public void onLocationChanged(Location location) {
        // GPS may be turned off
        if (location == null) {
            return;
        }
        currentLocation = location;
        displayLocation();
    }

    private void displayLocation() {
        if (currentLocation != null) {
            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 14);
            map.animateCamera(cameraUpdate);
        }
    }


    private List<Report> queryReports() {
        List<Report> reports = new ArrayList<>();
        ParseQuery<Report> query = ParseQuery.getQuery(Report.class);
        query.include(Report.TYPE_OF_CRIME_KEY);
        query.setLimit(50);
        query.addDescendingOrder("createdAt");
        query.findInBackground((reportList, e) -> {
            if (e != null) {
                Log.e(TAG, "Issue with getting posts", e);
                return;
            }
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
        MarkerOptions markerOptions = new MarkerOptions()
                .position(reportLatLng)
                .title(title).icon(PinAnimation.getNewIconWithLevelOfRisk(levelOfRisk));
        Marker marker = map.addMarker(markerOptions);
        PinAnimation.dropPinEffect(marker);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        Toast.makeText(getContext(), "Detected", Toast.LENGTH_SHORT).show();
        showAlertDialogForPoint(latLng);
    }

    private void showAlertDialogForPoint(final LatLng latLng) {
        // inflate message_item.xml view
        View messageView = LayoutInflater.from(getContext()).
                inflate(R.layout.new_report_item, null);
        // Create alert dialog builder
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        // set message_item.xml to AlertDialog builder
        alertDialogBuilder.setView(messageView);
        messageView.findViewById(R.id.ddlTypeOfCrime);
        EditText etDate = messageView.findViewById(R.id.etDate);
        DatePickerDialog.OnDateSetListener onDateSetListener = (view, year, month, dayOfMonth) -> {
            month = month+1;
            String date = dayOfMonth+"/"+month+"/"+year;
            etDate.setText(date);
        };
        etDate.setOnClickListener(v->
                {
                    DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                            onDateSetListener, 2002, 8, 02);
                    datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    datePickerDialog.show();
            });

        // Create alert dialog
        final AlertDialog alertDialog = alertDialogBuilder.create();
        // Configure dialog button (OK)
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
                (dialog, which) -> {
                    // Creates and adds marker to the map
                    Marker marker = map.addMarker(new MarkerOptions()
                            .position(latLng));
                    // Animate marker using drop effect
                    // --> Call the dropPinEffect method here
                    PinAnimation.dropPinEffect(marker);
                });
        // Configure dialog button (Cancel)
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                (dialog, id) -> dialog.cancel());
        // Display the dialog
        alertDialog.show();
    }
}