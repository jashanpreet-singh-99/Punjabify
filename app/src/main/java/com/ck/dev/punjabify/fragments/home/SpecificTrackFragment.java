package com.ck.dev.punjabify.fragments.home;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ck.dev.punjabify.R;
import com.ck.dev.punjabify.adapter.SpecificTrackListAdapter;
import com.ck.dev.punjabify.interfaces.HomeToOnlineFragment;
import com.ck.dev.punjabify.interfaces.OnServerizedItemClick;
import com.ck.dev.punjabify.interfaces.OnSwipeEvent;
import com.ck.dev.punjabify.model.ServerizedTrackData;
import com.ck.dev.punjabify.tasks.LoadLocalImage;
import com.ck.dev.punjabify.threads.ThreadConfig;
import com.ck.dev.punjabify.threads.interfaces.OnSpecificTrackDataFetch;
import com.ck.dev.punjabify.threads.tasks.AlbumArtDownloader;
import com.ck.dev.punjabify.threads.ThreadPoolManager;
import com.ck.dev.punjabify.threads.interfaces.UiAlbumArtThreadCallBack;
import com.ck.dev.punjabify.threads.tasks.SpecificTrackDataLoader;
import com.ck.dev.punjabify.utils.Config;
import com.ck.dev.punjabify.utils.GenreConfig;
import com.ck.dev.punjabify.utils.PreferenceConfig;
import com.ck.dev.punjabify.utils.PreferenceManager;
import com.ck.dev.punjabify.utils.ServerizedManager;
import com.ck.dev.punjabify.utils.SwipeDetector;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class SpecificTrackFragment extends Fragment implements OnServerizedItemClick, UiAlbumArtThreadCallBack, OnSpecificTrackDataFetch {

    private ImageButton    specificIcon;
    private ProgressBar    iconProgressBar;
    private ProgressBar    dataProgressBar;
    private Button         playBtn;
    private TextView       infoTxt;
    private RecyclerView   specificTrackList;
    private RelativeLayout specificIconOverlay;
    private RelativeLayout parentLayout;

    private ImageButton downloadBtn;
    private ImageButton likeBtn;

    private LinearLayoutManager linearLayoutManager;

    private ArrayList<ServerizedTrackData> specificTracks = new ArrayList<>();

    private int SPECIFIC_TRACK_MODE = -1;

    private String GENRE  = null;
    private String ARTIST = null;
    private String YEAR = null;

    private ArrayList<Integer> resourceBackground = new ArrayList<>();

    private ServerizedManager serverizedManager;

    private SpecificTrackListAdapter specificTrackListAdapter;

    private HomeToOnlineFragment homeToOnlineFragment;

    private RelativeLayout.LayoutParams layoutParams;

    private int WID_T = 0;
    private int SAFE_TOP = 0;

    private ThreadPoolManager threadPoolManager;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.homeToOnlineFragment = (HomeToOnlineFragment) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_specific_song_list, container, false);
        specificIcon        = view.findViewById(R.id.specific_layout_title_icon);
        iconProgressBar     = view.findViewById(R.id.specific_layout_title_icon_loader);
        dataProgressBar     = view.findViewById(R.id.loading_specific_tracks);
        playBtn             = view.findViewById(R.id.play_track_list_btn);
        infoTxt             = view.findViewById(R.id.specific_info);
        specificTrackList   = view.findViewById(R.id.specific_track_list);
        specificIconOverlay = view.findViewById(R.id.specific_icon_overlay);
        downloadBtn         = view.findViewById(R.id.download_btn);
        likeBtn             = view.findViewById(R.id.like_btn);
        parentLayout        = view.findViewById(R.id.parent_layout);

        resourceBackground.add(R.drawable.rounded_btn_20);

        serverizedManager = new ServerizedManager(getContext());
        specificTrackListAdapter = new SpecificTrackListAdapter(getActivity(), this, specificTracks, resourceBackground);
        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setSmoothScrollbarEnabled(true);
        specificTrackList.setLayoutManager(linearLayoutManager);
        specificTrackList.setAdapter(specificTrackListAdapter);
        specificTrackList.setItemViewCacheSize(20);

        threadPoolManager = ThreadPoolManager.getInstance();

        fetchDataFromDatabase();

        onClick();
        return view;
    }

    private void onClick() {
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serverizedManager.createQueueDB();
                for (ServerizedTrackData track: specificTracks) {
                    serverizedManager.insertQueueTrack(track.getIndex());
                }
                PreferenceManager.setInt(getContext(), PreferenceConfig.KEY_CURRENT_TRACK_INDEX_ONLINE, -1);
                PreferenceManager.setInt(getContext(), PreferenceConfig.KEY_ONLINE_QUEUE_MODE, SPECIFIC_TRACK_MODE);
                switch (SPECIFIC_TRACK_MODE) {
                    case Config.SPECIFIC_GENRE_MODE:
                        PreferenceManager.setString(getContext(), PreferenceConfig.KEY_ONLINE_QUEUE_MODE_VALUE, GENRE);
                        break;
                    case Config.SPECIFIC_ARTIST_MODE:
                        PreferenceManager.setString(getContext(), PreferenceConfig.KEY_ONLINE_QUEUE_MODE_VALUE, ARTIST);
                        break;
                    case Config.SPECIFIC_PLAYLIST_MODE:
                        // Will Add it soon really soon
                        break;
                }
                homeToOnlineFragment.updateOnlineQueue();
                playTrack(specificTracks.get(0));
            }
        });

        new SwipeDetector(specificIconOverlay).setOnSwipeListener(new OnSwipeEvent() {
            @Override
            public void SwipeEventDetected(View v, SwipeDetector.SwipeTypeEnum swipeType) {
                if (swipeType.equals(SwipeDetector.SwipeTypeEnum.TOP_TO_BOTTOM)) {
                    homeToOnlineFragment.hideSpecificTrackFragment();
                }
            }
        });

        specificTrackList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                View v = linearLayoutManager.getChildAt(0);
                int movement = Objects.requireNonNull(v).getTop();
                SAFE_TOP = (int) (playBtn.getHeight() + Config.convertDpToPixel(40, Objects.requireNonNull(getContext())));
                if (movement >= SAFE_TOP) {
                    recyclerView.setPadding(0, WID_T, 0, 0);
                    recyclerView.setClipToPadding(false);
                    int alpha = (int)(((double)( 2 * movement)/ WID_T - 1) * 255);
                    layoutParams.topMargin = movement - WID_T;
                    specificIconOverlay.setLayoutParams(layoutParams);
                    if (alpha < 0) {
                        alpha = 0;
                    }
                    specificIcon.setImageAlpha(alpha);
                } else {
                    layoutParams.topMargin = SAFE_TOP - WID_T;
                    specificIconOverlay.setLayoutParams(layoutParams);
                    recyclerView.setPadding(0, SAFE_TOP, 0, 0);
                    recyclerView.setClipToPadding(true);
                }
            }
        });

        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (ServerizedTrackData track: specificTracks) {
                    homeToOnlineFragment.downloadTrack(track);
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        makeIconSquared();
    }

    @Override
    public void onResume() {
        super.onResume();
        onStateChange();
    }

    private void onStateChange() {
        specificTrackList.setClipToPadding(false);
        specificIcon.setImageAlpha(255);
        linearLayoutManager.scrollToPosition(0);
        makeIconSquared();
    }

    private void makeIconSquared() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        Objects.requireNonNull(getActivity()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        WID_T = displayMetrics.widthPixels;
        layoutParams = new RelativeLayout.LayoutParams(WID_T, WID_T);
        layoutParams.topMargin = 0;
        specificIcon.setLayoutParams(layoutParams);
        Config.LOG(Config.TAG_SPECIFIC_TRACK, "Width Icon : " + WID_T, false);
        specificIconOverlay.setLayoutParams(layoutParams);
        specificTrackList.setPadding(0, WID_T,0,0);
    }

    public void updateSpecificTrackData(int mode, String value) {
        if (threadPoolManager != null) {
            threadPoolManager.cancelAllTasks();
        }
        dataProgressBar.setVisibility(View.VISIBLE);
        specificTrackList.setVisibility(View.GONE);
        SPECIFIC_TRACK_MODE = mode;
        onStateChange();
        switch (SPECIFIC_TRACK_MODE) {
            case Config.SPECIFIC_GENRE_MODE:
                GENRE = value;
                infoTxt.setVisibility(View.VISIBLE);
                infoTxt.setText(GENRE);
                resourceBackground.clear();
                resourceBackground.add(GenreConfig.getGenreResource(GENRE));
                dataProgressBar.setIndeterminateTintList(ColorStateList.valueOf(Color.parseColor(GenreConfig.getGenreColor(GENRE))));
                specificIcon.setImageResource(resourceBackground.get(0));
                iconProgressBar.setVisibility(View.GONE);
                playBtn.setBackgroundResource(resourceBackground.get(0));
                break;
            case Config.SPECIFIC_ARTIST_MODE:
                ARTIST = value;
                resourceBackground.clear();
                resourceBackground.add(R.drawable.rounded_btn_20);
                playBtn.setBackgroundResource(resourceBackground.get(0));
                infoTxt.setVisibility(View.GONE);
                dataProgressBar.setIndeterminateTintList(ColorStateList.valueOf(Color.parseColor(GenreConfig.getGenreColor("artist"))));
                new LoadLocalImage(getContext(), specificIcon, iconProgressBar, 0).execute(ARTIST);
                break;
            case Config.SPECIFIC_PLAYLIST_MODE:
                YEAR = value;
                infoTxt.setVisibility(View.VISIBLE);
                infoTxt.setText(YEAR);
                resourceBackground.clear();
                resourceBackground.add(R.drawable.rounded_btn_20);
                dataProgressBar.setIndeterminateTintList(ColorStateList.valueOf(Color.parseColor(GenreConfig.getGenreColor("year"))));
                iconProgressBar.setVisibility(View.GONE);
                specificIcon.setImageResource(resourceBackground.get(0));
                playBtn.setBackgroundResource(resourceBackground.get(0));
                break;
        }
        fetchDataFromDatabase();
    }

    private void fetchDataFromDatabase(){
        SpecificTrackDataLoader specificTrackDataLoader = new SpecificTrackDataLoader();
        switch (SPECIFIC_TRACK_MODE) {
            case Config.SPECIFIC_GENRE_MODE:
                specificTrackDataLoader.setMetaData(SpecificTrackFragment.this, serverizedManager, SPECIFIC_TRACK_MODE, GENRE);
                break;
            case Config.SPECIFIC_ARTIST_MODE:
                specificTrackDataLoader.setMetaData(SpecificTrackFragment.this, serverizedManager, SPECIFIC_TRACK_MODE, ARTIST);
                break;
            case Config.SPECIFIC_PLAYLIST_MODE:
                specificTrackDataLoader.setMetaData(SpecificTrackFragment.this, serverizedManager, SPECIFIC_TRACK_MODE, YEAR);
                break;
        }
        threadPoolManager.addCallable(specificTrackDataLoader, "Load Data");

    }

    private void startDownloadingAlbumArts(){
        for (int i = 0; i < specificTracks.size(); i++) {
            ServerizedTrackData track = specificTracks.get(i);
            File f = checkImageFiles(track.getArtist(), track.getTitle());
            AlbumArtDownloader albumArtDownloader = new AlbumArtDownloader();
            albumArtDownloader.setMetaData(
                    this,
                    track.getLink(),
                    f.getAbsolutePath(),
                    i
            );
            threadPoolManager.addCallable(albumArtDownloader, ThreadConfig.DOWNLOAD_IMAGE);
        }
    }

    private File checkImageFiles(String artist, String title) {
        File img;
        artist = artist.replace(" ", "_");
        title = title.replace(" ", "_");
        File storageLoc = new File(Objects.requireNonNull(getContext()).getCacheDir() + Config.ART_DIR +  artist);
        if (!storageLoc.exists()) {
            if (!storageLoc.mkdirs()) {
                Config.LOG(Config.TAG_ART_CACHE, "ERROR in creating art dir", false);
            }
        }
        img = new File(storageLoc.getAbsolutePath() + "/" + title + ".jpg");
        return img;
    }

    @Override
    public void playTrack(ServerizedTrackData serverizedTrackData) {
        homeToOnlineFragment.playOnlineTrack(serverizedTrackData);
    }

    @Override
    public void returnDownloadedPosition(int pos) {
        final int val = pos;
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable(){
            @Override
            public void run() {
                specificTrackListAdapter.notifyItemChanged(val);
            }
        });
    }

    @Override
    public void dataFetched(final ArrayList<ServerizedTrackData> serverizedTrackData) {
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                specificTracks.clear();
                specificTracks.addAll(serverizedTrackData);
                specificTrackList.setVisibility(View.VISIBLE);
                specificTrackListAdapter.notifyDataSetChanged();
                dataProgressBar.setVisibility(View.GONE);
                startDownloadingAlbumArts();
            }
        });
    }

    @Override
    public void dataFetchingError() {
        Config.LOG(Config.TAG_SPECIFIC_TRACK, "Data fetching Error in Loading Thread", true);
    }
}
