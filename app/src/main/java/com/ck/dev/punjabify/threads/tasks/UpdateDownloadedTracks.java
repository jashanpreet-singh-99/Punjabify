package com.ck.dev.punjabify.threads.tasks;

import com.ck.dev.punjabify.utils.ServerizedManager;

import java.io.File;
import java.lang.ref.WeakReference;

public class UpdateDownloadedTracks implements Runnable {

    private String path;
    private String artist;
    private String title;
    private int id;
    private WeakReference<ServerizedManager> weakReference;

    @Override
    public void run() {
        File track = new File(path + artist.replace(" ", "_") + "/" + title.replace(" ", "_") + ".mp3");
        if (track.exists()) {
            if (weakReference != null && weakReference.get() != null) {
                weakReference.get().updateTrackToDownloaded(id);
            }
        }
    }

    public void setMetaData(String path, String artist, String title, int id, ServerizedManager serverizedManager) {
        this.path = path;
        this.artist = artist;
        this.title = title;
        this.id = id;
        this.weakReference = new WeakReference<>(serverizedManager);
    }
}
