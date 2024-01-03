package com.example.opsc7312_poe;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
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
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

//TaskLoadedCallback

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback {
    private static final String TAG = "MapActivity";

    public static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(-40, -168), new LatLng(71, 136));

    //widgets
    private AutoCompleteTextView mSearchText;
    private ImageView mGps;
    Button btnGetDirections;
    Button btnAddFavourite;
    Button btnViewFavourites;
    ImageView imgInfoBackground;
    TextView txtInformation;
    String lastSearch = "Home";
    public static ArrayList<String> favouriteLocations = new ArrayList<String>();
    //variables
    private Boolean mLocationPermissionGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private GoogleApiClient mGoogleApiClient;

    //get directions
    GoogleMap map;
    MarkerOptions place1, place2;
    Polyline currentPolyLine;
    public static final int ERROR_DIALOG_REQUEST = 9001;
    Address ad1;
    LatLng currentAddress;
    LatLng destinationAddress;
    SupportMapFragment mapFragment;
    FirebaseDatabase database;
    DatabaseReference dbRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mSearchText = (AutoCompleteTextView) findViewById(R.id.input_search);
        mGps = (ImageView)findViewById(R.id.ic_gps);
        btnGetDirections = findViewById(R.id.btnGetDirection);
        btnAddFavourite = findViewById(R.id.btnAddFavourite);
        imgInfoBackground = findViewById(R.id.imgInfoBackground);
        txtInformation = findViewById(R.id.txtShowInfo);
//        btnViewFavourites = findViewById(R.id.btnViewFavourites);

        getLocationPermission();

        //get directions
        if(isServicesOK())
        {
            init();
        }


        Button btnMapSettings = findViewById(R.id.btnSettings);
        btnMapSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MapsActivity.this, SettingsActivity.class));
            }
        });

        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference();
    }


    private void init()
    {
        Log.d(TAG, "init: initializing");

        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if(actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || event.getAction() == KeyEvent.ACTION_DOWN
                        || event.getAction() == KeyEvent.KEYCODE_ENTER)
                {
                    //Execute our method for searching

                    geoLocate();
                }

                return false;
            }
        });

        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDeviceLocation();

                txtInformation.setText(null);
                imgInfoBackground.setVisibility(View.INVISIBLE);
            }
        });

        btnGetDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Current Address: " + currentAddress.latitude + ", " + currentAddress.longitude);
                Log.d(TAG, "Destination Address: " + destinationAddress.latitude + ", " + destinationAddress.longitude);

                place1 = new MarkerOptions().position(new LatLng(currentAddress.latitude, currentAddress.longitude)).title("User Location");
                place2 = new MarkerOptions().position(new LatLng(destinationAddress.latitude, destinationAddress.longitude)).title("Destination");

                String url = getUrl(place1.getPosition(), place2.getPosition(), "driving");
                new FetchURL(MapsActivity.this).execute(url, "driving");
                Log.d(TAG, "URL:: " + url);
            }
        });

        btnAddFavourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean found = false;
                if(!lastSearch.equals(null))
                {
                    favouriteLocations.add(lastSearch);

                }
            }
        });
