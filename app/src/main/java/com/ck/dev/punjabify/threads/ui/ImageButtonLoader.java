package com.ck.dev.punjabify.threads.ui;

import android.graphics.Bitmap;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.ck.dev.punjabify.utils.Config;

import java.lang.ref.WeakReference;

public class ImageButtonLoader implements Runnable{

    private Bitmap bitmap;
    private int mode = 0;
    private WeakReference<ImageView> imageButtonWeakReference;

    @Override
    public void run() {
        if (imageButtonWeakReference != null && imageButtonWeakReference.get() != null) {
            if (bitmap != null) {
                if (mode == 0) {
                    bitmap = Config.getRoundedBitmap(bitmap);
                } else if (mode == 2) {
                    bitmap = Config.getOvalBitmap(bitmap);
                }
                imageButtonWeakReference.get().setImageBitmap(bitmap);
            } else {
                imageButtonWeakReference.get().setImageBitmap(null);
                Config.LOG(Config.TAG_ART_CACHE, "No Image Found ", true);
            }
        }
    }

    public void setMataData(Bitmap bitmap, int mode, WeakReference<ImageView> imageButtonWeakReference) {
        this.bitmap = bitmap;
        this.mode = mode;
        this.imageButtonWeakReference = imageButtonWeakReference;
    }

}
