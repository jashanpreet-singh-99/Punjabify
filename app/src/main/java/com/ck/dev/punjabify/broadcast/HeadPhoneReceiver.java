package com.ck.dev.punjabify.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

import com.ck.dev.punjabify.services.MediaPlayerService;

import java.util.Objects;

public class HeadPhoneReceiver extends BroadcastReceiver {

    private final MediaPlayerService mediaPlayerService;

    public HeadPhoneReceiver(MediaPlayerService mediaPlayerService) {
        this.mediaPlayerService = mediaPlayerService;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.requireNonNull(intent.getAction()).equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
            mediaPlayerService.pauseTrack();
        }
    }
}
