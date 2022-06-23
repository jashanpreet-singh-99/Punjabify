package com.ck.dev.punjabify.threads.tasks;

import com.ck.dev.punjabify.model.ServerizedTrackData;
import com.ck.dev.punjabify.threads.interfaces.UiTrackDownloadingThreadCallBack;
import com.ck.dev.punjabify.utils.Config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;

/**
 * -> Callback, link
 */
public class TrackUrlDownloader implements Runnable {

    private WeakReference<UiTrackDownloadingThreadCallBack> uiTrackDownloadingThreadCallBackWeakReference;
    private String path;
    private ServerizedTrackData trackData;

    private int downloaded = 0;

    @Override
    public void run() {
        try {
            if (trackData.getLink().equals(Config.DOWNLOADED_TRACK)) {
                if (uiTrackDownloadingThreadCallBackWeakReference.get() != null) {
                    Config.LOG(Config.TAG_DOWNLOAD, "Already Downloaded. Checked Link", true);
                    uiTrackDownloadingThreadCallBackWeakReference.get().alreadyDownloaded();
                    return;
                }
            }
            checkDir();
            File track = new File(path + trackData.getArtist().replace(" ", "_") + "/" + trackData.getTitle().replace(" ", "_") + ".mp3");
            //Config.LOG(Config.TAG_DOWNLOAD, "Download Path :" + path + trackData.getArtist().replace(" ", "_") + "/" + trackData.getTitle().replace(" ", "_") + ".mp3", true);
            URL url = new URL(trackData.getLink());
            URLConnection connection = url.openConnection();
            connection.connect();
            InputStream inputStream = connection.getInputStream();
            int total = connection.getContentLength();

            if (track.exists()) {
                // check If is downloaded Completely
                if (track.length() == total) {
                    Config.LOG(Config.TAG_DOWNLOAD, "Already Downloaded. ", true);
                    if (uiTrackDownloadingThreadCallBackWeakReference.get() != null) {
                        uiTrackDownloadingThreadCallBackWeakReference.get().alreadyDownloaded();
                    }
                    return;
                }
            }
            if (uiTrackDownloadingThreadCallBackWeakReference.get() != null) {
                uiTrackDownloadingThreadCallBackWeakReference.get().startTrackDownload(trackData);
                uiTrackDownloadingThreadCallBackWeakReference.get().updateMax(total);
            }
            FileOutputStream fileOutputStream = new FileOutputStream(track);
            byte[] buffer = new byte[1024];
            int bufferLength;
            while ((bufferLength = inputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, bufferLength);
                downloaded += bufferLength;
                if (uiTrackDownloadingThreadCallBackWeakReference.get() != null) {
                    uiTrackDownloadingThreadCallBackWeakReference.get().progressUpdated(downloaded);
                }
            }
            fileOutputStream.close();
            inputStream.close();
            if (uiTrackDownloadingThreadCallBackWeakReference.get() != null) {
                uiTrackDownloadingThreadCallBackWeakReference.get().trackDownloaded(trackData.getIndex());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkDir() {
        File storeLoc = new File(path + trackData.getArtist().replace(" ", "_"));
        if (!storeLoc.exists()) {
            if (!storeLoc.mkdirs()) {
                Config.LOG(Config.TAG_DOWNLOAD, "Unable to create Dir For Tracks ", true);
            }
        }
    }

    public void setMetaData(UiTrackDownloadingThreadCallBack uiTrackDownloadingThreadCallBack, String path, ServerizedTrackData trackData) {
        this.uiTrackDownloadingThreadCallBackWeakReference = new WeakReference<>(uiTrackDownloadingThreadCallBack);
        this.path = path;
        this.trackData = trackData;
    }

}
