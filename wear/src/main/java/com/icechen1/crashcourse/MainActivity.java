package com.icechen1.crashcourse;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.wearable.DataMap;
import com.mariux.teleport.lib.TeleportClient;

public class MainActivity extends Activity {

    private TextView mTimer;
    private TextView mDistance;
    private TeleportClient mTeleportClient;
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTimer = (TextView) stub.findViewById(R.id.timer);
                mDistance = (TextView) stub.findViewById(R.id.distance);
            }
        });
        //Check for android wear
        mTeleportClient = new TeleportClient(this);
        mTeleportClient.connect();
        mTeleportClient.setOnSyncDataItemTask(new OnSyncDataItemTask());
        //Toast.makeText(getApplicationContext(), "GOT MESSAGE MAN", Toast.LENGTH_SHORT).show();
        //Log.e("CrashCourse", "GOT MESSAGE MAN");
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    }
    @Override
    protected void onStart() {
        super.onStart();
        mTeleportClient.connect();
    }
    @Override
    protected void onStop() {
        super.onStop();
        mTeleportClient.disconnect();

    }

    public class OnSyncDataItemTask extends TeleportClient.OnSyncDataItemTask {

        @Override
        protected void onPostExecute(DataMap dataMap) {
            //let`s get the String from the DataMap, using its identifier key
            String time = dataMap.getString("timer");
            double myloc_lat = Double.valueOf(dataMap.getString("timer"));
            double myloc_lon = Double.valueOf(dataMap.getString("timer"));
            double orb_lat = Double.valueOf(dataMap.getString("timer"));
            double orb_lon = Double.valueOf(dataMap.getString("timer"));

            //vibrate on demand
            byte vib = dataMap.getByte("timer");
            if(vib>0){
                long[] vibrationPattern = {0, 500, 50, 300};
                //-1 - don't repeat
                final int indexInPatternToRepeat = -1;
                vibrator.vibrate(vibrationPattern, indexInPatternToRepeat);
            }
            if(time != null){
                mTimer.setText(time);
            }
            float[] results = new float[3];
            Location.distanceBetween(myloc_lat, myloc_lon, orb_lat, orb_lon, results);
            if(myloc_lat != 0 && orb_lat != 0){
                mTimer.setText(time);
            }
            //let`s create a pretty Toast with our string!
            //Toast.makeText(getApplicationContext(), String.valueOf(time), Toast.LENGTH_SHORT).show();
            mTeleportClient.setOnSyncDataItemTask(new OnSyncDataItemTask());
        }
    }
}
