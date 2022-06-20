package com.ck.dev.punjabify.interfaces;

import com.ck.dev.punjabify.model.ServerizedTrackData;

public interface OnServiceBindEvent {
    void trackCompletedOnline(ServerizedTrackData serverizedTrackData);
    void trackStateChangedPaused(boolean state);
    void seekMaxUpdated(int max_duration);
}
