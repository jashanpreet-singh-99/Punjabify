package com.ck.dev.punjabify.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.ck.dev.punjabify.R;
import com.ck.dev.punjabify.activities.HomeScreen;
import com.ck.dev.punjabify.broadcast.HeadPhoneReceiver;
import com.ck.dev.punjabify.interfaces.OnServiceBindEvent;
import com.ck.dev.punjabify.model.ServerizedTrackData;
import com.ck.dev.punjabify.utils.Config;
import com.ck.dev.punjabify.utils.MediaCallBackConfig;
import com.ck.dev.punjabify.utils.PlaybackStatus;
import com.ck.dev.punjabify.utils.PreferenceConfig;
import com.ck.dev.punjabify.utils.PreferenceManager;
import com.ck.dev.punjabify.utils.ServerizedManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class MediaPlayerService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener,
        MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnBufferingUpdateListener, AudioManager.OnAudioFocusChangeListener {

    private static MediaPlayer mediaPlayer;

    private AudioManager audioManager;

    private ServerizedTrackData currentOnlineTrack;

    private int pausePlayPosition;

    private final IBinder iBinder = new LocalBinder();

    private ArrayList<Integer> trackQueueServerized = new ArrayList<>();

    private ServerizedManager serverizedManager;

    private MediaSessionManager mediaSessionManager;
    private MediaSessionCompat mediaSessionCompat;
    private MediaControllerCompat.TransportControls transportControls;

    private OnServiceBindEvent onServiceBindEvent;

    private HeadPhoneReceiver headPhoneReceiver;

    private boolean IS_PLAYING = false;

    private boolean onGoingCall = false;

    private int HEADSET_KEY_COUNTER = 0;

    private boolean firstStart = true;

    private int MAX_DURATION = 0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        PreferenceManager.setBoolean(getApplicationContext(), PreferenceConfig.KEY_SERVICE_RUNNING, true);
        serverizedManager = new ServerizedManager(getApplicationContext());
        trackQueueServerized = serverizedManager.getQueue();
        headPhoneReceiver = new HeadPhoneReceiver(this);
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(headPhoneReceiver, intentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Notification notification = new Notification.Builder(getApplicationContext(), Config.CHANNEL_ID)
                    .setContentTitle("Playing Tracks.")
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setSmallIcon(R.drawable.ic_sync)
                    .setChannelId(createNotificationChannel())
                    .setOngoing(true)
                    .build();
            if (mediaSessionCompat == null ) {
                startForeground(Config.NOTIFICATION_ID, notification);
            }
        }
        handleIncomingActions(intent);
        callStateListener();
        return super.onStartCommand(intent, flags, startId);
    }

    private String createNotificationChannel() {
        NotificationChannel chan;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            chan = new NotificationChannel(Config.CHANNEL_ID, "Playing Tracks ", NotificationManager.IMPORTANCE_MIN);
            chan.setLightColor( Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            Objects.requireNonNull(service).createNotificationChannel(chan);
            return Config.CHANNEL_ID;
        }
        return "None";
    }//createNotificationChannel

    private  void buildNotification(PlaybackStatus playbackStatus) {
        String artist;
        String title;
        artist = currentOnlineTrack.getArtist();
        title = currentOnlineTrack.getTitle();
        PendingIntent playPauseAction = null;
        int playPauseIcon;
        if (playbackStatus == PlaybackStatus.PLAYING) {
            playPauseIcon = R.drawable.ic_pause;
            playPauseAction = playbackIntentAction(1);
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            playPauseIcon = R.drawable.ic_play;
            playPauseAction = playbackIntentAction(0);
        } else {
            playPauseIcon = R.drawable.ic_unknown;
        }

        Intent intent = new Intent(getApplicationContext(), HomeScreen.class);
        intent.setAction(MediaCallBackConfig.ACTION_OPEN_CONTROLLER);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

        Intent killIntent = new Intent(getApplicationContext(), MediaPlayerService.class);
        killIntent.setAction(MediaCallBackConfig.ACTION_KILL);
        PendingIntent killPendingIntent = PendingIntent.getService(getApplicationContext(), 0, killIntent, 0);


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, Config.CHANNEL_ID);
        notificationBuilder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession( mediaSessionCompat.getSessionToken())
                .setShowActionsInCompactView(0, 1, 2))
                .setColor(getResources().getColor(R.color.colorPrimary))
                .setSmallIcon(R.drawable.ic_music)
                .setContentText(artist)
                .setContentTitle(title)
                .setCategory(Notification.CATEGORY_PROGRESS)
                .setChannelId(createNotificationChannel())
                .setContentIntent(pendingIntent)
                .setDeleteIntent(killPendingIntent)
                .setOngoing(playbackStatus == PlaybackStatus.PLAYING)
                .addAction(R.drawable.ic_skip_previous, "Previous", playbackIntentAction(3))
                .addAction(playPauseIcon, "play_pause", playPauseAction)
                .addAction(R.drawable.ic_skip_next, "Next", playbackIntentAction(2));

        startForeground(Config.NOTIFICATION_ID, notificationBuilder.build());

    }

    private void cancelNotification() {
        NotificationManager notificationManager = (NotificationManager) Objects.requireNonNull(getSystemService(NOTIFICATION_SERVICE));
        notificationManager.cancel(Config.NOTIFICATION_ID);
    }


    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if (mediaPlayer == null)
                    initMediaPlayer();
                else if (!mediaPlayer.isPlaying())
                    mediaPlayer.start();
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                if (mediaPlayer.isPlaying())
                    mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (mediaPlayer.isPlaying())
                    mediaPlayer.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (mediaPlayer.isPlaying())
                    mediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        int result = Objects.requireNonNull(audioManager).requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private boolean removeAudioFocus() {
        if (audioManager == null) {
            return false;
        }
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager.abandonAudioFocus(this);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        // means we are listening online
        if (trackQueueServerized == null) {
            onServerizedQueueUpdated();
        }
        if (trackQueueServerized != null && trackQueueServerized.size() > 1) {
            int trackIndex = PreferenceManager.getInt(getApplicationContext(), PreferenceConfig.KEY_CURRENT_TRACK_INDEX_ONLINE);
            trackIndex++;
            PreferenceManager.setInt(getApplicationContext(), PreferenceConfig.KEY_CURRENT_TRACK_INDEX_ONLINE, trackIndex);
            trackIndex = trackQueueServerized.get(trackIndex);
            Config.LOG(Config.TAG_MEDIA_ONLINE, "Playing Next Song " + trackIndex, false);
            currentOnlineTrack = serverizedManager.getIdSpecificTrack(trackIndex);
            Config.LOG(Config.TAG_MEDIA, "Playing Next Song " + currentOnlineTrack.getTitle(), false);
        }
        initMediaPlayer();
        if (onServiceBindEvent != null) {
            onServiceBindEvent.trackCompletedOnline(currentOnlineTrack);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Config.LOG(Config.TAG_MEDIA, "Progress playback error", true);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Config.LOG(Config.TAG_MEDIA, "Server died error", true);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Config.LOG(Config.TAG_MEDIA, "Some Thing did happened but i IDK what?", true);
                break;
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (IS_PLAYING) {
            playTrack();
        }
        MAX_DURATION = mediaPlayer.getDuration();
        if (onServiceBindEvent != null) {
            onServiceBindEvent.seekMaxUpdated(MAX_DURATION);
        }
        Config.LOG(Config.TAG_MEDIA_ONLINE, "Track Duration : " + mediaPlayer.getDuration()/1000, false);
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
    }

    private void initMediaPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();

            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnBufferingUpdateListener(this);
            mediaPlayer.setOnSeekCompleteListener(this);
            mediaPlayer.setOnInfoListener(this);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnErrorListener(this);
        }
        if (!requestAudioFocus()) {
            stopSelf();
        }
        mediaPlayer.reset();

        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());

        if (currentOnlineTrack == null) {
            return;
        }

        String link = currentOnlineTrack.getLink();
        if (!link.equals(Config.DOWNLOADED_TRACK)) {
            try {
                mediaPlayer.setDataSource(currentOnlineTrack.getLink());
                Config.LOG(Config.TAG_MEDIA_ONLINE, " Playing online", false);
            } catch (Exception e) {
                Config.LOG(Config.TAG_MEDIA, "Media Link creating problem.", true);
            }
        } else {
            try {
                Config.LOG(Config.TAG_DOWNLOAD, "Playing Downloaded Track", false);
                mediaPlayer.setDataSource(getApplicationContext().getCacheDir() + Config.TRACKS_DIR + currentOnlineTrack.getArtist().replace(" ", "_") + "/" + currentOnlineTrack.getTitle().replace(" ", "_") + ".mp3");
            } catch (IOException e) {
                Config.LOG(Config.TAG_DOWNLOAD, "Track Not Found Locally Error " + e, false);
                e.printStackTrace();
            }
        }
        mediaPlayer.prepareAsync();
        if (mediaSessionManager == null) {
            initMediaSession();
            buildNotification(PlaybackStatus.PAUSED);
        } else {
            mediaSessionCompat.setActive(true);
            updateMetaData();
        }

        if (firstStart) {
            String lastTitle = PreferenceManager.getString(getApplicationContext(), PreferenceConfig.KEY_CURRENT_TRACK_TITLE);
            if (lastTitle != null && lastTitle.equalsIgnoreCase(currentOnlineTrack.getTitle())) {
                setCurrentSeek(PreferenceManager.getInt(getApplicationContext(), PreferenceConfig.KEY_CURRENT_TRACK_SEEK));
            }
            firstStart = false;
        }
    }

    public void playTrack() {
        IS_PLAYING = true;
        if (mediaPlayer == null){
            // trying to play track previously in queue
            initMediaPlayer();
        }
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            if (mediaSessionCompat != null) {
                buildNotification(PlaybackStatus.PLAYING);
            }
            if (onServiceBindEvent != null) {
                onServiceBindEvent.trackStateChangedPaused(false);
            }
            PreferenceManager.setBoolean(getApplicationContext(), PreferenceConfig.KEY_SERVICE_RUNNING, true);
        }
    }

    public void stopTrack() {
        if (mediaPlayer == null)
            return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(Service.STOP_FOREGROUND_DETACH);
            }
        }
    }

    public void pauseTrack() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            pausePlayPosition = mediaPlayer.getCurrentPosition();
            if (mediaSessionCompat != null) {
                buildNotification(PlaybackStatus.PAUSED);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(Service.STOP_FOREGROUND_DETACH);
            }
            if (onServiceBindEvent != null) {
                onServiceBindEvent.trackStateChangedPaused(true);
            }
            PreferenceManager.setBoolean(getApplicationContext(), PreferenceConfig.KEY_SERVICE_RUNNING, false);
        }
    }

    public void resumeTrack() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(pausePlayPosition);
            mediaPlayer.start();
            buildNotification(PlaybackStatus.PLAYING);
            if (onServiceBindEvent != null) {
                onServiceBindEvent.trackStateChangedPaused(false);
            }
        }
    }

    public boolean isPlaying() {
        if (mediaPlayer == null) {
            return false;
        }
        try {
            return mediaPlayer.isPlaying();
        } catch (Exception ex) {
            return false;
        }
    }

    public void onServerizedQueueUpdated() {
        trackQueueServerized = serverizedManager.getQueue();
    }

    public long getCurrentTrackSeek() {
        return mediaPlayer.getCurrentPosition();
    }

    public void setCurrentSeek(int seek) {
        mediaPlayer.seekTo(seek);
    }

    public void sendTrackToPlay(ServerizedTrackData serverizedTrackData) {
        currentOnlineTrack = serverizedTrackData;
        initMediaPlayer();
    }

    public long getTrackDuration() {
        if (mediaPlayer == null) {
            return 0;
        }
        return MAX_DURATION;
    }
    public  ServerizedTrackData getCurrentOnlineTrack() {
        return currentOnlineTrack;
    }

    private void initMediaSession() {
        if (mediaSessionManager != null) {
            return;
        }

        mediaSessionManager = (MediaSessionManager) getSystemService(MEDIA_SESSION_SERVICE);
        mediaSessionCompat  = new MediaSessionCompat(getApplicationContext(), MediaCallBackConfig.MEDIA_SESSION_NAME);
        transportControls   = mediaSessionCompat.getController().getTransportControls();
        mediaSessionCompat.setActive(true);
        mediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS);

        updateMetaData();

        mediaSessionCompat.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                playTrack();
            }

            @Override
            public void onPause() {
                super.onPause();
                pauseTrack();
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                nextTrack();
                updateMetaData();
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                prevTrack();
                updateMetaData();
            }

            @Override
            public void onStop() {
                super.onStop();
                stopTrack();
            }

            @Override
            public void onSeekTo(long pos) {
                super.onSeekTo(pos);
                setCurrentSeek((int) pos);
            }

            @Override
            public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
                KeyEvent event = mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                Config.LOG(Config.TAG_MEDIA_HEADSET, " Action : " + event, false);
                if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_HEADSETHOOK) {
                    return handleHeadsetHook(event);
                }
                return true;
            }
        });
    }

    private void updateMetaData() {
        // only for online
        mediaSessionCompat.setMetadata(new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, currentOnlineTrack.getAlbum())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentOnlineTrack.getTitle())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, currentOnlineTrack.getArtist())
                .build()
        );
        buildNotification(PlaybackStatus.PLAYING);
    }

    public void nextTrack() {
        // next Online Track
        Config.LOG(Config.TAG_MEDIA_ONLINE, "Next Track", false);
        int trackIndex = PreferenceManager.getInt(getApplicationContext(), PreferenceConfig.KEY_CURRENT_TRACK_INDEX_ONLINE);
        trackIndex++;
        if (trackIndex >= trackQueueServerized.size()) {
            trackIndex = trackQueueServerized.size();
        }
        PreferenceManager.setInt(getApplicationContext(), PreferenceConfig.KEY_CURRENT_TRACK_INDEX_ONLINE, trackIndex);
        trackIndex = trackQueueServerized.get(trackIndex);
        Config.LOG(Config.TAG_MEDIA_ONLINE, "Playing Next Song " + trackIndex, false);
        ServerizedTrackData track = serverizedManager.getIdSpecificTrack(trackIndex);
        sendTrackToPlay(track);
        if (onServiceBindEvent != null) {
            onServiceBindEvent.trackCompletedOnline(currentOnlineTrack);
        }
    }

    public void prevTrack() {
        // previous Online Track
        Config.LOG(Config.TAG_MEDIA_ONLINE, "Previous Track complete.", false);
        int trackIndex = PreferenceManager.getInt(getApplicationContext(), PreferenceConfig.KEY_CURRENT_TRACK_INDEX_ONLINE);
        trackIndex--;
        // prevent to go below 0
        if (trackIndex < 0) {
            trackIndex = 0;
        }
        PreferenceManager.setInt(getApplicationContext(), PreferenceConfig.KEY_CURRENT_TRACK_INDEX_ONLINE, trackIndex);
        trackIndex = trackQueueServerized.get(trackIndex);
        Config.LOG(Config.TAG_MEDIA_ONLINE, "Playing Next Song " + trackIndex, false);
        ServerizedTrackData track = serverizedManager.getIdSpecificTrack(trackIndex);
        sendTrackToPlay(track);
        if (onServiceBindEvent != null) {
            onServiceBindEvent.trackCompletedOnline(currentOnlineTrack);
        }
    }

    /**
     *  Util Section
     */

    Runnable keyDownTimer = new Runnable() {
        @Override
        public void run() {
            if (HEADSET_KEY_COUNTER == 1) {
                if (mediaPlayer.isPlaying()) {
                    pauseTrack();
                } else {
                    resumeTrack();
                }
            } else if (HEADSET_KEY_COUNTER == 2){
                nextTrack();
            } else if (HEADSET_KEY_COUNTER == 3) {
                prevTrack();
            }
            HEADSET_KEY_COUNTER = 0;
        }
    };

    private boolean handleHeadsetHook(KeyEvent event){
        Config.LOG(Config.TAG_MEDIA_HEADSET, " Action : " + event.getFlags(), false);
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            HEADSET_KEY_COUNTER++;
            if (HEADSET_KEY_COUNTER == 1) {
                new Handler().postDelayed(keyDownTimer, 500);
            }
        }
        return true;
    }

    private PendingIntent playbackIntentAction(int action) {
        Intent playbackAction = new Intent(this, MediaPlayerService.class);
        switch (action) {
            case 0:
                playbackAction.setAction(MediaCallBackConfig.ACTION_PLAY);
                return PendingIntent.getService(this, action, playbackAction, 0);
            case 1:
                playbackAction.setAction(MediaCallBackConfig.ACTION_PAUSE);
                return PendingIntent.getService(this, action, playbackAction, 0);
            case 2:
                playbackAction.setAction(MediaCallBackConfig.ACTION_NEXT);
                return PendingIntent.getService(this, action, playbackAction, 0);
            case 3:
                playbackAction.setAction(MediaCallBackConfig.ACTION_PREVIOUS);
                return PendingIntent.getService(this, action, playbackAction, 0);
            default:
                break;
        }
        return null;
    }

    private void handleIncomingActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null ) return;
        String action = playbackAction.getAction();
        if (action.equalsIgnoreCase(MediaCallBackConfig.ACTION_PLAY)) {
            transportControls.play();
        } else if (action.equalsIgnoreCase(MediaCallBackConfig.ACTION_PAUSE)) {
            transportControls.pause();
        } else if (action.equalsIgnoreCase(MediaCallBackConfig.ACTION_NEXT)) {
            transportControls.skipToNext();
        } else if (action.equalsIgnoreCase(MediaCallBackConfig.ACTION_PREVIOUS)) {
            transportControls.skipToPrevious();
        } else if (action.equalsIgnoreCase(MediaCallBackConfig.ACTION_STOP)) {
            transportControls.stop();
        } else if (action.equalsIgnoreCase(MediaCallBackConfig.ACTION_KILL)) {
            Config.LOG(Config.TAG_MEDIA, "Killing due to Notification removal", true);
            stopSelf();
        }
     }

     private void callStateListener() {
         TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
         PhoneStateListener phoneStateListener = new PhoneStateListener() {
             @Override
             public void onCallStateChanged(int state, String phoneNumber) {
                 switch (state) {
                     case TelephonyManager.CALL_STATE_OFFHOOK:
                     case TelephonyManager.CALL_STATE_RINGING:
                         if (mediaPlayer != null) {
                             pauseTrack();
                             onGoingCall = true;
                         }
                         break;
                     case TelephonyManager.CALL_STATE_IDLE:
                         if (mediaPlayer != null) {
                             if (onGoingCall) {
                                 onGoingCall = false;
                                 resumeTrack();
                             }
                         }
                         break;
                 }
             }
         };
        Objects.requireNonNull(telephonyManager).listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
     }

     public void setOnServiceBindEvent(OnServiceBindEvent onServiceBindEvent) {
        this.onServiceBindEvent = onServiceBindEvent;
     }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelNotification();
        PreferenceManager.setBoolean(getApplicationContext(), PreferenceConfig.KEY_SERVICE_RUNNING, false);
        if (headPhoneReceiver != null) {
            unregisterReceiver(headPhoneReceiver);
        }
        if (!removeAudioFocus()) {
            Config.LOG(Config.TAG_MEDIA, "Error in losing Audio Focus", true);
        }
        if (mediaPlayer != null) {
            PreferenceManager.setString(getApplicationContext(), PreferenceConfig.KEY_CURRENT_TRACK_TITLE, currentOnlineTrack.getTitle());
            PreferenceManager.setInt(getApplicationContext(), PreferenceConfig.KEY_CURRENT_TRACK_SEEK, (int)getCurrentTrackSeek());
            PreferenceManager.setInt(getApplicationContext(), PreferenceConfig.KEY_CURRENT_TRACK_DURATION, MAX_DURATION);
            stopTrack();
            mediaPlayer.release();
        }
        Config.LOG(Config.TAG_MEDIA, "MEDIA PLAYER SERVICE KILLED.", true);
    }

    public class LocalBinder extends Binder {

        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }

    }

}
