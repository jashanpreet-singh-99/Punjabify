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
import com.ck.dev.punjabify.threads.interfaces.UiTrackDownloadingThreadCallBack;
import com.ck.dev.punjabify.threads.tasks.TrackUrlDownloader;
import com.ck.dev.punjabify.utils.Config;
import com.ck.dev.punjabify.utils.ServerizedManager;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadFragment extends Fragment implements OnDownloadQueueClicks, UiTrackDownloadingThreadCallBack {

    private RecyclerView downloadQueueList;

    private DownloadTrackListAdapter downloadTrackListAdapter;

    private ArrayList<DownloadData> downloadData = new ArrayList<>();

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
        downloadQueueList = view.findViewById(R.id.download_queue_list);

        serverizedManager = new ServerizedManager(getContext());

        downloadQueueList.setLayoutManager(new LinearLayoutManager(getContext()));
        downloadTrackListAdapter = new DownloadTrackListAdapter(getActivity(), this, downloadData);
        downloadQueueList.setAdapter(downloadTrackListAdapter);

        updateDownloadTracks();

        return view;
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Config.LOG(Config.TAG_DOWNLOAD, "Trying to create service connection with download manager.", false);
            DownloadManagerService.LocalBinder binder = (DownloadManagerService.LocalBinder) service;
            downloadManager = binder.getService();
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
            return;
        }
        Intent intent = new Intent(getContext(), DownloadManagerService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Objects.requireNonNull(getContext()).startForegroundService(intent);
        } else {
            Objects.requireNonNull(getContext()).startService(intent);
        }
        getContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        serverizedManager.clearDownloadQueue();
    }

    public void updateDownloadTracks(){
        if (downloadManager == null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startDownloadManager();
                    updateDownloadTracks();
                }
            }, 100);
        } else {
            downloadData.clear();
            Config.LOG(Config.TAG_DOWNLOAD, "Updating Queue", false);
            downloadData.addAll(downloadManager.getDownloadQueue());
            downloadTrackListAdapter.notifyDataSetChanged();
        }
    }

//    private void downloadFirstQueueTrack() {
//        TrackUrlDownloader trackUrlDownloader = new TrackUrlDownloader();
//        trackUrlDownloader.setMetaData(
//                this,
//                Objects.requireNonNull(getContext()).getCacheDir() + Config.TRACKS_DIR,
//                downloadData.get(0).getTrackData()
//        );
//        if (executorService == null) {
//            executorService = Executors.newSingleThreadExecutor();
//        }
//        executorService.submit(trackUrlDownloader);
//        Config.LOG(Config.TAG_DOWNLOAD, "Starting to Download track present in queue.", false);
//
//    }
//
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
            downloadTrackListAdapter.notifyDataSetChanged();
            if (downloadData.size() > 0) {
//                downloadFirstQueueTrack();
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
        Config.LOG(Config.TAG_DOWNLOAD, "Track Downloaded " + Config.convertToMB(progress), false);
        int progressPercent = (progress * 100)/CURRENT_MAX;
        downloadData.get(0).setProgress(progressPercent);
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                downloadTrackListAdapter.notifyItemChanged(0);
            }
        });
    }

    public void trackDownloaded(int id) {
        boolean work = serverizedManager.updateTrackToDownloaded(id);
        if (work) {
            serverizedManager.removeDownloadQueueTrack(id);
            downloadData.remove(0);
            Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    downloadTrackListAdapter.notifyDataSetChanged();
                }
            });
        }
        Config.LOG(Config.TAG_DOWNLOAD, "Track Downloaded To Local Dir " + work, false);
        if (downloadData.size() > 0) {
//            downloadFirstQueueTrack();
            homeToDownloadFragment.changeDownloadBtnVisibility(true);
        } else {
            homeToDownloadFragment.changeDownloadBtnVisibility(false);
        }
    }

    public void alreadyDownloaded() {
        Config.LOG(Config.TAG_DOWNLOAD, "Process the next Track to Download", false);
        onTrackCanceled(0);
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

