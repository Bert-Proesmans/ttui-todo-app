package com.example.dries.project;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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

import com.example.dries.project.google_example.GeofenceMain;
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

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "GeofenceMain";
    public static final String TODO_NOTIFICATION_CHANNEL_ID = "todo_channel";
    public static final String NEW_HERINNERING_ID_KEY = "new_herinnering_id";

    private GeofenceMain geofenceDriver;

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
        title = (TextView) findViewById(R.id.total);

        databaseHelper = new DatabaseHelper(this);
        herinneringList = new ArrayList<>();
        reloadingDatabase(); //loading table of DB to ListView

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

        geofenceDriver = new GeofenceMain();
        geofenceDriver.onCreate(this);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        // Toast.makeText(this, requestCode, Toast.LENGTH_SHORT).show();
        long new_model_id = data.getLongExtra(NEW_HERINNERING_ID_KEY, -1);
        if (resultCode == RESULT_OK && new_model_id > -1) {
            reloadingDatabase();

            Herinnering new_model = null;
            for (Herinnering h : databaseHelper.getAllHerinnerings()) {
                if (h.getId() == (new_model_id)) {
                    new_model = h;
                    break;
                }
            }

            setupGeofence(new_model);
        }

        if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "canceled", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupGeofence(Herinnering model) {

        if (model == null) {
            Log.e(TAG, "Passed down model is NULL");
            Log.w(TAG, "No geofence added..");
            return;
        }

        // Build geofence
        final String todo_description = model.getDescription();
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

        geofenceDriver.addGeofence(fence);
        // geofenceDriver.removeGeofencesHandler();
        geofenceDriver.addGeofencesHandler();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this,
                        TODO_NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setDefaults(Notification.DEFAULT_SOUND)
                        .setContentTitle("Herinnering")
                        .setContentText(todo_description)
                        .setPriority(NotificationCompat.PRIORITY_HIGH);
                NotificationManagerCompat man = NotificationManagerCompat.from(MainActivity.this);
                //
                int notification_id = (int)new Date().getTime();
                man.notify(notification_id, builder.build());
            }
        }, 25*1000);
    }

    //get text available in TextView/EditText
    private String getText(TextView textView) {
        return textView.getText().toString().trim();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == GeofenceMain.REQUEST_PERMISSIONS_REQUEST_CODE) {
            geofenceDriver.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}