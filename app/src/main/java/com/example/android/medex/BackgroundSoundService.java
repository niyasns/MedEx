package com.example.android.medex;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

public class BackgroundSoundService extends Service {
    MediaPlayer player;
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
        /* Initializing media player for playing audio */
        player = MediaPlayer.create(this, R.raw.background);
        player.setLooping(true);
        player.setVolume(100, 100);
    }


    public int onStartCommand(Intent intent, int flags, int startId) {
        /* Starting audio with intent */
        Bundle bundle = intent.getExtras();
        quizName = bundle.getString("quiz_name");
        quizTimeout = bundle.get("quiz_timeout").toString();
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
                    .setContentText(quizTimeout + " seconds to join")
                    .setSmallIcon(R.drawable.logo_launch)
                    .setAutoCancel(true)
                    .build();
            Intent notifyIntent = new Intent();
            notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startForeground(NOTIFICATION_ID, notification);
        }

        player.start();
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        player.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true); //true will remove notification
        }
        player.stop();
        player.reset();
        player.release();
    }


}
