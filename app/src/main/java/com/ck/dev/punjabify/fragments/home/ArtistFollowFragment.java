package com.ck.dev.punjabify.fragments.home;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ck.dev.punjabify.R;
import com.ck.dev.punjabify.adapter.ArtistCircularList3HorizontalAdapter;
import com.ck.dev.punjabify.adapter.ArtistCircularListFollowedAdapter;
import com.ck.dev.punjabify.interfaces.HomeToOnlineFragment;
import com.ck.dev.punjabify.interfaces.OnArtistRecyclerViewClicked;
import com.ck.dev.punjabify.threads.ThreadConfig;
import com.ck.dev.punjabify.threads.ThreadPoolManager;
import com.ck.dev.punjabify.threads.interfaces.UiArtistImageThreadCallBack;
import com.ck.dev.punjabify.threads.tasks.ArtistImageDownloader;
import com.ck.dev.punjabify.utils.Config;
import com.ck.dev.punjabify.utils.ServerizedConfig;
import com.ck.dev.punjabify.utils.ServerizedManager;

import java.util.ArrayList;
import java.util.Objects;

public class ArtistFollowFragment extends Fragment implements OnArtistRecyclerViewClicked, UiArtistImageThreadCallBack {

    private RecyclerView artistRecyclerView;
    private RecyclerView artistFollowedRecyclerView;
    private ArtistCircularList3HorizontalAdapter artistCircularList3HorizontalAdapter;

    private Button updateFollowListBtn;

    private ArtistCircularListFollowedAdapter circularListFollowedAdapter;
    private ServerizedManager serverizedManager;

    private ArrayList<String> artistList = new ArrayList<>();
    private ArrayList<String> followedArtistList = new ArrayList<>();

    private HomeToOnlineFragment homeToOnlineFragment;

    private ThreadPoolManager threadPoolManager;

    private int count = 0;

    public ArtistFollowFragment(HomeToOnlineFragment homeToOnlineFragment) {
        this.homeToOnlineFragment = homeToOnlineFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_artist_follow, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Config.LOG(Config.TAG_HOME, "Opening Artist Follow Fragment", false);
        artistRecyclerView         = view.findViewById(R.id.artist_list);
        artistFollowedRecyclerView = view.findViewById(R.id.artist_follow_list);
        updateFollowListBtn        = view.findViewById(R.id.update_follow_list);

        serverizedManager = new ServerizedManager(getContext());

        artistCircularList3HorizontalAdapter = new ArtistCircularList3HorizontalAdapter(getContext(),this, artistList);
        artistRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        artistRecyclerView.setAdapter(artistCircularList3HorizontalAdapter);

        circularListFollowedAdapter = new ArtistCircularListFollowedAdapter(getContext(), this, followedArtistList);
        artistFollowedRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        artistFollowedRecyclerView.setAdapter(circularListFollowedAdapter);

        threadPoolManager = ThreadPoolManager.getInstance();
        onClick();
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshedDBValues();
    }

    public void refreshedDBValues() {
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                artistList.clear();
                artistList.addAll(serverizedManager.getArtistsNameOnly(ServerizedConfig.ARTIST_MODE_UN_FOLLOWED));
                artistCircularList3HorizontalAdapter.notifyDataSetChanged();
                followedArtistList.clear();
                followedArtistList.addAll(serverizedManager.getArtistsNameOnly(ServerizedConfig.ARTIST_MODE_FOLLOWED));
                circularListFollowedAdapter.notifyDataSetChanged();
                downloadArtistImages();
            }
        });
    }

    private void downloadArtistImages() {
        for (int i = 0; i < artistList.size(); i++) {
            String artistName = artistList.get(i).replace(" ", "_");
            ArtistImageDownloader artistImageDownloader = new ArtistImageDownloader();
            artistImageDownloader.setMetaData(
                    this,
                    artistName,
                    Objects.requireNonNull(getContext()).getCacheDir() + Config.IMG_DIR,
                    ServerizedConfig.ARTIST_MODE_UN_FOLLOWED
            );
            threadPoolManager.addCallable(artistImageDownloader, ThreadConfig.DOWNLOAD_ARTIST_IMAGE);
        }
        for (int i = 0; i < followedArtistList.size(); i++) {
            String artistName = followedArtistList.get(i).replace(" ", "_");
            ArtistImageDownloader artistImageDownloader = new ArtistImageDownloader();
            artistImageDownloader.setMetaData(
                    this,
                    artistName,
                    Objects.requireNonNull(getContext()).getCacheDir() + Config.IMG_DIR,
                    ServerizedConfig.ARTIST_MODE_FOLLOWED
            );
            threadPoolManager.addCallable(artistImageDownloader, ThreadConfig.DOWNLOAD_ARTIST_IMAGE);
        }
    }

    private void onClick() {
        RecyclerView.OnItemTouchListener onItemTouchListener = new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                int action = e.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        homeToOnlineFragment.changeViewpagerTouchInterceptor(false);
                        count = 0;
                        new Handler().post(runnable);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        count = 0;
                        break;
                    default:
                        homeToOnlineFragment.changeViewpagerTouchInterceptor(true);
                        break;
                }
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        };

        artistFollowedRecyclerView.addOnItemTouchListener(onItemTouchListener);

        updateFollowListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serverizedManager.resetFollowedArtist();
                for (String artist : followedArtistList) {
                    serverizedManager.updateArtistFollowed(artist);
                }
                homeToOnlineFragment.updatedArtistFollowed();
            }
        });
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (count > 20) {
                homeToOnlineFragment.changeViewpagerTouchInterceptor(true);
                return;
            }
            count++;
            new Handler().postDelayed(this, 10);
        }
    };

    public static Fragment init(HomeToOnlineFragment homeToOnlineFragment) {
        return  new ArtistFollowFragment(homeToOnlineFragment);}

    @Override
    public void onNewArtistFollowed(int pos) {
        Config.LOG(Config.TAG_ARTIST_FOLLOW, " Artist List : " + pos , false);
        try {
            followedArtistList.add(artistList.get(pos));
            circularListFollowedAdapter.notifyItemInserted(followedArtistList.size());
            artistCircularList3HorizontalAdapter.notifyItemRemoved(pos);
            artistCircularList3HorizontalAdapter.notifyItemRangeChanged(pos, artistList.size());
            artistList.remove(pos);
        } catch (IndexOutOfBoundsException e) {
            Config.LOG(Config.TAG_ARTIST_FOLLOW, "Possible reason fast double click." , true);
        }
    }

    @Override
    public void oldArtistRemoved(int pos) {
        Config.LOG(Config.TAG_ARTIST_FOLLOW, " Followed List : " + pos , false);
        try {
            artistList.add(followedArtistList.get(pos));
            artistCircularList3HorizontalAdapter.notifyItemInserted(artistList.size());
            circularListFollowedAdapter.notifyItemRemoved(pos);
            circularListFollowedAdapter.notifyItemRangeChanged(pos, followedArtistList.size());
            followedArtistList.remove(pos);
        } catch (IndexOutOfBoundsException e) {
            Config.LOG(Config.TAG_ARTIST_FOLLOW, "Possible reason fast double click." , true);
        }
    }

    @Override
    public void returnDownloadedFollowedArtist(String artist) {
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable(){
            @Override
            public void run() {
                circularListFollowedAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void returnDownloadedUnFollowedArtist(String artist) {
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable(){
            @Override
            public void run() {
                artistCircularList3HorizontalAdapter.notifyDataSetChanged();
            }
        });
    }
}