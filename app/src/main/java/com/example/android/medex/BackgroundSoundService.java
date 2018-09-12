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
        player = MediaPlayer.create(this, R.raw.background);
        player.setLooping(true);
        player.setVolume(100, 100);
    }


    public int onStartCommand(Intent intent, int flags, int startId) {

        player.start();
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        player.start();
    }

    public IBinder onUnBind(Intent arg0) {
        // TODO Auto-generated method stub

        return null;
    }

    public void onStop() {

    }
    public void onPause() {

    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        player.stop();
        player.reset();
        player.release();
    }

    @Override
    public void onLowMemory() {

    }
}
