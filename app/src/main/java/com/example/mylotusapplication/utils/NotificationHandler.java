package com.example.mylotusapplication.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ListenableWorker;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.mylotusapplication.MainActivity;
import com.example.mylotusapplication.MainActivity2;
import com.example.mylotusapplication.R;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NotificationHandler extends Worker {
    public final String CHANNEL_ID = "default";
    int notificatioId = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);

    public NotificationHandler(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }


    public static void schedulerReminder(String when, String title, String desc, String tag, int what) {
        // what = 0 == create
        //what =1 == update
        //what =2 == cancel
        long duration  = getTime(when);
        Log.e("duration ","====> "+duration);
        Data reminderData = setInputData(title,desc);


        switch (what){
            case 0:

                OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(NotificationHandler.class)
                        .setInitialDelay(duration, TimeUnit.MINUTES)
                        .setConstraints(setConstraints())
                        .setInputData(reminderData)
                        .addTag(tag)
                        .build();
                WorkManager.getInstance().enqueue(oneTimeWorkRequest);


                break;

            case 1:

                WorkManager.getInstance().cancelAllWorkByTag (tag);


                OneTimeWorkRequest oneTimeWorkRequestUpdate = new OneTimeWorkRequest.Builder(NotificationHandler.class)
                        .setInitialDelay(duration, TimeUnit.MINUTES)
                        .setConstraints(setConstraints())
                        .setInputData(reminderData)
                        .addTag(tag)
                        .build();
                WorkManager.getInstance().enqueue(oneTimeWorkRequestUpdate);

                break;

            case 2:
                WorkManager.getInstance().cancelAllWorkByTag (tag);
                break;
        }


    }

    public static Data setInputData(String title, String desc){
        Data builder = new Data.Builder()
                .putString("title",title)
                .putString("desc",desc).build();
        return builder;
    }


    public void sendNotification(String title, String desc) {

        //intent to open our activity
        Intent intent = new Intent(getApplicationContext(), MainActivity2.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = title;
            String description = desc;
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getApplicationContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        //notifications
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentText(desc)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setVibrate(new long[]{1000,1000,1000,1000})
                .setLights(Color.RED, 3000, 3000)
                .setAutoCancel(true);

        //show notification
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
        notificationManagerCompat.notify(notificatioId, builder.build());


    }


    public static int getTime(String endTime) {
        Matcher m = Pattern.compile("(\\d{2}):(\\d{2})").matcher(endTime);
        Calendar cal = Calendar.getInstance();
        int nowHour = cal.get(Calendar.HOUR_OF_DAY);
        int nowMin = cal.get(Calendar.MINUTE);

        if (!m.matches())
            throw new IllegalArgumentException("Invalid time format: " + endTime);
        int endHour = Integer.parseInt(m.group(1));
        int endMin = Integer.parseInt(m.group(2));
        if (endHour >= 24 || endMin >= 60)
            throw new IllegalArgumentException("Invalid time format: " + endTime);
        int minutesLeft = endHour * 60 + endMin - (nowHour * 60 + nowMin);
        if (minutesLeft < 0)
            minutesLeft += 24 * 60; // Time passed, so time until 'end' tomorrow
        int hours = minutesLeft / 60;
        int minutes = minutesLeft - hours * 60;

        int time = ((hours * 60) + minutes);
        return time;

    }



    @NonNull
    @Override
    public ListenableWorker.Result doWork() {

        String title = getInputData().getString("title");
        String desc = getInputData().getString("desc");

        sendNotification(title,desc);
        return ListenableWorker.Result.success();
    }

    public static Constraints setConstraints() {

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        return constraints;

    }



}