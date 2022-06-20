package com.ck.dev.punjabify.threads.tasks;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.ck.dev.punjabify.threads.ThreadConfig;
import com.ck.dev.punjabify.threads.ThreadPoolManager;
import com.ck.dev.punjabify.threads.interfaces.UiArtistImageThreadCallBack;
import com.ck.dev.punjabify.utils.Config;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.lang.ref.WeakReference;

public class ArtistImageDownloader implements Runnable {

    private WeakReference<UiArtistImageThreadCallBack> uiArtistImageThreadCallBackWeakReference;

    private String artist;
    private String pathDir;
    private int type;

    private File storageLoc;

    @Override
    public void run() {
        checkLocalFiles();
        final File file = new File(storageLoc.getAbsolutePath() + "/" + artist + ".jpg");
        if (file.exists()) {
            return;
        }
        FirebaseStorage.getInstance().getReference("artist").child(artist + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                ThreadPoolManager threadPoolManager = ThreadPoolManager.getInstance();
                UrlConnectionTask urlConnectionTask = new UrlConnectionTask();
                urlConnectionTask.setMetaData(
                        uiArtistImageThreadCallBackWeakReference,
                        artist,
                        uri.toString(),
                        file.getAbsolutePath(),
                        type
                );
                threadPoolManager.addCallable(urlConnectionTask, ThreadConfig.URL_CONNECTIONS);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    public void setMetaData(UiArtistImageThreadCallBack uiArtistImageThreadCallBack, String artist, String pathDir, int type) {
        this.uiArtistImageThreadCallBackWeakReference = new WeakReference<>(uiArtistImageThreadCallBack);
        this.artist = artist;
        this.pathDir = pathDir;
        this.type = type;
    }

    private void checkLocalFiles() {
        storageLoc = new File(pathDir);
        if (!storageLoc.exists()) {
            if (!storageLoc.mkdirs()) {
                Config.LOG(Config.TAG_MEDIA_ONLINE, "ERROR in creating img dir", false);
            }
        }
    }

}
