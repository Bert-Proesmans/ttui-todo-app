package com.example.dries.project;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TODO_NOTIFICATION_CHANNEL_ID = "todo_channel";

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

       
    }

/*
                // Build the notification..
                NotificationCompat.Builder builder = new NotificationCompat.Builder(notification_context,
                        TODO_NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setDefaults(Notification.DEFAULT_SOUND)
                        .setContentTitle("Herinnering")
                        .setContentText("TEST")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                // .. and show it
                NotificationManagerCompat notification_manager = NotificationManagerCompat.from(notification_context);

                // Hack for using unique notification IDs
                int notification_id = (int)new Date().getTime();
                notification_manager.notify(notification_id, builder.build());
            }
        */

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
    //get text available in TextView/EditText
    private String getText(TextView textView) {
        return textView.getText().toString().trim();
    }
}