package com.example.android.medex;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class BackgroundSoundService extends Service {
    MediaPlayer player;
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
        player.stop();
        player.reset();
        player.release();
    }
}
