package com.ck.dev.punjabify.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.ck.dev.punjabify.R;
import com.ck.dev.punjabify.model.DownloadData;
import com.ck.dev.punjabify.model.ServerizedTrackData;
import com.ck.dev.punjabify.threads.interfaces.UiTrackDownloadingThreadCallBack;
import com.ck.dev.punjabify.utils.Config;
import com.ck.dev.punjabify.utils.ServerizedManager;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadManagerService extends Service implements UiTrackDownloadingThreadCallBack {

    private ServerizedManager serverizedManager;
    private ArrayList<DownloadData> downloadQueue = new ArrayList<>();

    private ExecutorService executorService;

    private final IBinder iBinder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        serverizedManager = new ServerizedManager(DownloadManagerService.this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Notification notification = new Notification.Builder(getApplicationContext(), Config.CHANNEL_ID)
                    .setContentTitle("Preparing Download Manager.")
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setSmallIcon(R.drawable.ic_download)
                    .setProgress(100, 0, true)
                    .setChannelId(createNotificationChannel())
                    .setOngoing(true)
                    .build();
                startForeground(Config.NOTIFICATION_ID, notification);
        }
        updateDownloadQueue();
        if (executorService == null) {
            executorService = Executors.newSingleThreadExecutor();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private String createNotificationChannel() {
        NotificationChannel chan;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            chan = new NotificationChannel(Config.CHANNEL_ID, "Preparing Download Manager ", NotificationManager.IMPORTANCE_MIN);
            chan.setLightColor( Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            Objects.requireNonNull(service).createNotificationChannel(chan);
            return Config.CHANNEL_ID;
        }
        return "None";
    }//createNotificationChannel

    public void updateDownloadQueue() {
        downloadQueue.clear();
        for (ServerizedTrackData trackData: serverizedManager.getDownloadQueue()) {
            downloadQueue.add(new DownloadData(trackData, 0));
        }
    }

    public ArrayList<DownloadData> getDownloadQueue() {
        updateDownloadQueue();
        return downloadQueue;
    }

    public void insertNewDownloads() {

    }

//    private void downloadFirstQueueTrack() {
//        TrackUrlDownloader trackUrlDownloader = new TrackUrlDownloader();
//        trackUrlDownloader.setMetaData(
//                this,
//                getCacheDir() + Config.TRACKS_DIR,
//                downloadQueue.get(0).getTrackData()
//        );
//        if (executorService == null) {
//            executorService = Executors.newSingleThreadExecutor();
//        }
//        executorService.submit(trackUrlDownloader);
//        Config.LOG(Config.TAG_DOWNLOAD, "Starting to Download track present in queue.", false);
//
//    }

    @Override
    public void progressUpdated(int progress) {

    }

    @Override
    public void trackDownloaded(int id) {

    }

    @Override
    public void updateMax(int max) {

    }

    @Override
    public void alreadyDownloaded() {

    }

    public class LocalBinder extends Binder {

        public DownloadManagerService getService() {
            return DownloadManagerService.this;
        }

    }
}
