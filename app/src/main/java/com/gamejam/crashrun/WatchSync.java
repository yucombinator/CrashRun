package com.gamejam.crashrun;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.google.android.gms.maps.model.LatLng;

import java.util.UUID;

/**
 * Created by YuChen on 2015-01-30.
 */
public class WatchSync {
    private final static UUID PEBBLE_APP_UUID = UUID.fromString("EC7EE5C6-8DDF-4089-AA84-C3396A11CC95");
    private final boolean pebble_connected;

    Context c;

    public WatchSync(final Context c){
        //save the contest
        this.c = c;
        //Check if pebble is active
        pebble_connected = PebbleKit.isWatchConnected(c);
        PebbleKit.registerPebbleConnectedReceiver(c, new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("CrashCourse", "Pebble connected!");
                // Launching my app
                PebbleKit.startAppOnPebble(c, PEBBLE_APP_UUID);
                // Closing my app
                //PebbleKit.closeAppOnPebble(getApplicationContext(), PEBBLE_APP_UUID);
            }
        });
        PebbleKit.registerPebbleDisconnectedReceiver(c, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("CrashCourse", "Pebble disconnected!");
            }
        });
    }

    public void sendUpdate(LatLng loc_user, LatLng loc_orb, String address){
        if(pebble_connected){
            PebbleDictionary data = new PebbleDictionary();
            // Add a key of 0, for the user's longitude
            data.addString(0, String.valueOf(loc_user.longitude));
            // Add a key of 1, for the user's latitude
            data.addString(1, String.valueOf(loc_user.latitude));
            // Add a key of 2, for the orb's longitude
            data.addString(2, String.valueOf(loc_orb.longitude));
            // Add a key of 3, for the orb's latitude
            data.addString(3, String.valueOf(loc_orb.latitude));
            // Add a key of 4, and a string for the street.
            data.addString(4, address);

            PebbleKit.sendDataToPebble(c, PEBBLE_APP_UUID, data);
        }

    }
}
