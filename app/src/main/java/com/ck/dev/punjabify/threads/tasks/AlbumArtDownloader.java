package com.ck.dev.punjabify.threads.tasks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;

import com.ck.dev.punjabify.threads.interfaces.UiAlbumArtThreadCallBack;
import com.ck.dev.punjabify.utils.Config;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;

public class AlbumArtDownloader implements Runnable {

    private WeakReference<UiAlbumArtThreadCallBack> uiThreadCallBackWeakReference;

    private String link;
    private String path;
    private int    pos;

    @Override
    public void run() {
        File img = new File(path);
        if (img.exists()) {
            Config.LOG(Config.TAG_THREAD, "Image available ", false);
            return;
        }
        Config.LOG(Config.TAG_THREAD, "Image Not Found Downloading " + path, true);
        MediaMetadataRetriever mmr;
        try {
            mmr = new MediaMetadataRetriever();
            mmr.setDataSource(link, new HashMap<String, String>());
            byte[] data = mmr.getEmbeddedPicture();
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            FileOutputStream fileOutputStream;
            fileOutputStream = new FileOutputStream(img);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            mmr.close();
            Config.LOG(Config.TAG_THREAD, " Downloading Complete " + path, false);
            if (uiThreadCallBackWeakReference != null && uiThreadCallBackWeakReference.get() != null) {
                uiThreadCallBackWeakReference.get().returnDownloadedPosition(pos);
            }
        } catch (Exception e) {
            Config.LOG(Config.TAG_ART_CACHE, "Image Downloading from Server Error " + e, true);
            if (uiThreadCallBackWeakReference != null && uiThreadCallBackWeakReference.get() != null) {
                uiThreadCallBackWeakReference.get().returnDownloadedPosition(-1);
            }
        }
    }

    public void setMetaData(UiAlbumArtThreadCallBack uiAlbumArtThreadCallBack, String link, String path, int pos) {
        this.uiThreadCallBackWeakReference = new WeakReference<>(uiAlbumArtThreadCallBack);
        this.link = link;
        this.path = path;
        this.pos  = pos;
    }

}
