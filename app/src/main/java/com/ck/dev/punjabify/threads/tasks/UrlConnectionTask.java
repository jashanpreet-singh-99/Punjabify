package com.ck.dev.punjabify.threads.tasks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.ck.dev.punjabify.threads.interfaces.UiArtistImageThreadCallBack;
import com.ck.dev.punjabify.utils.ServerizedConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;

public class UrlConnectionTask implements Runnable {

    private WeakReference<UiArtistImageThreadCallBack> uiArtistImageThreadCallBackWeakReference;

    private String artist;
    private String uri;
    private String path;
    private int type;

    @Override
    public void run() {
        try {
            URL url = new URL(uri);
            URLConnection connection = url.openConnection();
            connection.connect();
            InputStream inputStream = connection.getInputStream();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            Bitmap img = BitmapFactory.decodeStream(inputStream, null, options);
            FileOutputStream fileOutputStream = new FileOutputStream(new File(path));
            assert img != null;
            img.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            inputStream.close();
            if (uiArtistImageThreadCallBackWeakReference != null && uiArtistImageThreadCallBackWeakReference.get() != null) {
                switch (type) {
                    case ServerizedConfig.ARTIST_MODE_FOLLOWED :
                        uiArtistImageThreadCallBackWeakReference.get().returnDownloadedFollowedArtist(artist);
                        break;
                    case ServerizedConfig.ARTIST_MODE_UN_FOLLOWED:
                        uiArtistImageThreadCallBackWeakReference.get().returnDownloadedUnFollowedArtist(artist);
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setMetaData(WeakReference<UiArtistImageThreadCallBack> uiArtistImageThreadCallBack, String artist, String uri, String path, int type) {
        this.uiArtistImageThreadCallBackWeakReference = uiArtistImageThreadCallBack;
        this.artist = artist;
        this.uri = uri;
        this.path = path;
        this.type = type;
    }

}
