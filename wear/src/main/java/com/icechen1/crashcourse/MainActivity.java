package com.icechen1.crashcourse;

import android.app.Activity;
import android.graphics.Matrix;
import android.location.Location;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.wearable.DataMap;
import com.mariux.teleport.lib.TeleportClient;

public class MainActivity extends Activity {

    private TextView mTimer;
    private TextView mDistance;
    private TeleportClient mTeleportClient;
    private Vibrator vibrator;
    private ImageView mArrow;

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
                mArrow = (ImageView) stub.findViewById(R.id.arrow);
            }
        });
        //Check for android wear
        mTeleportClient = new TeleportClient(this);
        mTeleportClient.connect();
        mTeleportClient.setOnSyncDataItemTask(new OnSyncDataItemTask());

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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

            double myloc_lat = 0;
            if(dataMap.getString("myloc_lat") != null)
                myloc_lat = Double.valueOf(dataMap.getString("myloc_lat"));

            double myloc_lon = 0;
            if(dataMap.getString("myloc_lon") != null)
                myloc_lon = Double.valueOf(dataMap.getString("myloc_lon"));

            double orb_lat = 0;
            if(dataMap.getString("orb_lat") != null)
                orb_lat = Double.valueOf(dataMap.getString("orb_lat"));

            double orb_lon = 0;
            if(dataMap.getString("orb_lon") != null)
                orb_lon = Double.valueOf(dataMap.getString("orb_lon"));
            Log.d("SpeedRun",myloc_lat + " " + myloc_lon + " " + orb_lat + " " + orb_lon);
            //vibrate on demand
            byte vib = dataMap.getByte("vib");
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
            if(results[0] >0){
                mDistance.setText(String.format("%.2f", results[0]) + " meters");
                //Draw arrow and move heading
                float angle = results[1];
                if(results[2]!=0){
                    angle= results[2];
                }
                //Float angle = 75.0f;
                if(angle <0){
                    angle = angle + 360.0f;
                }
                (new DrawableOperation()).rotateDrawable(angle,mArrow);
            }
            try{
                mTeleportClient.setOnSyncDataItemTask(new OnSyncDataItemTask());
            }catch(Exception e){
                // :(
            }

        }
        class DrawableOperation extends Thread{
            Matrix rotateMatrix;
            public DrawableOperation(){
                rotateMatrix=new Matrix();
            }
            private void rotateDrawable(float angle, final ImageView target)
            {
                rotateMatrix.reset();
                try{
                    rotateMatrix.postRotate(-angle, target.getDrawable().getBounds().width()/2, target.getDrawable().getBounds().height()/2);
                    runOnUiThread(new Runnable(){
                        @Override
                        public void run() {
                            target.setImageMatrix(rotateMatrix);
                            //target.setGravity(Gravity.CENTER);
                            target.setScaleType(ImageView.ScaleType.MATRIX);   //required
                        }
                    });
                }catch(NullPointerException e){
                    Log.e("SpeedRun", "NullPointerException while drawing arrow");
                }
            }
        }
    }
}
