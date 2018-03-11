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
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnCompleteListener<Void> {

    private static final String TAG = "MainActivity";
    public static final String TODO_NOTIFICATION_CHANNEL_ID = "todo_channel";
    private static final int REQUEST_PERMISSION_REQUEST_ID = 34;
    public static final String NEW_HERINNERING_ID_KEY = "new_herinnering_id";

    private GeofencingClient geofenceClient;
    private GeofencingRequest pendingGeoRequest;
    private PendingIntent geoPendingIntent;

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

        if(!checkPermissions()) {
            requestPermissions();
        }

        Intent intent = new Intent(this, GeoBroadcastReceiver.class);
        geoPendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
       // Toast.makeText(this, requestCode, Toast.LENGTH_SHORT).show();
        long new_model_id = data.getLongExtra(NEW_HERINNERING_ID_KEY, -1);
        if(resultCode== RESULT_OK && new_model_id > -1)
        {
            reloadingDatabase();

            Herinnering new_model = null;
            for (Herinnering h: databaseHelper.getAllHerinnerings()) {
                if (h.getId() == (new_model_id)) {
                    new_model = h;
                    break;
                }
            }

            setupGeofence(new_model);

//            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, TODO_NOTIFICATION_CHANNEL_ID)
//                    .setSmallIcon(R.drawable.ic_launcher_foreground)
//                    .setContentTitle("TODO Notificationn")
//                    .setContentText("Title: test")
//                    .setDefaults(NotificationCompat.DEFAULT_ALL)
//                    .setPriority(NotificationCompat.PRIORITY_HIGH);
//            int notification_id = (int)new Date().getTime();
//            NotificationManagerCompat.from(this).notify(notification_id, mBuilder.build());

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

    private void setupGeofence(Herinnering model) {

        if(model == null) {
            Log.e(TAG, "Passed down model is NULL");
            Log.w(TAG, "No geofence added..");
            return;
        }

        // Build geofence
        String todo_description = model.getDescription();
        double todo_latitude = Double.parseDouble(model.getCoordlat()),
                todo_longtitude = Double.parseDouble(model.getCoordlong());
        // TODO; Make this changeable through the model
        int todo_radius = 200;

        Log.d(TAG, "Setting up GEOFENCE");
        Geofence fence = new Geofence.Builder()
                .setRequestId(todo_description)
                // degrees, degrees and meters!
                .setCircularRegion(todo_latitude, todo_longtitude, todo_radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();

        // Build request to get notified for fence.
        GeofencingRequest request = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(fence)
                .build();
        this.pendingGeoRequest = request;
        // Further execution flows through onRequestPermissionsResult!

        // Test for necessary user provided permissions before storing geofence requests
        if(!checkPermissions()) {
            Log.w(TAG, "Insufficient permissions!");
            return;
        }

        performPendingGeofenceRequest();
    }

    @SuppressLint("MissingPermission")
    private void performPendingGeofenceRequest() {
        if(this.pendingGeoRequest == null) {
            return;
        }

        if(!checkPermissions()) {
            requestPermissions();
            return;
        }

        // Push the request to the client handling geo updates.
        // The intent indicates what code must be executed on trigger.

        this.geofenceClient.addGeofences(pendingGeoRequest, geoPendingIntent).addOnCompleteListener(this);
        Log.i(TAG, "Geofence set!");
        this.pendingGeoRequest = null;
    }

    //get text available in TextView/EditText
    private String getText(TextView textView) {
        return textView.getText().toString().trim();
    }

    @Override
    public void onComplete(@NonNull Task<Void> task) {
        if(task.isSuccessful()) {
            Toast.makeText(this, "Geofence added", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Geofence added onComplete success");
        } else {
            String errorMessage = "Error setting geofence!";
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
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
                performPendingGeofenceRequest();
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