package com.icechen1.crashcourse;

import android.content.Intent;

import com.mariux.teleport.lib.TeleportService;

/**
 * Created by YuChen on 2015-01-31.
 */
public class WearService extends TeleportService {
    private static final String STARTACTIVITY = "startActivity";

    @Override
    public void onCreate() {
        super.onCreate();

        setOnGetMessageTask(new StartActivityTask());

    }

    //Task that shows the path of a received message
    public class StartActivityTask extends TeleportService.OnGetMessageTask {

        @Override
        protected void onPostExecute(String path) {

            if (path.equals(STARTACTIVITY)) {

                Intent startIntent = new Intent(getBaseContext(), MainActivity.class);
                startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(startIntent);
            }

            //let`s reset the task (otherwise it will be executed only once)
            setOnGetMessageTask(new StartActivityTask());
        }
    }
}