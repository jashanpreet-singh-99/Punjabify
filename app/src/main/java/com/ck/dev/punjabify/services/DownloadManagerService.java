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
import com.ck.dev.punjabify.threads.tasks.TrackUrlDownloader;
import com.ck.dev.punjabify.utils.Config;
import com.ck.dev.punjabify.utils.ServerizedManager;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadManagerService extends Service implements UiTrackDownloadingThreadCallBack {

    private UiTrackDownloadingThreadCallBack uiTrackDownloadingThreadCallBack = null;
    private ServerizedManager serverizedManager;
    private final ArrayList<DownloadData> downloadQueue = new ArrayList<>();

    private ExecutorService executorService;

    private ServerizedTrackData currentDownloadingTrack;

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
        if (downloadQueue.size() > 0 ) {
            Config.LOG(Config.TAG_DOWNLOAD, "Has some files to download.", false);
            downloadFirstQueueTrack();
            return START_STICKY;
        }
        this.stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }

    private String createNotificationChannel() {
        NotificationChannel Channel;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Channel = new NotificationChannel(Config.CHANNEL_ID, "Preparing Download Manager ", NotificationManager.IMPORTANCE_MIN);
            Channel.setLightColor( Color.BLUE);
            Channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            Objects.requireNonNull(service).createNotificationChannel(Channel);
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

    public void clearDownloadQueue() {
        if (serverizedManager != null) {
            serverizedManager.clearDownloadQueue();
        }
    }

    public void downloadFirstQueueTrack() {
        TrackUrlDownloader trackUrlDownloader = new TrackUrlDownloader();
        trackUrlDownloader.setMetaData(
                this,
                getCacheDir() + Config.TRACKS_DIR,
                downloadQueue.get(0).getTrackData()
        );
        currentDownloadingTrack = downloadQueue.get(0).getTrackData();
        if (executorService == null) {
            executorService = Executors.newSingleThreadExecutor();
        }
        executorService.submit(trackUrlDownloader);
        Config.LOG(Config.TAG_DOWNLOAD, "Starting to Download track present in queue.", false);
    }

    @Override
    public void progressUpdated(int progress) {
        uiTrackDownloadingThreadCallBack.progressUpdated(progress);
    }

    @Override
    public void trackDownloaded(int id) {
        boolean work = serverizedManager.updateTrackToDownloaded(id);
        if (work) {
            serverizedManager.removeDownloadQueueTrack(id);
            downloadQueue.remove(0);
            uiTrackDownloadingThreadCallBack.trackDownloaded(id);
        }
        Config.LOG(Config.TAG_DOWNLOAD, "Track Downloaded To Local Dir " + work, false);
        if (downloadQueue.size() > 0) {
            downloadFirstQueueTrack();
        }
    }

    @Override
    public void updateMax(int max) {
        uiTrackDownloadingThreadCallBack.updateMax(max);
    }

    @Override
    public void alreadyDownloaded() {
        uiTrackDownloadingThreadCallBack.alreadyDownloaded();
    }

    @Override
    public void startTrackDownload(ServerizedTrackData track) {
        uiTrackDownloadingThreadCallBack.startTrackDownload(track);
    }

    public void setCallBack(UiTrackDownloadingThreadCallBack uiTrackDownloadingThreadCallBack){
        this.uiTrackDownloadingThreadCallBack = uiTrackDownloadingThreadCallBack;
    }

    public class LocalBinder extends Binder {

        public DownloadManagerService getService() {
            return DownloadManagerService.this;
        }

    }
}
