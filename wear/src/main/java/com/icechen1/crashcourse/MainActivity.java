package com.icechen1.crashcourse;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.wearable.DataMap;
import com.mariux.teleport.lib.TeleportClient;

public class MainActivity extends Activity {

    private TextView mTextView;
    private TeleportClient mTeleportClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
            }
        });
        //Check for android wear
        mTeleportClient = new TeleportClient(this);
        mTeleportClient.setOnSyncDataItemTask(new OnSyncDataItemTask());
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
            long time = dataMap.getLong("timer");

            //let`s create a pretty Toast with our string!
            Toast.makeText(getApplicationContext(), String.valueOf(time), Toast.LENGTH_SHORT).show();

        }
    }
}