//        btnViewFavourites.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(MapsActivity.this, FavouritesActivity.class));
//
//            }
//        });

        hideSoftKeyboard();
    }

    private void geoLocate()
    {
        Log.d(TAG, "geoLocate: geoLocating");

        String searchString = mSearchText.getText().toString();
        lastSearch = searchString;
        Geocoder geocoder = new Geocoder(MapsActivity.this);
        List<Address> list = new ArrayList<>();

        try
        {
            list = geocoder.getFromLocationName(searchString, 1);
        }
        catch(IOException e)
        {
            Log.d(TAG, "geoLocate: IOException " + e.getMessage());
        }

        if(list.size() > 0)
        {
            Address address = list.get(0);
            ad1 = address;
            destinationAddress = new LatLng(address.getLatitude(), address.getLongitude());
            mMap.addMarker(new MarkerOptions().position(destinationAddress).title(searchString));
            hideSoftKeyboard();

            float results[] = new float[1];
            Location.distanceBetween(currentAddress.latitude, currentAddress.longitude,
                    destinationAddress.latitude, destinationAddress.longitude,
                    results);

            //Task<DataSnapshot> dataSnapshot = database.getReference().child("Users").child("Measurement").get();
            String measure = String.valueOf(results[0]);

            double distance = convertMetersToKmOrMiles(results[0], "Metric");
            String dist = String.format("%.2f", distance);
            boolean temp = true;
            if (temp)
            {
                txtInformation.setText("Distance to Location: " + dist + "km");
            }
            else
            {
                txtInformation.setText("Distance to Location: " + dist + "Mi");

            }
            imgInfoBackground.setVisibility(View.VISIBLE);

            Log.d(TAG, "geoLocate: found a location: " + address.toString());

            moveCamera(new LatLng(destinationAddress.latitude, destinationAddress.longitude), DEFAULT_ZOOM,
                    address.getAddressLine(0));
            mapFragment.getMapAsync(MapsActivity.this);

        }
    }

    private double convertMetersToKmOrMiles(double meters, String kmOrMiles)
    {

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child("Users").child("User1").child("123").getValue() == null){}

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if(kmOrMiles == "METRIC")
        {
            double kilometers = meters / 1000;

            return kilometers;
        }

        if(kmOrMiles == "IMPERIAL")
        {
            double miles = meters / 1609.34;

            return miles;
        }

        return 0;
    }

    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the device's current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionGranted) {
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {//We were able to find the location
                            Log.d(TAG, "onComplete: found location!");
                            Location currentLocation = (Location) task.getResult();

                            currentAddress = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

                            moveCamera(new LatLng(currentLocation.getLatitude(),
                                            currentLocation.getLongitude()),
                                    DEFAULT_ZOOM,
                                    "My Location");
                            ;

                        } else {
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MapsActivity.this, "unable to get current " +
                                    "location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: Security Exception" + e.getMessage());
        }
    }

    private void moveCamera(LatLng latlng, float zoom, String title) {
        Log.d(TAG, "moveCamera: Moving camera to lat: " + latlng.latitude
                + ", long: " + latlng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoom));

        if(!title.equals("My Location"))
        {
            MarkerOptions options = new MarkerOptions().position(latlng).title(title);
            mMap.addMarker(options);

            MarkerOptions information = new MarkerOptions().position(latlng).title(
                    "Phone: " + ad1.getPhone() + ". URL: " + ad1.getUrl() + ". Extras: " + ad1.getExtras()
            );
            mMap.addMarker(information);
        }

        hideSoftKeyboard();
    }

    private void initMap() {
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(MapsActivity.this);
    }

    private void getLocationPermission() {
        String[] permissions = {FINE_LOCATION, COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mLocationPermissionGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionGranted = true;

                    //initialize our map
                    initMap();
                }
            }
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Toast.makeText(this, "Map is ready", Toast.LENGTH_SHORT).show();
        mMap = googleMap;

        if (mLocationPermissionGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

            init();
        }
    }

    private void hideSoftKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    public void viewFavourites(View view)
    {
        startActivity(new Intent(MapsActivity.this, FavouritesActivity.class));

    }
    //get directions
    @Override
    public void onTaskDone(Object... values) {
        if(currentPolyLine != null)
        {
            currentPolyLine.remove();
        }

        currentPolyLine = mMap.addPolyline((PolylineOptions) values[0]);

    }

    //Method to check the version
    public boolean isServicesOK()
    {
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(MapsActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google PLay Services is working");
            return true;
        }
        else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occurred but we can resolve it
            Log.d(TAG, "");
            Dialog dialog = GoogleApiAvailability.getInstance()
                    .getErrorDialog(MapsActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }
        else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private String getUrl(LatLng origin, LatLng dest, String directionMode)
    {
        //Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        //Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        //Mode
        String mode = "mode=" + directionMode;

        //Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;

        //Output format
        String output = "json";

        //Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters
                + "&key=" + getString(R.string.google_maps_API_key);

        return url;
    }

}