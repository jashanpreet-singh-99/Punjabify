package com.ck.dev.punjabify.tasks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.ck.dev.punjabify.utils.Config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class LoadLocalImage extends AsyncTask<String,String, Bitmap> {

    @SuppressLint("StaticFieldLeak")
    private Context context;
    @SuppressLint("StaticFieldLeak")
    private ImageButton imageButton;
    @SuppressLint("StaticFieldLeak")
    private ProgressBar loadingBar;

    private int rounded;

    public LoadLocalImage(Context context, ImageButton imageButton, ProgressBar loadingBar, int rounded) {
        this.context = context;
        this.imageButton = imageButton;
        this.loadingBar = loadingBar;
        this.rounded = rounded;
    }

    public LoadLocalImage(Context context, ImageButton imageButton, ProgressBar loadingBar) {
        this.context = context;
        this.imageButton = imageButton;
        this.loadingBar = loadingBar;
        this.rounded = 1;
    }

    @Override
    protected Bitmap doInBackground(String... strings) {
        String artist = strings[0].replace(" ", "_");
        Bitmap b;
        try {
            File f = new File(context.getCacheDir() + Config.IMG_DIR + artist + ".jpg");
            b = BitmapFactory.decodeStream(new FileInputStream(f));
            Config.LOG(Config.TAG_LOCAL_FILE, "Image Loaded", false);
        } catch (FileNotFoundException e) {
            Config.LOG(Config.TAG_LOCAL_FILE, "Unable to Fetch " + e, true);
            b = null;
        }
        return b;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        if (bitmap != null) {
            switch (rounded) {
                case 1:
                    bitmap = Config.getOvalBitmap(bitmap);
                    break;
                case 2:
                    bitmap = Config.getRoundedBitmap(bitmap);
                    break;
                default:
                    break;
            }
            loadingBar.setVisibility(View.GONE);
            imageButton.setImageBitmap(bitmap);
        } else {
//            loadingBar.setVisibility(View.GONE);
//            imageButton.setImageResource(R.drawable.ic_unknown);
        }
    }

}
