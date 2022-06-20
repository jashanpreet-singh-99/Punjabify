package com.ck.dev.punjabify.interfaces;

import com.ck.dev.punjabify.model.ServerizedTrackData;

public interface MusicControlConnection {

    long getCurrentSeek();
    long getMaxSeek();
    void setCurrentSeek(int seek);
    void pauseTrack();
    boolean isPlaying();
    void playTrack();
    void nextTrack();
    void prevTrack();
    void hideController();
    Boolean checkIfNull();
    void openSpecificTrackFragment(int mode, String value);
    void downloadTrack(ServerizedTrackData track);
}
