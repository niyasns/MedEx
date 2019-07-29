package com.niyas.android.medex;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;

public class BackgroundSoundServiceAPI26 extends JobIntentService {

    public static final int JOB_ID = 1;
    MediaPlayer player;

    public BackgroundSoundServiceAPI26() {
    }

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, BackgroundSoundService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        player = MediaPlayer.create(this, R.raw.background);
        player.setLooping(true);
        player.setVolume(100, 100);
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
