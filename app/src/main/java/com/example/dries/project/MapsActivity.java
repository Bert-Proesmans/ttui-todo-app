package com.example.dries.project;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerDragListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private DatabaseHelper databaseHelper = new DatabaseHelper(this);;


    private double longitude;
    private double latitude;
   // private GoogleApiClient googleApiClient;
    private LatLng center;
    Double latitudecenter;
    Double longtitudecenter;
    String coordlat;
    String coordlong;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



        final EditText nameBox = findViewById(R.id.item_name);
        final  EditText descriptionBox = findViewById(R.id.item_description);
        final Button button = findViewById(R.id.item_ready);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                long new_herinnering_model_key = -1;

                if(nameBox.getText().toString().matches("")){
                    Herinnering herinnering = new Herinnering("no name", descriptionBox.getText().toString(),coordlat,coordlong);
                    new_herinnering_model_key = databaseHelper.addNewHerinnering(herinnering);

                }else{

                    Herinnering herinnering = new Herinnering(nameBox.getText().toString(), descriptionBox.getText().toString(),coordlat,coordlong);
                    new_herinnering_model_key = databaseHelper.addNewHerinnering(herinnering);
                }


                Intent returnIntent = new Intent();
                returnIntent.putExtra(MainActivity.NEW_HERINNERING_ID_KEY, new_herinnering_model_key);
                setResult(MapsActivity.RESULT_OK, returnIntent);
                finish();

               // reloadingDatabase();
            }
        });

        final Button buttoncancel = findViewById(R.id.item_cancel);
        buttoncancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent returnIntent = new Intent();
                setResult(MapsActivity.RESULT_CANCELED, returnIntent);
                finish();

            }
        });

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        // googleMapOptions.mapType(googleMap.MAP_TYPE_HYBRID)
        //    .compassEnabled(true);

        // Add a marker in Sydney and move the camera
        LatLng india = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(india).title("Marker in India"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(india));
        mMap.setOnMarkerDragListener(this);
        mMap.setOnMapLongClickListener(this);

        center = india;

        latitudecenter = center.latitude;
        longtitudecenter =center.longitude;
        coordlat = latitudecenter.toString();
        coordlong = longtitudecenter.toString();

    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng).draggable(true));
        center = latLng;

        latitudecenter = center.latitude;
        longtitudecenter =center.longitude;
        coordlat = latitudecenter.toString();
        coordlong = longtitudecenter.toString();
        //NU NOG TOEVOEGEN AAN DB en DOORSTUREN ALS HERRINING OBJ
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        Toast.makeText(MapsActivity.this, "onMarkerDragStart", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        Toast.makeText(MapsActivity.this, "onMarkerDrag", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        // getting the Co-ordinates
        latitude = marker.getPosition().latitude;
        longitude = marker.getPosition().longitude;

        //move to current position
        moveMap();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Toast.makeText(MapsActivity.this, "onMarkerClick", Toast.LENGTH_SHORT).show();
        return true;
    }

    private void moveMap() {
        /**
         * Creating the latlng object to store lat, long coordinates
         * adding marker to map
         * move the camera with animation
         */
        LatLng latLng = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .draggable(true)
                .title("Marker in India"));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        mMap.getUiSettings().setZoomControlsEnabled(true);


    }
}
