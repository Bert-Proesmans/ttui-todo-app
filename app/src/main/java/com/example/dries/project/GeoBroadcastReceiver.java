package com.example.dries.project;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Bert on 27/02/2018.
 */

public class GeoBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("GEO-RECEIVER", "Received GEO event notification!");
        // Tunnel this receive event into our job-worker
        GeofenceTransitionService.enqueueWork(context, intent);
    }
}
