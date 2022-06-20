package com.ck.dev.punjabify.fragments.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ck.dev.punjabify.R;
import com.ck.dev.punjabify.interfaces.MusicControlConnection;
import com.ck.dev.punjabify.model.ServerizedTrackData;
import com.ck.dev.punjabify.threads.ThreadConfig;
import com.ck.dev.punjabify.threads.ThreadPoolManager;
import com.ck.dev.punjabify.threads.tasks.AlbumArtLoader;
import com.ck.dev.punjabify.utils.Config;
import com.ck.dev.punjabify.utils.PreferenceConfig;
import com.ck.dev.punjabify.utils.PreferenceManager;
import com.ck.dev.punjabify.utils.SwipeDetector;
import com.ck.dev.punjabify.view.MusicProgressBar;
import com.ck.dev.punjabify.view.VolumeBar;

import java.util.Locale;
import java.util.Objects;

public class MediaControlUI extends Fragment {

    private RelativeLayout albumLayout;
    private ImageView      albumArt;
    private ImageButton    pausePlayBtn;
    private TextView       trackCurrentSeek;
    private TextView       trackMaxSeek;
    private TextView       titleTxtView;
    private TextView       artistTxtView;
    private VolumeBar      volumeBar;

    private MusicProgressBar musicProgressBar;

    private ImageButton    nextTrackBtn;
    private ImageButton    previousTrackBtn;

    private ImageButton    downloadBtn;
    private ImageButton    likeBtn;

    private ImageButton    incrementVolume;
    private ImageButton    decrementVolume;

    private ImageButton    openQueueBtn;

    private ProgressBar    bufferProgressBar;

    private long currentSeek;

    private ServerizedTrackData currentOnlineTrack;

    private boolean stopTracking = false;

    private boolean playing = false;

    private boolean firstTime = true;

    private MusicControlConnection musicControlConnection;

    private AudioManager audioManager;

    private GestureDetector gestureDetector;

