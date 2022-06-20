package com.ck.dev.punjabify.model;

public class DownloadData {

    private ServerizedTrackData trackData;
    private int                 progress;

    public DownloadData(ServerizedTrackData trackData, int progress) {
        this.trackData = trackData;
        this.progress = progress;
    }

    public ServerizedTrackData getTrackData() {
        return trackData;
    }

    public int getProgress() {
        return progress;
    }

    public void setTrackData(ServerizedTrackData trackData) {
        this.trackData = trackData;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}
