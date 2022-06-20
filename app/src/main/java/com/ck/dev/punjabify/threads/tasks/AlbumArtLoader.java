package com.ck.dev.punjabify.threads.tasks;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.ck.dev.punjabify.threads.ui.ImageButtonLoader;
import com.ck.dev.punjabify.utils.Config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;

/**
 * link, artist, title, path, image button, mode
 */
public class AlbumArtLoader implements Runnable {

    private String artist;
    private String title;
    private String path;
    private int mode = 0;

    private WeakReference<ImageView> weakReference;
    private WeakReference<Activity> contextWeakReference;

    @Override
    public void run() {
        Bitmap bitmap = null;
        artist = artist.replace(" ", "_");
        title = title.replace(" ", "_");
        File storeLoc = new File(path + artist);
        if (!storeLoc.exists()) {
            if (!storeLoc.mkdirs()) {
                Config.LOG(Config.TAG_ART_CACHE, "ERROR in creating art dir", false);
            }
        }
        File img = new File(storeLoc.getAbsolutePath() + "/" + title + ".jpg");
        if (img.exists()) {
            try {
                bitmap = BitmapFactory.decodeStream(new FileInputStream(img));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Config.LOG(Config.TAG_ART_CACHE, "Local File not found " + e, true);
            }
        }
        if (contextWeakReference != null && contextWeakReference.get() != null){
            ImageButtonLoader imageButtonLoader = new ImageButtonLoader();
            imageButtonLoader.setMataData(bitmap, mode, weakReference);
            contextWeakReference.get().runOnUiThread(imageButtonLoader);
        }
    }

    public void setMetaData(Activity activity, ImageView imageBtn, String path, String artist, String title, int mode) {
        this.artist = artist;
        this.title = title;
        this.path = path;
        this.weakReference = new WeakReference<>(imageBtn);
        this.contextWeakReference = new WeakReference<>(activity);
        this.mode = mode;
    }
}
