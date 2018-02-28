package com.example.dries.project;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;
import android.content.Intent;

import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private DatabaseHelper databaseHelper = new DatabaseHelper(this);;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        final RadioGroup rGroup = (RadioGroup)findViewById(R.id.radioGroup);
        final RadioButton buttonCurrent = findViewById(R.id.buttoncurrent);
        final RadioButton buttonMarked = findViewById(R.id.buttonmarked);



        final EditText nameBox = findViewById(R.id.item_name);
        final  EditText descriptionBox = findViewById(R.id.item_description);
        final Button button = findViewById(R.id.item_ready);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ////
                //GOOGLE MAPS DATA MOET NOG UITGEHAALD WORDEN
                //GOOGLE MAPS DATA MOET TOEGEVOEGD WORDEN AAN DE DB
                //GOOGLE MAPS DATA MOET TOEGEVOEGD WORDEN AAN HERINNERING
                // Executie van MainActivity.setupGeofence(..)
                ////
                if(nameBox.getText().toString().matches("")){
                    Herinnering herinnering = new Herinnering("no name", descriptionBox.getText().toString());
                    databaseHelper.addNewHerinnering(herinnering);

                }else{
                    Herinnering herinnering = new Herinnering(nameBox.getText().toString(), descriptionBox.getText().toString());
                    databaseHelper.addNewHerinnering(herinnering);
                }

                if(buttonCurrent.isChecked()) {
                    Toast.makeText(MapsActivity.this, "current location checked", Toast.LENGTH_LONG).show();
                }

                Intent returnIntent = new Intent();
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

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.buttoncurrent:
                if (checked)
                    // blalbabala
                    break;
            case R.id.buttonmarked:
                if (checked)
                    // baljblajopeijvaoj
                    break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}
