package com.ck.dev.punjabify.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import com.ck.dev.punjabify.R;
import com.ck.dev.punjabify.adapter.HomeScreenAdapter;
import com.ck.dev.punjabify.fragments.home.MediaControlUI;
import com.ck.dev.punjabify.fragments.home.ArtistFollowFragment;
import com.ck.dev.punjabify.fragments.home.DownloadFragment;
import com.ck.dev.punjabify.fragments.home.ServerizedSongsFragment;
import com.ck.dev.punjabify.fragments.home.SpecificTrackFragment;
import com.ck.dev.punjabify.interfaces.HomeToDownloadFragment;
import com.ck.dev.punjabify.interfaces.HomeToOnlineFragment;
import com.ck.dev.punjabify.interfaces.MusicControlConnection;
import com.ck.dev.punjabify.interfaces.OnServiceBindEvent;
import com.ck.dev.punjabify.interfaces.ViewPagerBackPressed;
import com.ck.dev.punjabify.model.ServerizedTrackData;
import com.ck.dev.punjabify.observers.HomeObservableObject;
import com.ck.dev.punjabify.services.MediaPlayerService;
import com.ck.dev.punjabify.services.UpdateDatabaseService;
import com.ck.dev.punjabify.threads.ThreadConfig;
import com.ck.dev.punjabify.threads.ThreadPoolManager;
import com.ck.dev.punjabify.threads.interfaces.OnServerizedDataFetchCompleted;
import com.ck.dev.punjabify.threads.tasks.AlbumArtLoader;
import com.ck.dev.punjabify.utils.Config;
import com.ck.dev.punjabify.utils.FirebaseConfig;
import com.ck.dev.punjabify.utils.MediaCallBackConfig;
import com.ck.dev.punjabify.utils.PreferenceConfig;
import com.ck.dev.punjabify.utils.PreferenceManager;
import com.ck.dev.punjabify.utils.ServerizedManager;
import com.ck.dev.punjabify.view.NotificationDialogs;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;

/**
 * LifeCycle -|
 *            |
 *           |_|
 *    Create Views
 *            |
 *    Start UpdateDatabaseService to Fetch Data from server
 *            |
 */

