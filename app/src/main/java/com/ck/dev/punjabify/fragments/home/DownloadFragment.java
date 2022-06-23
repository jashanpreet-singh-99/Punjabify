package com.ck.dev.punjabify.fragments.home;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ck.dev.punjabify.R;
import com.ck.dev.punjabify.adapter.DownloadTrackListAdapter;
import com.ck.dev.punjabify.interfaces.HomeToDownloadFragment;
import com.ck.dev.punjabify.interfaces.OnDownloadQueueClicks;
import com.ck.dev.punjabify.model.DownloadData;
import com.ck.dev.punjabify.model.ServerizedTrackData;
import com.ck.dev.punjabify.services.DownloadManagerService;
import com.ck.dev.punjabify.threads.ThreadConfig;
import com.ck.dev.punjabify.threads.ThreadPoolManager;
import com.ck.dev.punjabify.threads.interfaces.UiTrackDownloadingThreadCallBack;
import com.ck.dev.punjabify.threads.tasks.AlbumArtLoader;
import com.ck.dev.punjabify.utils.Config;
import com.ck.dev.punjabify.utils.ServerizedManager;
import com.ck.dev.punjabify.view.CircularProgress;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadFragment extends Fragment implements OnDownloadQueueClicks, UiTrackDownloadingThreadCallBack {

    private RecyclerView downloadQueueList;
    private TextView     noDownloadTextView;
    private Button       clearAllBtn;

    private LinearLayout     currentDownloadLayout;
    private CircularProgress downloadProgress;
    private ImageView        albumArtView;
    private TextView         trackTitleTxt;
    private TextView         progressTxt;
    private ImageButton      cancelBtn;

    private DownloadTrackListAdapter downloadTrackListAdapter;

    private final ArrayList<DownloadData> downloadData = new ArrayList<>();

    private ServerizedManager serverizedManager;

    private int CURRENT_MAX = 0;

    private ExecutorService executorService;

    private HomeToDownloadFragment homeToDownloadFragment;

    private DownloadManagerService downloadManager;

    private boolean DOWNLOAD_MANAGER_BOUNDED = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.homeToDownloadFragment = (HomeToDownloadFragment) context;
        if (executorService == null) {
            executorService = Executors.newSingleThreadExecutor();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_download, container, false);
        downloadQueueList     = view.findViewById(R.id.download_queue_list);
        noDownloadTextView    = view.findViewById(R.id.no_download_txt);
        clearAllBtn           = view.findViewById(R.id.clear_all_btn);

        currentDownloadLayout = view.findViewById(R.id.current_download_stat);
        downloadProgress      = view.findViewById(R.id.downloading_progress);
        albumArtView          = view.findViewById(R.id.album_art_btn);
        trackTitleTxt         = view.findViewById(R.id.track_title);
        progressTxt           = view.findViewById(R.id.downloading_progress_txt);
        cancelBtn             = view.findViewById(R.id.cancel_btn);

        serverizedManager = new ServerizedManager(getContext());

        downloadQueueList.setLayoutManager(new LinearLayoutManager(getContext()));
        downloadTrackListAdapter = new DownloadTrackListAdapter(getActivity(), this, downloadData);
        downloadQueueList.setAdapter(downloadTrackListAdapter);

        updateDownloadTracks();

        onClick();
        return view;
    }

    private void onClick() {

        clearAllBtn.setOnClickListener((v) -> {
            if (downloadManager != null) {
                if (downloadData.size() == 0) {
                    return;
                }

                downloadTrackListAdapter.notifyItemRangeRemoved(0, downloadData.size());
                downloadManager.clearDownloadQueue();
                downloadData.clear();
                noDownloadTextView.setVisibility(View.VISIBLE);
                currentDownloadLayout.setVisibility(View.GONE);
            } else {
                Config.LOG(Config.TAG_DOWNLOAD, "Clearing Download Queue. Error", true);
            }
        });

        cancelBtn.setOnClickListener((v) -> onTrackCanceled(0));
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Config.LOG(Config.TAG_DOWNLOAD, "Trying to create service connection with download manager.", false);
            DownloadManagerService.LocalBinder binder = (DownloadManagerService.LocalBinder) service;
            downloadManager = binder.getService();
            downloadManager.setCallBack(DownloadFragment.this);
            DOWNLOAD_MANAGER_BOUNDED = true;
            updateDownloadTracks();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Config.LOG(Config.TAG_DOWNLOAD, "Disconnected from download manager.", false);
            DOWNLOAD_MANAGER_BOUNDED = false;
        }
    };

    public void startDownloadManager() {
        if (DOWNLOAD_MANAGER_BOUNDED) {
            Config.LOG(Config.TAG_DOWNLOAD, "Already the service is running", false);
            updateDownloadTracks();
            downloadManager.setCallBack(DownloadFragment.this);
            return;
        }
        Intent intent = new Intent(getContext(), DownloadManagerService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Objects.requireNonNull(getContext()).startForegroundService(intent);
        } else {
            Objects.requireNonNull(getContext()).startService(intent);
        }
        getContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        // Move to on Service connected ->

        //serverizedManager.clearDownloadQueue();
    }

    public void updateDownloadTracks(){
        if (downloadManager == null) {
            new Handler().postDelayed(this::startDownloadManager, 100);
        } else {
            downloadManager.updateDownloadQueue();
            downloadData.clear();
            Config.LOG(Config.TAG_DOWNLOAD, "Updating Download Queue", false);
            downloadData.addAll(downloadManager.getDownloadQueue());
            Config.LOG(Config.TAG_DOWNLOAD, "Download Queue Size : " + downloadData.size(), false);
            if (downloadData.size() <= 0 ) {
                noDownloadTextView.setVisibility(View.VISIBLE);
                currentDownloadLayout.setVisibility(View.GONE);
            } else {
                noDownloadTextView.setVisibility(View.GONE);
                downloadTrackListAdapter.notifyItemRangeInserted(0, downloadData.size());
            }
        }
    }

    public void insertDownloadTrack(int index){
        downloadData.add(new DownloadData(serverizedManager.getIdSpecificTrack(index), 0));
        downloadTrackListAdapter.notifyItemChanged(downloadData.size());
//        if (downloadData.size() == 1) {
//            // if a new download is inserted
//            downloadFirstQueueTrack();
//        }
//        if (downloadData.size() > 0) {
//            homeToDownloadFragment.changeDownloadBtnVisibility(true);
//        }
    }

    @Override
    public void onTrackCanceled(int pos) {
        serverizedManager.removeDownloadQueueTrack(downloadData.get(pos).getTrackData().getIndex());
        Config.LOG(Config.TAG_DOWNLOAD, "Track Canceled " + downloadData.get(pos).getTrackData().getTitle(), false);
        if (pos == 0) {
            downloadData.remove(0);
            downloadTrackListAdapter.notifyItemRemoved(0);
            if (downloadData.size() > 0) {
                downloadManager.downloadFirstQueueTrack();
                homeToDownloadFragment.changeDownloadBtnVisibility(true);
            } else {
                homeToDownloadFragment.changeDownloadBtnVisibility(false);
            }
            return;
        }
        downloadData.remove(pos);
        downloadTrackListAdapter.notifyItemRangeRemoved(pos, downloadData.size());
    }

    public void progressUpdated(int progress) {
        //Config.LOG(Config.TAG_DOWNLOAD, "Track Downloaded " + Config.convertToMB(progress), false);
        int progressPercent = (progress * 100)/CURRENT_MAX;
        downloadProgress.setProgress(progressPercent);
        progressTxt.setText(String.format(Locale.ENGLISH,"Downloading %d %%", progressPercent));
    }

    public void trackDownloaded(int id) {
        downloadData.remove(0);
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> downloadTrackListAdapter.notifyItemRemoved(0));
        if (downloadData.size() <= 0) {
            noDownloadTextView.setVisibility(View.VISIBLE);
            currentDownloadLayout.setVisibility(View.GONE);
        }
        homeToDownloadFragment.changeDownloadBtnVisibility(downloadData.size() > 0);
    }

    public void alreadyDownloaded() {
        Config.LOG(Config.TAG_DOWNLOAD, "Process the next Track to Download", false);
        onTrackCanceled(0);
    }

    @Override
    public void startTrackDownload(ServerizedTrackData track) {
        if (currentDownloadLayout.getVisibility() == View.GONE)
            currentDownloadLayout.setVisibility(View.VISIBLE);
        downloadProgress.setProgress(0);
        trackTitleTxt.setText(track.getTitle());
        progressTxt.setText(String.format(Locale.ENGLISH,"Downloading %d %%", 0));
        albumArtView.setBackgroundResource(R.color.colorAccent);
        AlbumArtLoader albumArtLoader = new AlbumArtLoader();
        albumArtLoader.setMetaData(
                requireActivity(),
                albumArtView,
                requireActivity().getCacheDir() + Config.ART_DIR,
                track.getArtist(),
                track.getTitle(),
                2
        );
        ThreadPoolManager.getInstance().addCallable(albumArtLoader, ThreadConfig.IMAGE_LOAD);
    }

    public void updateMax(int max) {
        CURRENT_MAX = max;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (downloadManager != null) {
            Objects.requireNonNull(getContext()).unbindService(serviceConnection);
        }
    }
}

