package com.ck.dev.punjabify.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.ck.dev.punjabify.fragments.ErrorFragment;
import com.ck.dev.punjabify.fragments.home.ArtistFollowFragment;
import com.ck.dev.punjabify.fragments.home.ServerizedSongsFragment;
import com.ck.dev.punjabify.fragments.home.SongsApprovalFragment;
import com.ck.dev.punjabify.interfaces.HomeToOnlineFragment;

import java.util.ArrayList;

public class HomeScreenAdapter extends FragmentStateAdapter {

    private ArrayList<Fragment> allFragments = new ArrayList<>();

    private HomeToOnlineFragment homeToOnlineFragment;

    public HomeScreenAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        this.homeToOnlineFragment = (HomeToOnlineFragment) fragmentActivity;
    }

    public static final int SERVERIZED_TRACK_FRAGMENT = 0;
    public static final int ARTIST_FOLLOW_FRAGMENT    = 1;
    public static final int DIR_LOCAL_TRACK_FRAGMENT  = 2;

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment curFragment;
        switch (position) {
            case SERVERIZED_TRACK_FRAGMENT : {
                curFragment = ServerizedSongsFragment.init(homeToOnlineFragment);
                allFragments.add(curFragment);
                return curFragment;
            }
            case ARTIST_FOLLOW_FRAGMENT : {
                curFragment = ArtistFollowFragment.init(homeToOnlineFragment);
                allFragments.add(curFragment);
                return curFragment;
            }
            case DIR_LOCAL_TRACK_FRAGMENT : {
                curFragment = SongsApprovalFragment.init(homeToOnlineFragment);
                allFragments.add(curFragment);
                return curFragment;
            }
            default : {
                curFragment = ErrorFragment.init();
                return curFragment;
            }
        }
    }

    public Fragment getCurFragment(int pos) {
        return allFragments.get(pos);
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
