package com.gamejam.crashrun;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.wearable.DataMap;
import com.mariux.teleport.lib.TeleportClient;

import java.util.UUID;

/**
 * Created by YuChen on 2015-01-30.
 */
public class WatchSync {
    private final static UUID PEBBLE_APP_UUID = UUID.fromString("5f54ae56-7776-4333-8f69-a44d9d2ee55a");
    private  boolean pebble_connected = false;
    private  TeleportClient mTeleportClient;
    Context c;
    private boolean wear_connected = false;
    private static WatchSync mInstance;
    private BroadcastReceiver connectedReceiver;
    private BroadcastReceiver disconnectedReceiver;

    public WatchSync(final Context c){
        //save the contest
        this.c = c;
        mInstance = this;
        //Check for android wear
        mTeleportClient = new TeleportClient(c);
    }
    public static WatchSync newInstance(Context c){
        if(mInstance != null) return mInstance;
        return new WatchSync(c);
    }
    protected void onStart() {
        mTeleportClient = new TeleportClient(c);
        mTeleportClient.connect();
        //Check if pebble is active
        pebble_connected = PebbleKit.isWatchConnected(c);
        connectedReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("CrashCourse", "Pebble connected!");
                // Launching my app
                PebbleKit.startAppOnPebble(c, PEBBLE_APP_UUID);
                // Closing my app
                //PebbleKit.closeAppOnPebble(getApplicationContext(), PEBBLE_APP_UUID);
            }
        };
        PebbleKit.registerPebbleConnectedReceiver(c, connectedReceiver);
        disconnectedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("CrashCourse", "Pebble disconnected!");
            }
        };

        PebbleKit.registerPebbleDisconnectedReceiver(c, disconnectedReceiver);
        wear_connected = true;
        promptAppStart();
    }

    protected void onStop() {
        mTeleportClient.disconnect();
        wear_connected = false;
        c.unregisterReceiver(connectedReceiver);
        c.unregisterReceiver(disconnectedReceiver);
    }

    public void sendUpdate(LatLng loc_user, LatLng loc_orb, String address, String time, byte vib){
        if(pebble_connected){
            PebbleDictionary data = new PebbleDictionary();
            if(loc_user != null && loc_orb != null){
                // Add a key of 0, for the distance
                float[] results = new float[3];
                Location.distanceBetween(loc_user.latitude,loc_user.longitude,loc_orb.latitude,loc_orb.longitude, results);
               // Log.d("Test",loc_user.latitude+ " " +loc_user.longitude+ " " +loc_orb.latitude+ " " +loc_orb.longitude);
               // Log.d("Test",results[0] + " " + results[1]+ " " + results[2]);
               // Log.d("Test", (int)results[0] + " " + (int)results[1]+ " " + (int) results[2]);
                data.addString(0, String.format("%.2f", results[0]) + " meters"); //DISTANCE
                if(results.length>=3)
                    data.addInt32(1, (int)results[2]); //BEARING
                else
                    data.addInt32(1, (int)results[1]); //BEARING
            }
            if(address != null)
            // Add a key of 4, and a string for the street.
                data.addString(4, address);
            if(time != null)
            // Add a key of 5, and a string for the timer.
                data.addString(5, time);
            data.addInt8(6, vib);

            PebbleKit.sendDataToPebble(c, PEBBLE_APP_UUID, data);
        }
        if(wear_connected){
            DataMap map = new DataMap();
            if(loc_user != null){
                map.putString("myloc_lat", String.valueOf(loc_user.longitude));
                map.putString("myloc_lon", String.valueOf(loc_user.latitude));
            }
            if(loc_orb != null) {
                map.putString("orb_lat", String.valueOf(loc_orb.longitude));
                map.putString("orb_lon", String.valueOf(loc_orb.latitude));
            }
            if(time != null)
                map.putString("timer", time);
            map.putByte("vib", vib);
            mTeleportClient.syncAll(map);
        }

    }
    public void promptAppStart(){
        if(wear_connected)mTeleportClient.sendMessage("startActivity", null);
    }
}
