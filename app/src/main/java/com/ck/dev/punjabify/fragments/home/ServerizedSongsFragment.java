package com.ck.dev.punjabify.fragments.home;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ck.dev.punjabify.R;
import com.ck.dev.punjabify.adapter.ArtistCircularListAdapter;
import com.ck.dev.punjabify.adapter.GenreRoundedListAdapter;
import com.ck.dev.punjabify.adapter.ServerizedAllSongsListAdapter;
import com.ck.dev.punjabify.interfaces.HomeToOnlineFragment;
import com.ck.dev.punjabify.interfaces.OnRecyclerItemClick;
import com.ck.dev.punjabify.interfaces.ViewPagerBackPressed;
import com.ck.dev.punjabify.utils.Config;
import com.ck.dev.punjabify.utils.GenreConfig;
import com.ck.dev.punjabify.utils.ServerizedConfig;
import com.ck.dev.punjabify.utils.ServerizedManager;

import java.util.ArrayList;
import java.util.Random;

public class ServerizedSongsFragment extends Fragment implements
        ViewPagerBackPressed {

    private RecyclerView songRecyclerView;
    private RecyclerView artistRecyclerView;
    private RecyclerView genreRecyclerView;
    private HomeToOnlineFragment homeToOnlineFragment;

    private ServerizedManager serverizedManager;

    private ArrayList<String> yearArray = new ArrayList<>();
    private ArrayList<Integer> randArray = new ArrayList<>();
    private ArrayList<String> artistArray = new ArrayList<>();
    private String[] genreArray;

    private ServerizedAllSongsListAdapter serverizedAllSongsListAdapter;
    private ArtistCircularListAdapter     artistCircularListAdapter;
    private GenreRoundedListAdapter       genreRoundedListAdapter;

    private String ARTIST = null;

    private int count = 0;

    public ServerizedSongsFragment(HomeToOnlineFragment homeToOnlineFragment) {
        this.homeToOnlineFragment = homeToOnlineFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        serverizedManager = new ServerizedManager(getContext());
        return inflater.inflate(R.layout.fragment_serverized_tracks, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        songRecyclerView   = view.findViewById(R.id.songs_list);
        artistRecyclerView = view.findViewById(R.id.artist_list);
        genreRecyclerView  = view.findViewById(R.id.genre_btn_layout_viewer);

        songRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        artistRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        genreRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        try {
            artistArray.addAll(serverizedManager.getArtistsNameOnly(ServerizedConfig.ARTIST_MODE_FOLLOWED));
        } catch (Exception e) {
            Config.LOG(Config.TAG_SPECIFIC_TRACK, "Table are updating.", true);
        }
        serverizedAllSongsListAdapter = new ServerizedAllSongsListAdapter( homeToOnlineFragment, yearArray, randArray);
        songRecyclerView.setAdapter(serverizedAllSongsListAdapter);

        artistCircularListAdapter = new ArtistCircularListAdapter(getContext(), artistArray);
        artistRecyclerView.setAdapter(artistCircularListAdapter);

        genreArray = GenreConfig.getGenres();
        genreRoundedListAdapter = new GenreRoundedListAdapter(getContext(), genreArray);
        genreRecyclerView.setAdapter(genreRoundedListAdapter);

        onClick();
        fetchYearsData();
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

        genreRecyclerView.addOnItemTouchListener(onItemTouchListener);
        artistRecyclerView.addOnItemTouchListener(onItemTouchListener);
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
        return  new ServerizedSongsFragment(homeToOnlineFragment);
    }

    @Override
    public boolean onBackPressed() {
        return true;
    }

    public void updateArtistFollowed() {
        artistArray.clear();
        artistArray.addAll(serverizedManager.getArtistsNameOnly(ServerizedConfig.ARTIST_MODE_FOLLOWED));
        artistCircularListAdapter.notifyDataSetChanged();
    }

    public void fetchYearsData() {
        yearArray.clear();
        for (String year: serverizedManager.getDistinctDates()) {
            if(year != null) {
                yearArray.add(year);
            }
            if (yearArray.size() >= 10) {
                break;
            }
            Config.LOG(Config.TAG_SPECIFIC_TRACK, "year : " + year, true);
        }
        Random random = new Random();
        randArray.clear();
        while(randArray.size() != 10) {
            int r = random.nextInt(10);
            if (!randArray.contains(r)) {
                randArray.add(r);
            }
            Config.LOG(Config.TAG_SPECIFIC_TRACK, "rand : " + r, false);
        }
        serverizedAllSongsListAdapter.notifyDataSetChanged();
    }
}
