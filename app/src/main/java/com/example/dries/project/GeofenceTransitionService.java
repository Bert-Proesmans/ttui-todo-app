package com.example.dries.project;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.Date;
import java.util.List;

public class GeofenceTransitionService extends JobIntentService {

    private static final int JOB_ID = 2654;
    private static final String TAG = "GeoTransitionService";

    public GeofenceTransitionService() {
        super();
    }

    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, GeofenceTransitionService.class, JOB_ID, intent);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Log.d(TAG, "Doing work");
        GeofencingEvent geoEvent = GeofencingEvent.fromIntent(intent);
        if(geoEvent.hasError()) {
            Log.e(TAG, "Geo-event has error");
            return;
        }

        Log.d(TAG, "Processing GEO-event");
        int geofenceTransition = geoEvent.getGeofenceTransition();
        if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            List<Geofence> triggeringFences = geoEvent.getTriggeringGeofences();

            StringBuilder ss = new StringBuilder();
            for(Geofence fence: triggeringFences) {
                ss.append(fence.getRequestId());
                ss.append(", ");
            }

            String notificationBody = ss.toString();
            this.sendNotification(notificationBody);

            Log.i(TAG, "GeoFence ENTER processed: " + notificationBody);
        } else {
            Log.d(TAG, "GeoFence unknown transition");
        }
    }

    private void sendNotification(String body) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MainActivity.TODO_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentTitle("Todo reminder")
                .setContentText(body);

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        // Hack for using unique notification IDs
        int notification_id = (int)new Date().getTime();
        notificationManager.notify(notification_id, builder.build());
    }
}
