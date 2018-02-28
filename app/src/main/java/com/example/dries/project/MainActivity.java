package com.example.dries.project;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnCompleteListener<Void> {

    private static final String TAG = "MainActivity";
    public static final String TODO_NOTIFICATION_CHANNEL_ID = "todo_channel";
    private static final int REQUEST_PERMISSION_REQUEST_ID = 34;

    private GeofencingClient geofenceClient;

    private ListView listView;
    private ListViewAdapter adapter;
    private DatabaseHelper databaseHelper;
    private List<Herinnering> herinneringList;
    private TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.list_view);
        title = (TextView)findViewById(R.id.total);

        databaseHelper = new DatabaseHelper(this);
        herinneringList = new ArrayList<>();
        reloadingDatabase(); //loading table of DB to ListView

        geofenceClient = LocationServices.getGeofencingClient(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            CharSequence name = "todo_channel";
            String description = "Notifications about TODOs";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(TODO_NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void reloadingDatabase() {
        herinneringList = databaseHelper.getAllHerinnerings();
        if (herinneringList.size() == 0) {
            Toast.makeText(this, "No record found in database!", Toast.LENGTH_SHORT).show();
            title.setVisibility(View.GONE);
        }
        adapter = new ListViewAdapter(this, R.layout.item_listview, herinneringList, databaseHelper);
        listView.setAdapter(adapter);
        title.setVisibility(View.VISIBLE);
        title.setText("Total notifications: " + databaseHelper.getContactsCount());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add) {
            addingNewHerinneringDialog();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addingNewHerinneringDialog() {

        //GEEN DIALOG MEER MAAR VERWIJZING NAAR MAPSACTIVITY!!!
        Intent intent = new Intent(MainActivity.this, MapsActivity.class);
        startActivityForResult(intent, 1);

       //  setupGeofence(herinnering);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
       // Toast.makeText(this, requestCode, Toast.LENGTH_SHORT).show();
        if(resultCode== RESULT_OK)
        {
            reloadingDatabase();

            Toast.makeText(this, "added", Toast.LENGTH_SHORT).show();
        }
        if(resultCode==RESULT_CANCELED){

            Toast.makeText(this, "canceled", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if(shouldProvideRationale) {
            View.OnClickListener ok_listener = new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    // Actually request permission
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_PERMISSION_REQUEST_ID);
                }
            };

            // Use a snackbar to present the user the request rationale.
            Snackbar.make(findViewById(R.id.main_content_pane),
                    "Fine access is necessary for geofences to work",
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction("Enable", ok_listener)
                    .show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSION_REQUEST_ID);
        }
    }

    @SuppressLint("MissingPermission")
    private void setupGeofence(Herinnering model) {
        // Test for necessary user provided permissions before storing geofence requests
        if(!checkPermissions()) {
            requestPermissions();
            if(!checkPermissions()) {
                Log.w(TAG, "Insufficient permissions!");
                return;
            }
        }

        // Build geofence
        String todo_description = "";
        double todo_latitude = 0,
                todo_longtitude = 0;
        int todo_radius = 0;

        Geofence fence = new Geofence.Builder()
            .setRequestId(todo_description)
            // degrees, degrees and meters!
            .setCircularRegion(todo_latitude, todo_longtitude, todo_radius)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
            .build();

        // Build request to get notified for fence.
        GeofencingRequest request = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(fence)
                .build();

        // Push the request to the client handling geo updates.
        // The intent indicates what code must be executed on trigger.
        Intent intent = new Intent(this, GeoBroadcastReceiver.class);
        PendingIntent geoProcessIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        this.geofenceClient.addGeofences(request, geoProcessIntent).addOnCompleteListener(this);
        Log.i(TAG, "Geofence set!");
    }

    //get text available in TextView/EditText
    private String getText(TextView textView) {
        return textView.getText().toString().trim();
    }

    @Override
    public void onComplete(@NonNull Task<Void> task) {
        if(task.isSuccessful()) {
            Toast.makeText(this, "Geofence added", Toast.LENGTH_SHORT).show();
        } else {
            String errorMessage = task.getException().getMessage();
            Log.e(TAG, errorMessage);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_PERMISSION_REQUEST_ID) {
            if(grantResults.length <= 0) {
                Log.i(TAG, "Permission interaction cancelled");
            } else if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Received adequate permissions");
            } else {
                // Permission denied
                Snackbar.make(findViewById(R.id.main_content_pane),
                        "Geofences aren't available without permissions",
                        Snackbar.LENGTH_SHORT)
                        .show();
            }
        }
    }
}