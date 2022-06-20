package com.ck.dev.punjabify.threads.interfaces;

public interface UiTrackDownloadingThreadCallBack {
    void progressUpdated(int progress);
    void trackDownloaded(int id);
    void updateMax(int max);
    void alreadyDownloaded();
}
