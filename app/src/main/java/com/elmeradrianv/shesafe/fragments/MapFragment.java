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
import android.os.Looper;
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
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.parse.ParseGeoPoint;
import com.parse.ParsePolygon;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import permissions.dispatcher.NeedsPermission;

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {
    private static final String TAG = MapFragment.class.getSimpleName();
    private final static String KEY_LOCATION = "location";
    private static final int UPDATE_INTERVAL = 60000; //In milliseconds, 60s
    private static final int FASTEST_INTERVAL = 5000; //In milliseconds, 5s
    private Location currentLocation;
    private HashMap<Integer, ParsePolygon> polygonGrid;
    private HashMap<Integer, ArrayList<Report>> reportsInGrid;
    private HashMap<Integer, ArrayList<Marker>> markersInGrid;
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
        startLocationUpdates();
        map.setOnMapLongClickListener(this);
    }

    private void queryFirstReports() {
        polygonGrid = getActualGridSquare();
        reportsInGrid = new HashMap<>();
        markersInGrid = new HashMap<>();
        for (Integer k : polygonGrid.keySet()) {
            ParseQuery<Report> query = ParseQuery.getQuery(Report.class);
            query.include(Report.TYPE_OF_CRIME_KEY);
            query.whereWithinPolygon("location", polygonGrid.get(k));
            query.addDescendingOrder("date");
            query.findInBackground((reportList, e) -> {
                if (e != null) {
                    Log.e(TAG, "Issue with getting reports", e);
                    return;
                }
                reportsInGrid.put(k, new ArrayList<>());
                reportsInGrid.get(k).addAll(reportList);
                showReports(k,reportsInGrid.get(k));
            });
        }
    }
    private void  showReports(Integer keySquare, List<Report> reports) {
        markersInGrid.put(keySquare,new ArrayList<>());
        for (Report report : reports) {
            Marker marker = showMarker(report.getTypeOfCrime().getTag(),
                    report.getLocation().getLatitude(),
                    report.getLocation().getLongitude(),
                    report.getTypeOfCrime().getLevelOfRisk());
            markersInGrid.get(keySquare).add(marker);
        }
    }

    @SuppressWarnings({"MissingPermission"})
    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    private void getMyLocation() {
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);

        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(getContext());
        locationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        //firstDisplayLocation;
                        currentLocation = location;
                        onLocationChanged(location);
                        displayLocation();
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
        double longitudePFL = location.getLongitude() - currentLocation.getLongitude();
        double latitudePFL = location.getLatitude() - currentLocation.getLatitude();
        currentLocation = location;
        LatLng speedVector = new LatLng(latitudePFL, longitudePFL);
        LatLng actualLocation = new LatLng(
                currentLocation.getLatitude(),
                currentLocation.getLongitude());
    }

    private double positiveRemainder(double divisor, double dividend) {
        double quotient = Math.floor(dividend / divisor);
        return dividend - divisor * quotient;
    }

    private HashMap<Integer, ParsePolygon> getActualGridSquare() {
        HashMap<Integer, ArrayList<ParseGeoPoint>> grid = new HashMap<>();
        //squareSize is a "debug number and it will change depends of the speedVector"
        double squareSize = 0.005;
        int numSquareSizeGrid = 3;
        double cornerLatitudeCC = currentLocation.getLatitude() - positiveRemainder(squareSize, currentLocation.getLatitude());
        double cornerLongitudeCC = currentLocation.getLongitude() - positiveRemainder(squareSize, currentLocation.getLongitude());
        //saving the number of points in the grid
        LatLng[][] gridCorners = new LatLng[numSquareSizeGrid + 1][numSquareSizeGrid + 1];
        for (int i = 0; i < gridCorners.length; i++) {
            for (int j = 0; j < gridCorners[0].length; j++) {
                gridCorners[i][j] = new LatLng(
                        cornerLatitudeCC - (1 - i) * squareSize,
                        cornerLongitudeCC - (1 - j) * squareSize
                );
            }
        }
        //k is nine because the grid has 9 squares
        for (int k = 0; k < 9; k++) {
            grid.put(k, new ArrayList<>());
            PolygonOptions polygonOptions = new PolygonOptions();
            for (int i = k % numSquareSizeGrid; i < k % numSquareSizeGrid + 2; i++) {
                //Saving the corners of each square
                if (i - k % numSquareSizeGrid == 0) {

                    for (int j = k / numSquareSizeGrid; j < k / numSquareSizeGrid + 2; j++) {
                        grid.get(k).add(new ParseGeoPoint(
                                gridCorners[i][j].latitude,
                                gridCorners[i][j].longitude)
                        );
                        polygonOptions.add(gridCorners[i][j]);
                    }
                } else {
                    for (int j = k / numSquareSizeGrid + 1; j >= k / numSquareSizeGrid; j--) {
                        grid.get(k).add(new ParseGeoPoint(
                                gridCorners[i][j].latitude,
                                gridCorners[i][j].longitude)
                        );
                        polygonOptions.add(gridCorners[i][j]);
                    }
                }
            }
            polygonOptions.add(gridCorners[k % numSquareSizeGrid][k / numSquareSizeGrid]);
            Polygon polygon = map.addPolygon(polygonOptions);
        }
        return getGridPolygons(grid);
    }

    private HashMap<Integer, ParsePolygon> getGridPolygons(HashMap<Integer, ArrayList<ParseGeoPoint>> grid) {
        HashMap<Integer, ParsePolygon> gridPolygons = new HashMap<>();
        for (int k = 0; k < 9; k++) {
            gridPolygons.put(k,
                    new ParsePolygon(
                            grid.get(k)
                    ));
        }
        return gridPolygons;
    }

    private void displayLocation() {
        if (currentLocation != null) {
            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 14);
            map.animateCamera(cameraUpdate);
            queryFirstReports();
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
            //showReports(reports);
        });
        return reports;
    }



    private Marker showMarker(String title, double latitude, double longitude, int levelOfRisk) {
        LatLng reportLatLng = new LatLng(latitude, longitude);
        MarkerOptions markerOptions = new MarkerOptions()
                .position(reportLatLng)
                .title(title).icon(PinAnimation.getNewIconWithLevelOfRisk(levelOfRisk));
        Marker marker = map.addMarker(markerOptions);
        PinAnimation.dropPinEffect(marker);
        return marker;
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
                        createReport(messageView, latLng, crimes);
                    }
                });
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                (dialog, id) -> dialog.cancel());
        alertDialog.show();
    }

    private void createReport(View messageView, LatLng latLng, HashMap<String, TypeOfCrime> crimes) {
        Spinner spinnerCrimes = messageView.findViewById(R.id.sTypeOfCrime);
        TypeOfCrime crime = crimes.get(spinnerCrimes.getSelectedItem().toString());
        EditText etDate = messageView.findViewById(R.id.etDate);
        EditText etDescription = messageView.findViewById(R.id.etDescription);
        DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy"); // Make sure user insert date into edittext in this format.
        try {
            Date dateObject;
            String dob_var = (etDate.getText().toString());
            dateObject = formatter.parse(dob_var);
            saveReportInDataBase(etDescription.getText().toString(), dateObject, new ParseGeoPoint(latLng.latitude, latLng.longitude), crime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        showMarker(crime.getTag(), latLng.latitude, latLng.longitude, crime.getLevelOfRisk());
    }

    private void saveReportInDataBase(String description, Date date, ParseGeoPoint location, TypeOfCrime crime) {
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
                onDateSetListener, date.getYear(), date.getMonthValue() -1, date.getDayOfMonth());
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

    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    protected void startLocationUpdates() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();
        SettingsClient settingsClient = LocationServices.getSettingsClient(getContext());
        settingsClient.checkLocationSettings(locationSettingsRequest);
        //noinspection MissingPermission
        getFusedLocationProviderClient(getContext()).requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        onLocationChanged(locationResult.getLastLocation());
                    }
                },
                Looper.myLooper());
    }

}