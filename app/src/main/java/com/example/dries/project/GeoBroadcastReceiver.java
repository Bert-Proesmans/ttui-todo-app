package com.example.dries.project;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Bert on 27/02/2018.
 */

public class GeoBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Tunnel this receive event into our job-worker
        GeofenceTransitionService.enqueueWork(context, intent);
    }
}
