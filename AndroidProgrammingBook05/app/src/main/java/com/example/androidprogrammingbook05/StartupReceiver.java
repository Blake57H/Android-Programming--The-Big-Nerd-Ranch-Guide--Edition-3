package com.example.androidprogrammingbook05;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class StartupReceiver extends BroadcastReceiver {
    private static final String TAG = "StartupReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Received broadcast intent: " + intent.getAction());

        boolean isOn = QueryPreferences.isAlarmOn(context);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            PollService_JobScheduler.setJobScheduler(context, isOn);
        }else{
            PollService.setServiceAlarm(context, isOn);
        }
    }
}
