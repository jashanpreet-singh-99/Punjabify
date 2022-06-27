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
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ck.dev.punjabify.R;
import com.ck.dev.punjabify.threads.ThreadConfig;
import com.ck.dev.punjabify.threads.ThreadPoolManager;
import com.ck.dev.punjabify.threads.interfaces.OnServerizedDataFetchCompleted;
import com.ck.dev.punjabify.threads.tasks.ArtistDataFetcher;
import com.ck.dev.punjabify.utils.Config;
import com.ck.dev.punjabify.utils.FirebaseConfig;
import com.ck.dev.punjabify.utils.ServerizedManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class UpdateDatabaseService extends Service implements OnServerizedDataFetchCompleted {

    private DatabaseReference databaseReferenceStat;
    private DatabaseReference databaseReferenceTracks;

    private ThreadPoolManager threadPoolManager;
    private ServerizedManager serverizedManager;

    private Map<String, Integer> artistCounts = new HashMap<>();

    private long doneCount = 0;
    private long maxCount = 0;

    private final IBinder iBinder = new UpdateDatabaseService.LocalBinder();
    private Notification.Builder notification;
    private NotificationManager service;

    private OnServerizedDataFetchCompleted uiConnection;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        databaseReferenceStat   = FirebaseDatabase.getInstance().getReference(FirebaseConfig.KEY_STATIC);
        databaseReferenceTracks = FirebaseDatabase.getInstance().getReference("tracks");
        threadPoolManager       = ThreadPoolManager.getInstance();
        serverizedManager       = new ServerizedManager(getApplicationContext());
    }

    private String createNotificationChannel() {
        NotificationChannel Channel;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Channel = new NotificationChannel(Config.CHANNEL_ID, "Searching for new Tracks.", NotificationManager.IMPORTANCE_MIN);
            Channel.setLightColor( Color.BLUE);
            Channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            Objects.requireNonNull(service).createNotificationChannel(Channel);
            return Config.CHANNEL_ID;
        }
        return "None";
    }//createNotificationChannel

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notification = new Notification.Builder(getApplicationContext(), Config.CHANNEL_ID)
                    .setContentTitle("Searching for new Tracks.")
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setSmallIcon(R.drawable.ic_sync)
                    .setChannelId(createNotificationChannel())
                    .setOngoing(true)
                    .setProgress(0,0,true)
                    .setOngoing(false);
            startForeground(Config.NOTIFICATION_ID + 1, notification.build());
        }
        checkDatabaseForRefresh();
        return super.onStartCommand(intent, flags, startId);
    }

    private void checkDatabaseForRefresh() {
        artistCounts = serverizedManager.getArtistMap();
        doneCount = 0;
        if (artistCounts.size() == 0) {
            Config.LOG(Config.TAG_MEDIA_ONLINE, "Fetching whole Database.", false);
            databaseReferenceTracks.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            maxCount = dataSnapshot.getChildrenCount();
                            updateNotificationProgress();
                            for (DataSnapshot data: dataSnapshot.getChildren()) {
                                ArtistDataFetcher artistDataFetcher = new ArtistDataFetcher();
                                artistDataFetcher.setMetaData(data, serverizedManager, getCacheDir() + Config.TRACKS_DIR, UpdateDatabaseService.this);
                                threadPoolManager.addCallable(artistDataFetcher, ThreadConfig.SERVERIZED_DATA);
                            }
                            Config.LOG(Config.TAG_MEDIA_ONLINE, "Fetched track Data. Processing it now.", false);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError dError) {
                            Config.LOG(Config.TAG_MEDIA_ONLINE, "Unable to get Track Data : " + dError, true);
                        }
                    });
        } else {
            databaseReferenceStat.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    maxCount = snapshot.getChildrenCount();
                    updateNotificationProgress();
                    Config.LOG(Config.TAG_MEDIA_ONLINE, "Fetching new Data. " + (Looper.myLooper() == Looper.getMainLooper()), false);
                    for (DataSnapshot data: snapshot.getChildren()) {
                        String artist = data.getKey();
                        if (artist == null) { continue; }
                        artist = artist.replace("_", " ");
                        if (artistCounts.containsKey(artist)) {
                            int count = Objects.requireNonNull(data.getValue(Integer.class));
                            if (Objects.requireNonNull(artistCounts.get(artist)) == count) {
                                // If check passes
                                Config.LOG(Config.TAG_MEDIA_ONLINE, "Data : " + artist + " : " + count, false);
                                singleArtistCompleted(artist);
                                continue;
                            }
                        }
                        // Call thread to get new Data
                        ArtistDataFetcher artistDataFetcher = new ArtistDataFetcher();
                        artistDataFetcher.setMetaData(databaseReferenceTracks.child(artist), serverizedManager, getCacheDir() + Config.TRACKS_DIR, UpdateDatabaseService.this);
                        threadPoolManager.addCallable(artistDataFetcher, ThreadConfig.SERVERIZED_DATA);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Config.LOG(Config.TAG_MEDIA_ONLINE, "Error While getting User refresh Stat : " + error, true);
                }
            });
        }
    }

    // yet to test below android 8 for non foreground service initiation.
    private void updateNotificationProgress() {
        if (notification == null) {
            return;
        }
        notification.setProgress((int) maxCount, (int) doneCount, false)
                .setContentText(String.format(Locale.ENGLISH, "%d of %d Updated", doneCount, maxCount));
        service.notify(Config.NOTIFICATION_ID + 1, notification.build());
    }

    @Override
    public void onDataCompleted() {
        if (uiConnection != null) {
            uiConnection.onDataCompleted();
        }
    }

    @Override
    public void singleArtistCompleted(String artist) {
        doneCount += 1;
        updateNotificationProgress();
        if (uiConnection != null) {
            uiConnection.singleArtistCompleted(artist);
        }
        if (maxCount == doneCount) {
            Config.LOG(Config.TAG_MEDIA_ONLINE, "competed Data Fetching.", false);
            if (uiConnection != null) {
                uiConnection.onDataCompleted();
            }
            stopSelf();
        } else if (doneCount < maxCount) {
            Config.LOG(Config.TAG_MEDIA_ONLINE, "Fetching ... " + doneCount + "/" + maxCount, false);
        } else {
            Config.LOG(Config.TAG_MEDIA_ONLINE, "Error Artist count miss match.", true);
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiConnection = null;
        Config.LOG(Config.TAG_MEDIA_ONLINE, "Closing Update Database Service.", false);
    }

    public void setListener(OnServerizedDataFetchCompleted onServerizedDataFetchCompleted) {
        this.uiConnection = onServerizedDataFetchCompleted;
    }

    public class LocalBinder extends Binder {

        public UpdateDatabaseService getService() {
            return UpdateDatabaseService.this;
        }

    }

}
