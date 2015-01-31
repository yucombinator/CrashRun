package com.gamejam.crashrun;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.google.android.gms.maps.model.LatLng;
import com.mariux.teleport.lib.TeleportClient;

import java.util.UUID;

/**
 * Created by YuChen on 2015-01-30.
 */
public class WatchSync {
    private final static UUID PEBBLE_APP_UUID = UUID.fromString("EC7EE5C6-8DDF-4089-AA84-C3396A11CC95");
    private  boolean pebble_connected = false;
    private  TeleportClient mTeleportClient;
    Context c;
    private boolean wear_connected = false;

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
        //Check for android wear
        mTeleportClient = new TeleportClient(c);
    }
    protected void onStart() {
        mTeleportClient.connect();
        wear_connected = true;
    }

    protected void onStop() {
        mTeleportClient.disconnect();
        wear_connected = false;

    }

    public void sendUpdate(LatLng loc_user, LatLng loc_orb, String address, int time){
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
            // Add a key of 4, and a string for the timer.
            data.addInt32(5, time);

            PebbleKit.sendDataToPebble(c, PEBBLE_APP_UUID, data);
        }
        if(wear_connected){
            mTeleportClient.syncString("myloc_lat", String.valueOf(loc_user.longitude));
            mTeleportClient.syncString("myloc_lon", String.valueOf(loc_user.latitude));
            mTeleportClient.syncString("orb_lat", String.valueOf(loc_orb.longitude));
            mTeleportClient.syncString("orb_lon", String.valueOf(loc_orb.latitude));
            mTeleportClient.syncString("address", address);
            mTeleportClient.syncLong("timer", time);
        }

    }
}