    private final GestureDetector.SimpleOnGestureListener simpleOnGestureListener = new GestureDetector.SimpleOnGestureListener(){

        @Override
        public boolean onDown(MotionEvent e) {
            stopTracking = true;
            Config.LOG(Config.TAG_ALBUM_VIEW, "DOWN.", false);
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            musicControlConnection.setCurrentSeek(musicProgressBar.getProgress());
            stopTracking = false;
            new Handler().post(trackSongSeek);
            return true;
        }

    };

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        musicControlConnection = (MusicControlConnection) context;
    }

    public  void  newTrack(ServerizedTrackData onlineTrackData) {
        if (onlineTrackData == null) {
            return;
        }
        currentOnlineTrack = onlineTrackData;
        updateUI();
        if (firstTime) {
            String lastTitle = PreferenceManager.getString(getContext(), PreferenceConfig.KEY_CURRENT_TRACK_TITLE);
            if (lastTitle != null &&lastTitle.equalsIgnoreCase(currentOnlineTrack.getTitle())) {
                musicProgressBar.setProgress(PreferenceManager.getInt(getContext(), PreferenceConfig.KEY_CURRENT_TRACK_SEEK));
                musicProgressBar.setMAX(PreferenceManager.getInt(getContext(), PreferenceConfig.KEY_CURRENT_TRACK_DURATION));
                trackCurrentSeek.setText(getFormatDuration(String.valueOf(PreferenceManager.getInt(getContext(), PreferenceConfig.KEY_CURRENT_TRACK_SEEK))));
                trackMaxSeek.setText(getFormatDuration(String.valueOf(PreferenceManager.getInt(getContext(), PreferenceConfig.KEY_CURRENT_TRACK_DURATION))));
            }
            firstTime = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (audioManager != null) {
            volumeBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        }
        if (musicControlConnection.isPlaying()) {
            trackMaxSeek.setText(getFormatDuration(String.valueOf(musicControlConnection.getMaxSeek())));
            musicProgressBar.setMAX(Integer.parseInt(musicControlConnection.getMaxSeek() + ""));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music_control, container, false);
        pausePlayBtn      = view.findViewById(R.id.pause_play_btn);
        trackCurrentSeek  = view.findViewById(R.id.track_current_seek);
        trackMaxSeek      = view.findViewById(R.id.track_max_seek);
        titleTxtView      = view.findViewById(R.id.song_title_txt);
        artistTxtView     = view.findViewById(R.id.song_artist_txt);
        albumArt          = view.findViewById(R.id.album_art);
        albumLayout       = view.findViewById(R.id.album_art_layout);
        nextTrackBtn      = view.findViewById(R.id.next_track_btn);
        previousTrackBtn  = view.findViewById(R.id.previous_track_btn);
        volumeBar         = view.findViewById(R.id.track_volume);
        incrementVolume   = view.findViewById(R.id.increment_volume);
        decrementVolume   = view.findViewById(R.id.decrement_volume);
        openQueueBtn      = view.findViewById(R.id.open_queue);
        downloadBtn       = view.findViewById(R.id.download_btn);
        likeBtn           = view.findViewById(R.id.like_btn);
        bufferProgressBar = view.findViewById(R.id.buffering_progress_bar);

        musicProgressBar = view.findViewById(R.id.music_bar);
        musicProgressBar.setProgress(0);
        gestureDetector = new GestureDetector(getContext(), simpleOnGestureListener);

        audioManager = (AudioManager) Objects.requireNonNull(getContext()).getSystemService(Context.AUDIO_SERVICE);
        volumeBar.setMAX(Objects.requireNonNull(audioManager).getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        volumeBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));

        onClick();
        return view;
    }

    Runnable trackSongSeek = new Runnable() {
        @Override
        public void run() {
            if (musicControlConnection.checkIfNull()) {
                playing = false;
                pausePlayBtn.setImageResource(R.drawable.ic_play);
                return;
            }
            if (musicControlConnection.isPlaying()) {
                if (!playing) {
                    pausePlayBtn.setImageResource(R.drawable.ic_pause);
                    playing = true;
                }
                long cSeek = musicControlConnection.getCurrentSeek();
                if (cSeek == currentSeek) {
                    bufferProgressBar.setVisibility(View.VISIBLE);
                } else {
                    bufferProgressBar.setVisibility(View.GONE);
                }
                currentSeek = cSeek;
                trackCurrentSeek.setText(getFormatDuration(String.valueOf(currentSeek)));
                musicProgressBar.setProgress(Integer.parseInt(String.valueOf(currentSeek)));
                if (stopTracking) {
                    return;
                }
            } else {
                playing = false;
                if (bufferProgressBar.getVisibility() == View.VISIBLE) {
                    bufferProgressBar.setVisibility(View.GONE);
                }
                pausePlayBtn.setImageResource(R.drawable.ic_play);
            }
            new Handler().postDelayed(trackSongSeek, 100);
        }
    };

    private void updateUI() {
        if (currentOnlineTrack != null) {
            if (currentOnlineTrack.getLink().equals(Config.DOWNLOADED_TRACK)) {
                downloadBtn.setImageResource(R.drawable.ic_download_dark);
            } else {
                downloadBtn.setImageResource(R.drawable.ic_download);
            }
            if (musicControlConnection.isPlaying()) {
                trackMaxSeek.setText(getFormatDuration(String.valueOf(musicControlConnection.getMaxSeek())));
                musicProgressBar.setMAX(Integer.parseInt(musicControlConnection.getMaxSeek() + ""));
            }
            titleTxtView.setText(currentOnlineTrack.getTitle());
            artistTxtView.setText(currentOnlineTrack.getArtist());
            AlbumArtLoader albumArtLoader = new AlbumArtLoader();
            albumArtLoader.setMetaData(
                    getActivity(),
                    albumArt,
                    Objects.requireNonNull(getContext()).getCacheDir() + Config.ART_DIR,
                    currentOnlineTrack.getArtist(),
                    currentOnlineTrack.getTitle(),
                    1
            );
            ThreadPoolManager.getInstance().addCallable(albumArtLoader, ThreadConfig.IMAGE_LOAD);
            new Handler().post(trackSongSeek);
        }
    }

    private String getFormatDuration(String val) {
        long value = Long.parseLong(val) / 1000;
        long sec = value % 60;
        long min = value / 60;
        return String.format(Locale.ENGLISH, "%02d:%02d", min, sec);
    }

    public void updateVolumeBar() {
        if (audioManager == null) {
            return;
        }
        volumeBar.setVisibility(View.VISIBLE);
        Config.LOG(Config.TAG_MEDIA, "Volume " + audioManager.getStreamVolume(AudioManager.STREAM_MUSIC), true);
        volumeBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        new Handler().postDelayed(() -> volumeBar.setVisibility(View.GONE), 500);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void onClick() {
        pausePlayBtn.setOnClickListener(v -> {
            if (musicControlConnection.isPlaying()) {
                musicControlConnection.pauseTrack();
                pausePlayBtn.setImageResource(R.drawable.ic_play);
            } else {
                musicControlConnection.playTrack();
                pausePlayBtn.setImageResource(R.drawable.ic_pause);
                new Handler().post(trackSongSeek);
            }
        });

        new SwipeDetector(albumLayout).setOnSwipeListener((v, swipeType) -> {
            switch (swipeType) {
                case RIGHT_TO_LEFT:
                    musicControlConnection.nextTrack();
                    break;
                case LEFT_TO_RIGHT:
                    musicControlConnection.prevTrack();
                    break;
                case TOP_TO_BOTTOM:
                    musicControlConnection.hideController();
                    break;
                case BOTTOM_TO_TOP:
                    musicControlConnection.openSpecificTrackFragment(
                            PreferenceManager.getInt(getContext(), PreferenceConfig.KEY_ONLINE_QUEUE_MODE),
                            PreferenceManager.getString(getContext(), PreferenceConfig.KEY_ONLINE_QUEUE_MODE_VALUE)
                    );
                    musicControlConnection.hideController();
                    break;
            }
        });

        nextTrackBtn.setOnClickListener(v -> musicControlConnection.nextTrack());

        previousTrackBtn.setOnClickListener(v -> musicControlConnection.prevTrack());

        musicProgressBar.setOnTouchListener((v, event) -> {
            trackCurrentSeek.setText(getFormatDuration(String.valueOf(musicProgressBar.getProgress())));
            if (gestureDetector.onTouchEvent(event)) {
                return true;
            } else {
                Config.LOG(Config.TAG_ALBUM_VIEW, "Moved! Baby.", false);
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    Config.LOG(Config.TAG_ALBUM_VIEW, "Up! Baby.", false);
                    musicControlConnection.setCurrentSeek(musicProgressBar.getProgress());
                    stopTracking = false;
                    new Handler().post(trackSongSeek);
                } else {
                    Config.LOG(Config.TAG_ALBUM_VIEW, "Moved! Baby.", false);
                }
                return false;
            }
        });

        incrementVolume.setOnClickListener(v -> {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE,0);
            updateVolumeBar();
        });

        decrementVolume.setOnClickListener(v -> {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER,0);
            updateVolumeBar();
        });

        openQueueBtn.setOnClickListener(v -> {
            musicControlConnection.openSpecificTrackFragment(
                    PreferenceManager.getInt(getContext(), PreferenceConfig.KEY_ONLINE_QUEUE_MODE),
                    PreferenceManager.getString(getContext(), PreferenceConfig.KEY_ONLINE_QUEUE_MODE_VALUE)
            );
            musicControlConnection.hideController();
        });

        downloadBtn.setOnClickListener(v -> {
            Config.LOG(Config.TAG_DOWNLOAD, "Starting to download " + currentOnlineTrack.getTitle(), false);
            musicControlConnection.downloadTrack(currentOnlineTrack);
        });
    }

    public void updateMaxSeek(int max_duration) {
        trackMaxSeek.setText(getFormatDuration(String.valueOf(max_duration)));
        musicProgressBar.setMAX(max_duration);
    }
}