public class HomeScreen extends FragmentActivity implements MusicControlConnection,
        OnServiceBindEvent, HomeToOnlineFragment, Observer, HomeToDownloadFragment,
        OnServerizedDataFetchCompleted {

    private ViewPager2   homeViewPager;

    private ImageButton albumArt;
    private ImageButton pausePlayBtn;
    private TextView    trackTitleTxt;
    private TextView    trackArtistTxt;

    private ImageButton downloadFragmentBtn;

    private RelativeLayout navBarMusic;

    private HomeScreenAdapter  homeScreenAdapter;

    private MediaPlayerService mediaPlayerService;
    private boolean            serviceBounded;

    private ServerizedTrackData currentOnlineTrack;

    private FragmentManager       fragmentManager;
    private MediaControlUI        mediaControlUI;
    private SpecificTrackFragment specificTrackFragment;
    private DownloadFragment      downloadFragment;

    private ArrayList<Integer> trackQueueServerized = new ArrayList<>();

    private ServerizedManager serverizedManager;
    private ThreadPoolManager threadPoolManager;
    private DatabaseReference databaseReference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_home_screen);
        Config.LOG(Config.TAG_HOME, "Home Screen Activity started", false);
        handleIncomingActions();
        HomeObservableObject.getInstance().addObserver(this);
        threadPoolManager = ThreadPoolManager.getInstance();

        fetchServerizedData();
        iniView();
    }

    private void handleIncomingActions() {
        String action = getIntent().getAction();
        if (action == null) {
            return;
        }
        if (action.equalsIgnoreCase(MediaCallBackConfig.ACTION_OPEN_CONTROLLER)) {
            startMediaService();
        }
    }

    private void iniView() {
        homeViewPager  = this.findViewById(R.id.home_page_manager);
        pausePlayBtn   = this.findViewById(R.id.pause_play_btn);
        albumArt       = this.findViewById(R.id.song_album_art);
        trackTitleTxt  = this.findViewById(R.id.song_title_txt);
        trackArtistTxt = this.findViewById(R.id.song_artist_txt);
        navBarMusic    = this.findViewById(R.id.music_nav_bar);

        downloadFragmentBtn = this.findViewById(R.id.download_fragment_btn);

        homeScreenAdapter = new HomeScreenAdapter(this);
        homeViewPager.setAdapter(homeScreenAdapter);

        fragmentManager = getSupportFragmentManager();
        mediaControlUI  = (MediaControlUI) fragmentManager.findFragmentById(R.id.music_controller_fragment);
        hideShowFragment(mediaControlUI, false);
        mediaControlUI.newTrack(currentOnlineTrack);

        specificTrackFragment = (SpecificTrackFragment) fragmentManager.findFragmentById(R.id.specific_track_fragment);
        hideShowFragment(specificTrackFragment, false);

        downloadFragment = (DownloadFragment) fragmentManager.findFragmentById(R.id.download_track_fragment);
        hideShowFragment(downloadFragment, false);

        serverizedManager = new ServerizedManager(getApplicationContext());
        trackQueueServerized = serverizedManager.getQueue();

        checkPreviousTrackData();

        onClick();
    }

    /**
     * Check for any previous track Queue if exist
     */
    private void checkPreviousTrackData() {
        try {
            if (trackQueueServerized != null && trackQueueServerized.size() > 0) {
                int currentTrackIndex = PreferenceManager.getInt(getApplicationContext(), PreferenceConfig.KEY_CURRENT_TRACK_INDEX_ONLINE);
                currentTrackIndex = trackQueueServerized.get(currentTrackIndex);
                currentOnlineTrack = serverizedManager.getIdSpecificTrack(currentTrackIndex);
                mediaControlUI.newTrack(currentOnlineTrack);
                navBarMusic.setVisibility(View.VISIBLE);
                trackTitleTxt.setText(currentOnlineTrack.getTitle());
                trackArtistTxt.setText(currentOnlineTrack.getArtist());

                AlbumArtLoader albumArtLoader = new AlbumArtLoader();
                albumArtLoader.setMetaData(
                        HomeScreen.this,
                        albumArt,
                        getCacheDir() + Config.ART_DIR,
                        currentOnlineTrack.getArtist(),
                        currentOnlineTrack.getTitle(),
                        0
                );
                threadPoolManager.addCallable(albumArtLoader, ThreadConfig.IMAGE_LOAD);
            }
        } catch (Exception e) {
            Config.LOG(Config.TAG_HOME, "Error previous track fetch", true);
        }
        if (PreferenceManager.getBoolean(getApplicationContext(), PreferenceConfig.KEY_SERVICE_RUNNING)) {
            Config.LOG(Config.TAG_MEDIA_ONLINE, "Already Running", false);
            startMediaService();
        }
        if (mediaPlayerService == null) {
            pausePlayBtn.setImageResource(R.drawable.ic_play);
        } else {
            pausePlayBtn.setImageResource(R.drawable.ic_pause);
        }
    }

    /**
     * Used to start Media Service if not Running.
     */
    private void startMediaService() {
        Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    /**
     * Used to hide Fragment
     * @param state hide or not
     */
    private void hideShowFragment(Fragment fragment,boolean state) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_up, R.anim.slide_down);
        if (state) {
            fragmentTransaction.show(fragment);
        } else {
            fragmentTransaction.hide(fragment);
        }
        fragmentTransaction.commit();
    }

    private void onClick() {
        pausePlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayerService == null ) {
                    startMediaService();
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            if  (serviceBounded) {
                                if (isPlaying()) {
                                    pauseTrack();
                                } else {
                                    playTrack();
                                }
                                return;
                            }
                            new Handler().postDelayed(this, 50);
                        }
                    });
                } else {
                    if (isPlaying()) {
                        pauseTrack();
                    } else {
                        playTrack();
                    }
                }
            }
        });

        navBarMusic.setOnClickListener(v -> hideShowFragment(mediaControlUI, true));

        downloadFragmentBtn.setOnClickListener(v -> {
            Config.LOG(Config.TAG_HOME, "show download fragment", false);
            hideShowFragment(downloadFragment, true);
            downloadFragmentBtn.setVisibility(View.GONE);
        });

    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Config.LOG(Config.TAG_MEDIA, "Service Connected to UI.", false);
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            mediaPlayerService = binder.getService();
            serviceBounded = true;
            mediaPlayerService.setOnServiceBindEvent(HomeScreen.this);
            if (mediaPlayerService.getCurrentOnlineTrack() == null) {
                mediaPlayerService.sendTrackToPlay(currentOnlineTrack);
            }
            updateUI();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Config.LOG(Config.TAG_MEDIA, "Service Disconnected from UI.", true);
            serviceBounded = false;
        }
    };

    private void updateUI() {
        if (mediaPlayerService.getCurrentOnlineTrack() != null) {
            if (currentOnlineTrack == null) {
                currentOnlineTrack = mediaPlayerService.getCurrentOnlineTrack();
            }
            mediaControlUI.newTrack(currentOnlineTrack);
            navBarMusic.setVisibility(View.VISIBLE);
            trackTitleTxt.setText(currentOnlineTrack.getTitle());
            trackArtistTxt.setText(currentOnlineTrack.getArtist());
            AlbumArtLoader albumArtLoader = new AlbumArtLoader();
            albumArtLoader.setMetaData(
                    HomeScreen.this,
                    albumArt,
                    getCacheDir() + Config.ART_DIR,
                    currentOnlineTrack.getArtist(),
                    currentOnlineTrack.getTitle(),
                    0
            );
            threadPoolManager.addCallable(albumArtLoader, ThreadConfig.IMAGE_LOAD);
            if (isPlaying()) {
                pausePlayBtn.setImageResource(R.drawable.ic_pause);
            } else {
                pausePlayBtn.setImageResource(R.drawable.ic_play);
            }
        } else {
            navBarMusic.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = homeScreenAdapter.getCurFragment(homeViewPager.getCurrentItem());
        if (fragment instanceof ViewPagerBackPressed) {
            ((ViewPagerBackPressed) fragment).onBackPressed();
        }
        if (mediaControlUI.isVisible()) {
            hideShowFragment(mediaControlUI, false);
        }else if (specificTrackFragment.isVisible()) {
            hideShowFragment(specificTrackFragment, false);
        } else if (downloadFragment.isVisible()) {
            hideShowFragment(downloadFragment, false);
            downloadFragmentBtn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayerService != null) {
            updateUI();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBounded) {
            unbindService(serviceConnection);
            mediaPlayerService.setOnServiceBindEvent(null);
            if (!mediaPlayerService.isPlaying()) {
                mediaPlayerService.stopSelf();
            }
        }
    }

    @Override
    public long getCurrentSeek() {
        return mediaPlayerService.getCurrentTrackSeek();
    }

    @Override
    public long getMaxSeek() {
        return mediaPlayerService.getTrackDuration();
    }

    @Override
    public void setCurrentSeek(int seek) {
        mediaPlayerService.setCurrentSeek(seek);
    }

    @Override
    public void pauseTrack() {
        mediaPlayerService.pauseTrack();
        pausePlayBtn.setImageResource(R.drawable.ic_play);
    }

    @Override
    public void playTrack() {
        if (mediaPlayerService == null) {
            startMediaService();
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    if  (serviceBounded) {
                        mediaPlayerService.playTrack();
                        pausePlayBtn.setImageResource(R.drawable.ic_pause);
                        return;
                    }
                    new Handler().postDelayed(this, 50);
                }
            });
        } else {
            mediaPlayerService.playTrack();
            pausePlayBtn.setImageResource(R.drawable.ic_pause);
        }
    }

    @Override
    public boolean isPlaying() {
        if (mediaPlayerService == null) {
            return false;
        }
        return mediaPlayerService.isPlaying();
    }

    @Override
    public void nextTrack() {
        if (mediaPlayerService == null) {
            startMediaService();
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    if  (serviceBounded) {
                        nextTrack();
                        return;
                    }
                    new Handler().postDelayed(this, 50);
                }
            });
        } else {
            if (trackQueueServerized != null) {
                mediaPlayerService.nextTrack();
            }
        }
    }

    @Override
    public void prevTrack() {
        if (mediaPlayerService == null) {
            startMediaService();
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    if  (serviceBounded) {
                        prevTrack();
                        return;
                    }
                    new Handler().postDelayed(this, 50);
                }
            });
        } else {
            if (trackQueueServerized != null) {
                mediaPlayerService.prevTrack();
            }
        }
    }

    @Override
    public void hideController() {
        hideShowFragment(mediaControlUI,false);
    }

    @Override
    public Boolean checkIfNull() {
        return (mediaPlayerService == null);
    }


    @Override
    public void trackCompletedOnline(ServerizedTrackData serverizedTrackData) {
        currentOnlineTrack = serverizedTrackData;
        updateUI();
    }

    @Override
    public void trackStateChangedPaused(boolean state) {
        if (state) {
            pausePlayBtn.setImageResource(R.drawable.ic_play);
        } else {
            pausePlayBtn.setImageResource(R.drawable.ic_pause);
        }
    }

    @Override
    public void seekMaxUpdated(int max_duration) {
        if (mediaControlUI != null) {
            mediaControlUI.updateMaxSeek(max_duration);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            mediaControlUI.updateVolumeBar();
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            mediaControlUI.updateVolumeBar();
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
            mediaControlUI.updateVolumeBar();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void playOnlineTrack(ServerizedTrackData serverizedTrackData) {
        currentOnlineTrack = serverizedTrackData;
        final ServerizedTrackData onlineTrackData1 = serverizedTrackData;
        if (mediaPlayerService == null) {
            startMediaService();
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    if  (serviceBounded) {
                        playOnlineTrack(onlineTrackData1);
                        return;
                    }
                    new Handler().postDelayed(this, 50);
                }
            });
        } else {
            mediaPlayerService.sendTrackToPlay(serverizedTrackData);
            mediaPlayerService.playTrack();
            updateUI();
        }
    }

    @Override
    public void changeViewpagerTouchInterceptor(Boolean state) {
        homeViewPager.setUserInputEnabled(state);
    }

    @Override
    public void openSpecificTrackFragment(int mode, String value) {
        hideShowFragment(specificTrackFragment, true);
        specificTrackFragment.updateSpecificTrackData(mode, value);
    }

    @Override
    public void downloadTrack(ServerizedTrackData track) {
        if (track.getDownloaded() == 1) {
            Config.LOG(Config.TAG_DOWNLOAD, "Already Downloaded. So skipping this track.", false);
            return;
        }
        if (serverizedManager.insertDownloadQueueTrack(track.getIndex())) {
            downloadFragment.startDownloadManager();
        } else {
            Config.LOG(Config.TAG_DOWNLOAD, "Track not inserted into the Queue", false);
        }
    }

    @Override
    public void updateOnlineQueue() {
        if (mediaPlayerService == null) {
            startMediaService();
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    if  (serviceBounded) {
                        updateOnlineQueue();
                        return;
                    }
                    new Handler().postDelayed(this, 50);
                }
            });
        } else {
                mediaPlayerService.onServerizedQueueUpdated();
        }
    }

    @Override
    public void hideSpecificTrackFragment() {
        hideShowFragment(specificTrackFragment, false);
    }

    @Override
    public void updatedArtistFollowed() {
        ServerizedSongsFragment serverizedSongsFragment = (ServerizedSongsFragment) homeScreenAdapter.getCurFragment(HomeScreenAdapter.SERVERIZED_TRACK_FRAGMENT);
        serverizedSongsFragment.updateArtistFollowed();
    }

    public void onArtistDataUpdated() {
        ArtistFollowFragment artistFollowFragment = (ArtistFollowFragment) homeScreenAdapter.getCurFragment(HomeScreenAdapter.ARTIST_FOLLOW_FRAGMENT);
        artistFollowFragment.refreshedDBValues();
    }

    @Override
    public void update(Observable o, Object arg) {
        Config.LOG(Config.TAG_INTERNET, " Network Changed " + arg, false);
        if (!(boolean)arg) {
            NotificationDialogs notificationDialogs = new NotificationDialogs(
                    this,
                    android.R.style.Theme_Holo_Light,
                    "Network Error",
                    "Disconnected from internet. You might have some network problems, check your internet connection or Switch to offline mode.",
                    "Go Offline",
                    "Retry",
                    true,
                    true
                    );

            Objects.requireNonNull(notificationDialogs.getWindow()).setBackgroundDrawableResource(R.color.colorTransparent50);
            notificationDialogs.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            notificationDialogs.show();
        }
    }

    private final ServiceConnection updateServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Config.LOG(Config.TAG_MEDIA_ONLINE, "Service Connected with Update Track Server.", false);
            UpdateDatabaseService.LocalBinder binder = (UpdateDatabaseService.LocalBinder) service;
            UpdateDatabaseService updateDatabaseService = binder.getService();
            updateDatabaseService.setListener(HomeScreen.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Config.LOG(Config.TAG_MEDIA, "Update Service Disconnected from UI.", true);
        }
    };

    private void fetchServerizedData() {
        Intent intent = new Intent(getApplicationContext(), UpdateDatabaseService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        //bindService(intent, , BIND_AUTO_CREATE);
//        databaseReference = FirebaseDatabase.getInstance().getReference();
//        databaseReference.child("user").child(PreferenceManager.getString(getApplicationContext(), FirebaseConfig.USER_ID)).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (Objects.requireNonNull(snapshot.getValue(String.class)).equals("yes")) {
//                    databaseReference.child("tracks").addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                            Config.LOG(Config.TAG_MEDIA_ONLINE, "Online Data Fetch ", false);
//                            ServerizedDataFetcher serverizedDataFetcher = new ServerizedDataFetcher();
//                            serverizedDataFetcher.setMetaData(dataSnapshot, serverizedManager, getCacheDir() + Config.TRACKS_DIR, HomeScreen.this);
//                            threadPoolManager.addCallable(serverizedDataFetcher, ThreadConfig.SERVERIZED_DATA);
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError databaseError) {
//                            Config.LOG(Config.TAG_MEDIA_ONLINE, "Error Online Track Fetch : " + databaseError, false);
//                        }
//                    });
//                } else {
//                    Config.LOG(Config.TAG_MEDIA_ONLINE, "Tracks are already updated.", false);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Config.LOG(Config.TAG_MEDIA_ONLINE, "Tracks are already updated.", false);
//            }
//        });
    }

    @Override
    public void onDataCompleted() {
        databaseReference.child("user").child(PreferenceManager.getString(getApplicationContext(), FirebaseConfig.USER_ID)).setValue("no");
    }

    @Override
    public void singleArtistCompleted(String artist) {
        onArtistDataUpdated();
    }

    @Override
    public void changeDownloadBtnVisibility(final Boolean state) {
        if (downloadFragmentBtn == null) {
            new Handler().postDelayed(() -> changeDownloadBtnVisibility(state), 50);
        } else {
            if (state) {
                if (downloadFragmentBtn.getVisibility() == View.GONE && !downloadFragment.isVisible()) {
                    downloadFragmentBtn.setVisibility(View.VISIBLE);
                }
            } else {
                if (downloadFragmentBtn.getVisibility() == View.VISIBLE) {
                    downloadFragmentBtn.setVisibility(View.GONE);
                }
            }
        }
    }
}
