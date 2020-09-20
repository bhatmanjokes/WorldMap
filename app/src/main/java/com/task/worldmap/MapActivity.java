package com.task.worldmap;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.task.worldmap.storage.DBHelper;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "OnMapReady: Map is ready");
        mMap = googleMap;

        if (mPermissionGranted && isServiceValid()) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this,   Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

            init();


        }

    }

    private static final String TAG = "MapActivity";

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static int AUTOCOMPLETE_REQUEST_CODE = 1;
    private static DBHelper dbHelper;
    //widgets
    private Button btnDirection;
    private ImageView mGps;
    private AutoCompleteTextView mAutoComplete;


    //variables
    private Boolean mPermissionGranted = false;
    private GoogleMap mMap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);


        dbHelper = new DBHelper(MapActivity.this);
        mGps = (ImageView) findViewById(R.id.ic_gps);
        btnDirection = findViewById(R.id.btn_direction);
        mAutoComplete = (AutoCompleteTextView) findViewById(R.id.autoComplete);

        Places.initialize(getApplicationContext(),  getString(R.string.google_maps_API_key), Locale.US);

        final com.task.worldmap.PlaceAutoSuggestAdapter placeAutoSuggestAdapter = new com.task.worldmap.PlaceAutoSuggestAdapter(MapActivity.this, R.layout.list_item_1);
        mAutoComplete.setAdapter(placeAutoSuggestAdapter);


        getLocationPermission();

    }

    private void init(){

        mAutoComplete.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                btnDirection.setVisibility(View.GONE);
                if(actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || event.getAction() == KeyEvent.ACTION_DOWN
                        || event.getAction() == KeyEvent.KEYCODE_ENTER){

                    geoLocate();

                }
                return false;
            }
        });


        mAutoComplete.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnDirection.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final String locationData = mAutoComplete.getText().toString().trim();
                if (!dbHelper.checkWhereLocationIsAvailableOrNot(locationData)) {
                    showAlertForSavingTheLocation(locationData);
                } else if(dbHelper.getRowCount() == 0){
                    showAlertForSavingTheLocation(locationData);
                }else {
                    btnDirection.setVisibility(View.VISIBLE);
                    geoLocate();
                }
            }
        });



        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: GPS icon is clicked");
                getDeviceLocation();
            }
        });
        hideSoftKeyboard();
    }

    private void showAlertForSavingTheLocation(final String locationData) {
        btnDirection.setVisibility(View.VISIBLE);
        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
        builder.setTitle("Save Location data? ");
        builder.setMessage("Saving location data with title");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                View bottomSheetView = getLayoutInflater().inflate(R.layout.bottle_sheet, null);
                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MapActivity.this);
                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.setCancelable(false);
                final BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from((View) bottomSheetView.getParent());
                TextView tvLocationData = bottomSheetView.findViewById(R.id.tv_selected_location);
                final EditText etTitle = bottomSheetView.findViewById(R.id.et_title);
                tvLocationData.setText(locationData);
                bottomSheetView.findViewById(R.id.btn_save).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!etTitle.getText().toString().trim().isEmpty()) {
                            dbHelper.insertSearchLocality(locationData, etTitle.getText().toString().trim());
                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                            bottomSheetDialog.dismiss();
                            Toast.makeText(MapActivity.this, "Location Saved SuccessFully", Toast.LENGTH_SHORT).show();
                            geoLocate();
                        } else {
                            etTitle.setError("Please enter title");
                        }
                    }
                });

                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                bottomSheetDialog.show();

            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setCancelable(false);
        if (!isFinishing()) {
            builder.show();
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                assert data != null;
                Place place = Autocomplete.getPlaceFromIntent(data);
                geoLocate();

                Log.i(TAG, "Place: " + place.getName() + ", " + place.getAddress());// place.getLatLng());
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                assert data != null;
                Status status = Autocomplete.getStatusFromIntent(data);
                assert status.getStatusMessage() != null;
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void geoLocate(){
        Log.d(TAG, "geoLocate: Locating address");

//        String searchString = mSearchText.getText().toString();
        String searchString = mAutoComplete.getText().toString();

        Geocoder geocoder = new Geocoder(MapActivity.this);
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(searchString, 1);
        }catch (IOException e){
            Log.e(TAG, "geoLocate: IOException" + e.getMessage());
        }

        if(list.size() >0){
            Address address = list.get(0);

            Log.d(TAG, "geoLocate: found a location" + address.toString());

            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM
                    , address.getAddressLine(0));
        }
    }

    private void getDeviceLocation(){
        Log.d(TAG,"getDeviceLocation: fetching the device location");

        FusedLocationProviderClient mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if(mPermissionGranted){
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "onComplete: found location!");
                            Location currentLocation = (Location) task.getResult();

                            assert currentLocation != null;
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                    DEFAULT_ZOOM, "Your Location");

                        }else {
                            Log.d(TAG, "onComplete: current location not found");
                            Toast.makeText(MapActivity.this, "unable to get current location ", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

        } catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, float zoom, String title){
        Log.d(TAG, "moveCamera moving the camera to lat: " + latLng.latitude + ", lng: " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if(!title.equals("Your Location")) {
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title);
            mMap.addMarker(options);
        }

        hideSoftKeyboard();
    }

    private void initMap(){
        Log.d(TAG, "intMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        assert mapFragment != null;
        mapFragment.getMapAsync(MapActivity.this);

    }

    private void getLocationPermission(){
        Log.d(TAG, "getLocationPermission: Requesting location permission");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mPermissionGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionResult: Called Here");
        mPermissionGranted = false;

        if ((requestCode) == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        mPermissionGranted = false;
                        Log.d(TAG, "onRequestPermissionResult: Permission Denied");
                        return;
                    }
                }
                Log.d(TAG, "OnRequestPermissionResult: Permission Granted");
                mPermissionGranted = true;

                //initialize maps
                initMap();
            }
        }
    }

    private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public boolean isServiceValid(){
        Log.d(TAG, "isServicesValid: Checking google service version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MapActivity.this);

        if(available == ConnectionResult.SUCCESS){
            //Successful and user can initiate map service requests
            Log.d(TAG, "isServiceValid: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //Error handling
            Log.d(TAG, "isServiceValid: an error occurred but can be fixed");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MapActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }
        else{
            Toast.makeText(this, "You can't initiate map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

}

