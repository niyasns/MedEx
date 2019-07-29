package com.niyas.android.medex;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class BackgroundSoundService extends Service {
    private String quizName;
    private String quizTimeout;
    private int NOTIFICATION_ID = 1;
    private String CHANNEL_ID = "1";
    private String CHANNEL_NAME = "PRATITI";
    private int IMPORTANCE = NotificationManager.IMPORTANCE_HIGH;
    private Notification notification;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }


    public int onStartCommand(Intent intent, int flags, int startId) {
        /* Starting audio with intent */
        Bundle bundle = intent.getExtras();
        quizName = bundle.getString("quiz_name");
        Integer temp = bundle.getInt("quiz_timeout") + 10;
        quizTimeout = temp.toString();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, IMPORTANCE);
            notificationChannel.setDescription("PRATITI quiz started");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
            notification = new Notification.Builder(getBaseContext(), CHANNEL_ID)
                    .setContentTitle(quizName + " started")
                    .setContentText("Click to join")
                    .setSmallIcon(R.drawable.logo_launch)
                    .setAutoCancel(true)
                    .build();
            Intent notifyIntent = new Intent(getBaseContext(), HomeActivity.class);
            notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            notifyIntent.setAction(Intent.ACTION_MAIN);
            notifyIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            notification.contentIntent = PendingIntent.getActivity(getBaseContext(), 0, notifyIntent, 0);
            startForeground(NOTIFICATION_ID, notification);
        }

        /* player.start();*/
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true); //true will remove notification
        }
        /*player.stop();
        player.reset();
        player.release();*/
    }
}
