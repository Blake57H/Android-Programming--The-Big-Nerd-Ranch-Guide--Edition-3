package com.example.androidprogrammingbook05;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.app.job.JobWorkItem;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class PollService_JobScheduler extends JobService {
    private static final String TAG = "PollServiceJobScheduler";
    private static final int JOB_ID = 57;

    private boolean jobCanceled = false;

    public ComponentName newComponentName(){
        return new ComponentName(this, PollService_JobScheduler.class);
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.i(TAG, "Job started");
        doingJob(jobParameters);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.i(TAG, "Job stopped before completion");
        return false;
    }

    private void doingJob(JobParameters parameters){
        //Log.i(TAG, "Received an intent: " + intent);
        new Thread(new Runnable() {
            @Override
            public void run() {
                String query = QueryPreferences.getStoredQuery(getBaseContext());
                String lastResultID = QueryPreferences.getLastResultID(getBaseContext());
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
                    Intent i = PhotoGalleryActivity.newIntent(getBaseContext());
                    PendingIntent pi = PendingIntent.getService(getBaseContext(), 0, i, 0);
                    Notification notification = new NotificationCompat.Builder(getBaseContext())
                            .setTicker(resources.getString(R.string.new_picture_title))
                            .setSmallIcon(android.R.drawable.ic_menu_report_image)
                            .setContentTitle(resources.getString(R.string.new_picture_title))
                            .setContentText(resources.getString(R.string.new_pictures_text))
                            .setContentIntent(pi)
                            .setAutoCancel(true)
                            .build();
                    //NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getBaseContext());
                    //notificationManagerCompat.notify(0, notification);
                    showBackgroundNotification(0, notification);

                    sendBroadcast(new Intent(PollService.ACTION_SHOW_NOTIFICATION), PollService.PERM_PRIVATE);
                }

                QueryPreferences.setLastResultID(getBaseContext(), resultID);

            }
        });
    }

    private void showBackgroundNotification(int requestCode, Notification notification) {
        Intent i = new Intent(PollService. ACTION_SHOW_NOTIFICATION);
        i.putExtra(PollService.REQUEST_CODE, requestCode);
        i.putExtra(PollService.NOTIFICATION, notification);
        sendOrderedBroadcast(i, PollService.PERM_PRIVATE, null, null, Activity.RESULT_OK, null, null);
    }


    public   static boolean isSchedulerExist(Context context) {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        List<JobInfo> jobInfos = jobScheduler.getAllPendingJobs();
        for (JobInfo info : jobInfos) {
            if (info.getId() == JOB_ID) return true;
        }
        return false;
    }


    public static void setJobScheduler(Context context, boolean isAlreadyOn){
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (!isAlreadyOn) {
            ComponentName componentName = new ComponentName(context, PollService_JobScheduler.class);
            JobInfo info = new JobInfo.Builder(JOB_ID, componentName)
                    .setRequiresCharging(true)
                    .setPersisted(true)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                    .setPeriodic(5 * 50 * 1000)
                    .build();
            int result = jobScheduler.schedule(info);
            if (result == JobScheduler.RESULT_SUCCESS) {
                Log.i(TAG, "Job scheduled");
            } else {
                Log.i(TAG, "Job scheduling error");
            }
        } else {
            jobScheduler.cancel(JOB_ID);
            Log.i(TAG, "Job schedule canceled");
        }

        QueryPreferences.setAlarmOn(context, !isAlreadyOn);
    }

}
