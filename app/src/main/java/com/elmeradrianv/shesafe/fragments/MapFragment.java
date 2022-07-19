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
import java.util.HashSet;
import java.util.List;

import permissions.dispatcher.NeedsPermission;

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {
    private static final String TAG = MapFragment.class.getSimpleName();
    private final static String KEY_LOCATION = "location";
    private static final int UPDATE_INTERVAL = 20000; //In milliseconds, 20s
    private static final int UPDATE_INTERVAL_SECONDS = 20; //In milliseconds, 20s
    private static final int FASTEST_INTERVAL = 5000; //In milliseconds, 5s
    private static final int SQUARE_GRID_LENGTH = 3;
    private static final int SQUARE_GRID_3X3_COUNT = SQUARE_GRID_LENGTH * SQUARE_GRID_LENGTH;
    private static final int SQUARE_CENTER_CENTER = 4;
    private static final int OUTSIDE_GRID = -1;
    private static final double SPEED_MAX_WALK = 8;
    private static final double SQUARE_SIZE_WALK = 0.005;
    private static final double SPEED_MAX_BIKE = 20;
    private static final double SQUARE_SIZE_BIKE = 0.015;
    private static final double SQUARE_SIZE_CAR = 0.045;
    private static final int MOV_WALK = 0;
    private static final int MOV_BIKE = 1;
    private static final int MOV_CAR = 2;
    private static final double LAT_TO_KM = 110.574;
    private static final double LNG_TO_KM = 111.320;

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
        reportsInGrid = new HashMap<>();
        markersInGrid = new HashMap<>();
        getMyLocation();
        startLocationUpdates();
        map.setOnMapLongClickListener(this);
    }

    private void queryFirstReports(double squareSize) {
        polygonGrid = getActualGridSquare(squareSize);
        for (Integer keySquare : polygonGrid.keySet()) {
            ParseQuery<Report> query = ParseQuery.getQuery(Report.class);
            query.include(Report.TYPE_OF_CRIME_KEY);
            query.whereWithinPolygon("location", polygonGrid.get(keySquare));
            query.addDescendingOrder("date");
            query.findInBackground((reportList, e) -> {
                if (e != null) {
                    Log.e(TAG, "Issue with getting reports", e);
                    return;
                }
                reportsInGrid.put(keySquare, new ArrayList<>());
                reportsInGrid.get(keySquare).addAll(reportList);
                showReports(keySquare, reportsInGrid.get(keySquare));
            });
        }
    }

    private void showReports(Integer keySquare, List<Report> reports) {
        markersInGrid.put(keySquare, new ArrayList<>());
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
                        queryFirstReports(SQUARE_SIZE_WALK);
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
        double oldSquareSize = polygonGrid.get(0).getCoordinates().get(1).getLatitude() - polygonGrid.get(0).getCoordinates().get(0).getLatitude();
        int oldWayMov = determinateWayToMove(oldSquareSize);
        double speedInKilometers = determinateSpeed(speedVector);
        double newSquareSize = determinateSizeBySpeed(speedInKilometers);
        int newWayToMove = determinateWayToMove(newSquareSize);
        if (oldWayMov != newWayToMove) {
            resizeSquare(newSquareSize);
        } else {
            ParseGeoPoint actualLocation = new ParseGeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
            int gridPosition = getGridPosition(actualLocation);
            if (gridPosition != SQUARE_CENTER_CENTER) {
                recenterGrid(gridPosition, newSquareSize);
            }
        }
    }

    private void resizeSquare(double newSquareSize) {
        removeMarkersFromGrid(markersInGrid);
        removeReportsFromWholeGrid();
        queryFirstReports(newSquareSize);
    }

    private int determinateWayToMove(double squareSize) {
        if (Math.abs(squareSize - SQUARE_SIZE_WALK) <= 0.001) {
            return MOV_WALK;
        }
        if (Math.abs(squareSize - SQUARE_SIZE_BIKE) <= 0.001) {
            return MOV_BIKE;
        } else {
            return MOV_CAR;
        }
    }


    private double determinateSpeed(LatLng speedVector) {
        double distance = Math.sqrt(
                Math.pow(latitudeToKilometer(speedVector.latitude), 2)
                        +
                        Math.pow(longitudeToKilometer(speedVector.latitude), 2)
        );
        return distance / UPDATE_INTERVAL_SECONDS;
    }

    private double longitudeToKilometer(double latitude) {
        return LNG_TO_KM * Math.cos(latitude);
    }

    private double latitudeToKilometer(double latitude) {
        return latitude * LAT_TO_KM;
    }

    private double determinateSizeBySpeed(double speed) {
        if (speed < SPEED_MAX_WALK) {
            return SQUARE_SIZE_WALK;
        }
        if (speed < SPEED_MAX_BIKE) {
            return SQUARE_SIZE_BIKE;
        } else {
            return SQUARE_SIZE_CAR;
        }
    }

    private void recenterGrid(int newGridPosition, double newSquareSize) {
        if (newGridPosition == OUTSIDE_GRID) {
            removeMarkersFromGrid(markersInGrid);
            removeReportsFromWholeGrid();
            queryFirstReports(newSquareSize);
        } else {
            int newCenterGridPositionRow = newGridPosition % SQUARE_GRID_LENGTH;
            int newCenterGridPositionColumn = newGridPosition / SQUARE_GRID_LENGTH;
            int centerGridPositionRow = SQUARE_CENTER_CENTER % SQUARE_GRID_LENGTH;
            int centerGridPositionColumn = SQUARE_CENTER_CENTER / SQUARE_GRID_LENGTH;
            int columnDisplacement = newCenterGridPositionColumn - centerGridPositionColumn;
            int rowDisplacement = newCenterGridPositionRow - centerGridPositionRow;
            HashMap<Integer, ArrayList<Marker>> removeMarkers = new HashMap<>();
            if (columnDisplacement != 0) {
                pushGridVertical(columnDisplacement, removeMarkers);
            }
            if (rowDisplacement != 0) {
                pushGridHorizontal(rowDisplacement, removeMarkers);
            }
            removeMarkersFromGrid(removeMarkers);
            polygonGrid = getActualGridSquare(newSquareSize);
            requeryReports(columnDisplacement, rowDisplacement);
        }
    }

    private int getGridPosition(ParseGeoPoint actualLocation) {
        for (int keySquare : polygonGrid.keySet()) {
            if (polygonGrid.get(keySquare).containsPoint(actualLocation)) {
                return keySquare;
            }
        }
        return OUTSIDE_GRID;
    }

    private void pushGridHorizontal(int rowDisplacement, HashMap<Integer, ArrayList<Marker>> removeMarkers) {
        for (int keySquare = 0; keySquare < SQUARE_GRID_3X3_COUNT; keySquare++) {
            int oldGridRow = keySquare / SQUARE_GRID_LENGTH;
            int newKeySquarePosition = keySquare - rowDisplacement;
            int newGridRow = newKeySquarePosition / SQUARE_GRID_LENGTH;
            if (newGridRow == oldGridRow && 0 <= newKeySquarePosition && newKeySquarePosition <= 9) {
                polygonGrid.replace(newKeySquarePosition, polygonGrid.get(keySquare));
                markersInGrid.replace(newKeySquarePosition, markersInGrid.get(keySquare));
                reportsInGrid.replace(newKeySquarePosition, reportsInGrid.get(keySquare));
            }
        }
    }

    private void pushGridVertical(int columnDisplacement, HashMap<Integer, ArrayList<Marker>> removeMarkers) {
        for (int keySquare = 0; keySquare < SQUARE_GRID_3X3_COUNT; keySquare++) {
            int newKeySquarePosition = keySquare - SQUARE_GRID_LENGTH * columnDisplacement;
            if (0 <= newKeySquarePosition && newKeySquarePosition <= 9) {
                polygonGrid.replace(newKeySquarePosition, polygonGrid.get(keySquare));
                markersInGrid.replace(newKeySquarePosition, markersInGrid.get(keySquare));
                reportsInGrid.replace(newKeySquarePosition, reportsInGrid.get(keySquare));
            }
        }
    }

    private void requeryReports(int columnDisplacement, int rowDisplacement) {
        HashSet<Integer> squareKeys = obtainSquareKeysToQuery(columnDisplacement, rowDisplacement);
        for (Integer keySquare : squareKeys) {
            ParseQuery<Report> query = ParseQuery.getQuery(Report.class);
            query.include(Report.TYPE_OF_CRIME_KEY);
            query.whereWithinPolygon("location", polygonGrid.get(keySquare));
            query.addDescendingOrder("date");
            query.findInBackground((reportList, e) -> {
                if (e != null) {
                    Log.e(TAG, "Issue with getting reports", e);
                    return;
                }
                reportsInGrid.put(keySquare, new ArrayList<>());
                reportsInGrid.get(keySquare).addAll(reportList);
                showReports(keySquare, reportsInGrid.get(keySquare));
            });
        }
    }

    private HashSet<Integer> obtainSquareKeysToQuery(int columnDisplacement, int rowDisplacement) {
        HashSet<Integer> squareKeys = new HashSet<>();
        if (columnDisplacement != 0) {
            int centerRow = SQUARE_CENTER_CENTER / SQUARE_GRID_LENGTH;
            int horizontalGap = centerRow + columnDisplacement;
            int horizontalStart = 3 * horizontalGap - 3 * horizontalGap % SQUARE_GRID_LENGTH;
            for (int i = horizontalStart; i < horizontalStart + SQUARE_GRID_LENGTH; i++) {
                squareKeys.add(i);
            }
        }
        if (rowDisplacement != 0) {
            int centerColumn = SQUARE_CENTER_CENTER % SQUARE_GRID_LENGTH;
            int verticalGap = centerColumn + rowDisplacement;
            verticalGap -= verticalGap / SQUARE_GRID_LENGTH;
            int verticalStart = verticalGap - verticalGap / SQUARE_GRID_LENGTH;
            for (int i = verticalStart; i < verticalStart + 3 * SQUARE_GRID_LENGTH; i += 3) {
                squareKeys.add(i);
            }
        }

        return squareKeys;
    }

    private void removeReportsFromWholeGrid() {
        for (int keySquare : reportsInGrid.keySet()) {
            reportsInGrid.get(keySquare).clear();
        }
    }

    private void removeMarkersFromGrid(HashMap<Integer, ArrayList<Marker>> markersInGrid) {
        for (int keySquare : markersInGrid.keySet()) {
            for (Marker marker : markersInGrid.get(keySquare)) {
                marker.remove();
            }
        }
    }


    private double positiveRemainder(double divisor, double dividend) {
        double quotient = Math.floor(dividend / divisor);
        return dividend - divisor * quotient;
    }

    private HashMap<Integer, ParsePolygon> getActualGridSquare(double squareSize) {
        HashMap<Integer, ArrayList<ParseGeoPoint>> grid = new HashMap<>();
        // abbr. CS means central square
        double cornerLatitudeCS = currentLocation.getLatitude() - positiveRemainder(squareSize, currentLocation.getLatitude());
        double cornerLongitudeCS = currentLocation.getLongitude() - positiveRemainder(squareSize, currentLocation.getLongitude());
        //saving the number of points in the grid
        LatLng[][] gridCorners = new LatLng[SQUARE_GRID_LENGTH + 1][SQUARE_GRID_LENGTH + 1];
        for (int i = 0; i < gridCorners.length; i++) {
            for (int j = 0; j < gridCorners[0].length; j++) {
                gridCorners[i][j] = new LatLng(
                        cornerLatitudeCS - (1 - i) * squareSize,
                        cornerLongitudeCS - (1 - j) * squareSize
                );
            }
        }
        for (int keySquare = 0; keySquare < SQUARE_GRID_3X3_COUNT; keySquare++) {
            grid.put(keySquare, new ArrayList<>());
            PolygonOptions polygonOptions = new PolygonOptions();
            int gridCornerStartColumn = keySquare % SQUARE_GRID_LENGTH;
            int gridCornerStartRow = keySquare / SQUARE_GRID_LENGTH;
            for (int i = gridCornerStartColumn; i < gridCornerStartColumn + 2; i++) {
                //Saving the corners of each square
                if (i - gridCornerStartColumn == 0) {
                    for (int j = gridCornerStartRow; j < gridCornerStartRow + 2; j++) {
                        grid.get(keySquare).add(new ParseGeoPoint(
                                gridCorners[i][j].latitude,
                                gridCorners[i][j].longitude)
                        );
                        polygonOptions.add(gridCorners[i][j]);
                    }
                } else {
                    for (int j = gridCornerStartRow + 1; j >= gridCornerStartRow; j--) {
                        grid.get(keySquare).add(new ParseGeoPoint(
                                gridCorners[i][j].latitude,
                                gridCorners[i][j].longitude)
                        );
                        polygonOptions.add(gridCorners[i][j]);
                    }
                }
            }
            polygonOptions.add(gridCorners[gridCornerStartColumn][gridCornerStartRow]);
            Polygon polygon = map.addPolygon(polygonOptions);
        }
        return getGridPolygons(grid);
    }

    private HashMap<Integer, ParsePolygon> getGridPolygons(HashMap<Integer, ArrayList<ParseGeoPoint>> grid) {
        HashMap<Integer, ParsePolygon> gridPolygons = new HashMap<>();
        for (int k = 0; k < SQUARE_GRID_3X3_COUNT; k++) {
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
        }
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
                onDateSetListener, date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth());
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