package com.example.androidprogrammingbook05;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.job.JobScheduler;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class PollService extends IntentService{
    private static final String TAG = "PoolService";
    public static final String ACTION_SHOW_NOTIFICATION = "com.example.androidprogrammingbook05.SHOW_NOTIFICATION";
    public static final String PERM_PRIVATE = "com.example.androidprogrammingbook05.PRIVATE";
    public static final String REQUEST_CODE = "REQUEST_CODE";
    public static final String NOTIFICATION = "NOTIFICATION";
    //private static final long POLL_INTERVAL_MS = TimeUnit.MINUTES.toMillis(15);
    private static final long POLL_INTERVAL_MS = TimeUnit.SECONDS.toMillis(60);

    public static Intent newIntent(Context context) {
        return new Intent(context, PollService.class);
    }

    public PollService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (!isNetworkAvailableAndConnected()) {
            return;
        }
        //Log.i(TAG, "Received an intent: " + intent);
        String query = QueryPreferences.getStoredQuery(this);
        String lastResultID = QueryPreferences.getLastResultID(this);
        GalleryItemRaw itemRaw;

        if (query == null) {
            itemRaw = new FlickrFetchr().fetchRecentPhoto(null);
        } else {
            itemRaw = new FlickrFetchr().searchPhotos(query, null);
        }
        List<GalleryItem> items = itemRaw.mPhotos.mItems;

        if (items.size() == 0) return;

        String resultID = items.get(0).getID();

        if (resultID.equals(lastResultID)) {
            Log.i(TAG, "Got an old result: " + resultID);
        } else {
            Log.i(TAG, "Got a new result: " + resultID);

            Resources resources = getResources();
            Intent i = PhotoGalleryActivity.newIntent(this);
            PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
            Notification notification = new NotificationCompat.Builder(this)
                    .setTicker(resources.getString(R.string.new_picture_title))
                    .setSmallIcon(android.R.drawable.ic_menu_report_image)
                    .setContentTitle(resources.getString(R.string.new_picture_title))
                    .setContentText(resources.getString(R.string.new_pictures_text))
                    .setContentIntent(pi)
                    .setAutoCancel(true)
                    .build();
//            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
//            notificationManagerCompat.notify(0, notification);
            showBackgroundNotification(0, notification);

            sendBroadcast(new Intent(ACTION_SHOW_NOTIFICATION), PERM_PRIVATE);
        }

        QueryPreferences.setLastResultID(this, resultID);

    }

    private void showBackgroundNotification(int requestCode, Notification notification) {
        Intent i = new Intent(ACTION_SHOW_NOTIFICATION);
        i.putExtra(REQUEST_CODE, requestCode);
        i.putExtra(NOTIFICATION, notification);
        sendOrderedBroadcast(i, PERM_PRIVATE, null, null, Activity.RESULT_OK, null, null);
    }

    private boolean isNetworkAvailableAndConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        boolean isNetWorkAvailable = cm.getActiveNetworkInfo() != null;
        boolean isNetworkConnected = isNetWorkAvailable && cm.getActiveNetworkInfo().isConnected();
        return isNetworkConnected;
    }

    public static void setServiceAlarm(Context context, boolean isOn){
        Intent i = PollService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context, 0, i, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if(isOn){
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), POLL_INTERVAL_MS, pi);
        }else{
            alarmManager.cancel(pi);
            pi.cancel();
        }

        QueryPreferences.setAlarmOn(context, isOn);
    }

    public static boolean isServiceAlarmOn(Context context){
        Intent i = PollService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_NO_CREATE);
        return pi != null;
    }
}

