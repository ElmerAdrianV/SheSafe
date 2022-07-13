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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.elmeradrianv.shesafe.R;
import com.elmeradrianv.shesafe.auxiliar.PinAnimation;
import com.elmeradrianv.shesafe.database.Report;
import com.elmeradrianv.shesafe.database.TypeOfCrime;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import permissions.dispatcher.NeedsPermission;

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {
    private static final String TAG = MapFragment.class.getSimpleName();
    private final static String KEY_LOCATION = "location";
    Location currentLocation;
    private GoogleMap map;


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
                Log.e(TAG, "Issue with getting reports", e);
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
        View messageView = LayoutInflater.from(getContext()).
                inflate(R.layout.new_report_item, null);
        setupEditDate(messageView);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialogBuilder.setView(messageView);
        HashMap<String, TypeOfCrime> crimes = new HashMap<>();
        queryTypeOfCrimes(messageView, crimes);

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
                (dialog, which) -> {
                    EditText etDescription = messageView.findViewById(R.id.etDescription);
                    if (etDescription.getText().toString().isEmpty()) {
                        Toast.makeText(getContext(), "Please fill the description", Toast.LENGTH_SHORT).show();
                    } else {
                        Spinner spinnerCrimes = messageView.findViewById(R.id.sTypeOfCrime);
                        TypeOfCrime crime = crimes.get(spinnerCrimes.getSelectedItem().toString());
                        EditText etDate = messageView.findViewById(R.id.etDate);
                        DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy"); // Make sure user insert date into edittext in this format.
                        try {
                            Date dateObject;
                            String dob_var = (etDate.getText().toString());
                            dateObject = formatter.parse(dob_var);
                            String date = new SimpleDateFormat("dd/MM/yyyy").format(dateObject);
                            createReport(etDescription.getText().toString(), dateObject, new ParseGeoPoint(latLng.latitude, latLng.longitude), crime);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        showMarker(crime.getTag(), latLng.latitude, latLng.longitude, crime.getLevelOfRisk());
                    }
                });
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                (dialog, id) -> dialog.cancel());
        alertDialog.show();
    }

    private void createReport(String description, Date date, ParseGeoPoint location, TypeOfCrime crime) {
        Report report = new Report();
        report.put(Report.DESCRIPTION_KEY, description);
        report.put(Report.DATE_KEY, date);
        report.put(Report.USER_KEY, ParseUser.getCurrentUser());
        report.put(Report.LOCATION_KEY, location);
        report.put(Report.TYPE_OF_CRIME_KEY, crime);
        report.saveInBackground(e -> {
            if (e != null) {
                Log.e(TAG, "showAlertDialogForPoint: exception", e);
            } else {
                Log.i(TAG, "showAlertDialogForPoint: report saved");
            }
        });
    }


    public void populateSpinner(List<TypeOfCrime> crimesList, HashMap<String, TypeOfCrime> crimes, View messageView) {
        List<String> list = new ArrayList<>();
        for (TypeOfCrime crime : crimesList) {
            crimes.put(crime.getTag(), crime);
            list.add(crime.get(TypeOfCrime.TAG_KEY).toString());
        }
        Spinner typesOfCrime = messageView.findViewById(R.id.sTypeOfCrime);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typesOfCrime.setAdapter(adapter);
        typesOfCrime.setSelection(0);
    }

    private void queryTypeOfCrimes(View messageView, HashMap<String, TypeOfCrime> crimes) {
        ParseQuery<TypeOfCrime> query = ParseQuery.getQuery(TypeOfCrime.class);
        query.addDescendingOrder(TypeOfCrime.LEVEL_OF_RISK_KEY);
        query.findInBackground((crimesList, e) -> {
            if (e != null) {
                Log.e(TAG, "Issue with getting crimes", e);
                return;
            } else {
                populateSpinner(crimesList, crimes, messageView);
            }
        });
    }

    private void setupEditDate(View messageView) {

        messageView.findViewById(R.id.sTypeOfCrime);
        EditText etDate = messageView.findViewById(R.id.etDate);
        DatePickerDialog.OnDateSetListener onDateSetListener = (view, year, month, dayOfMonth) -> {
            month = month + 1;
            String date = month + "/" + dayOfMonth + "/" + year;
            etDate.setText(date);
        };
        LocalDate date = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        String stringDate = date.format(formatter);
        etDate.setText(stringDate);
        DatePickerDialog datePickerDialog = new DatePickerDialog(MapFragment.this.getContext(), android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                onDateSetListener, date.getYear(), date.getMonthValue()+1, date.getDayOfMonth());
        datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        etDate.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                datePickerDialog.show();
            }
        });
        etDate.setOnClickListener(v -> {
            datePickerDialog.show();
        });
    }
}