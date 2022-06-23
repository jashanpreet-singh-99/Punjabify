package com.ck.dev.punjabify.threads.interfaces;

import com.ck.dev.punjabify.model.ServerizedTrackData;

public interface UiTrackDownloadingThreadCallBack {
    void progressUpdated(int progress);
    void trackDownloaded(int id);
    void updateMax(int max);
    void alreadyDownloaded();
    void startTrackDownload(ServerizedTrackData track);
}
