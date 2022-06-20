package com.ck.dev.punjabify.interfaces;


import com.ck.dev.punjabify.model.ServerizedTrackData;

public interface HomeToOnlineFragment {

    void playOnlineTrack(ServerizedTrackData serverizedTrackData);
    void changeViewpagerTouchInterceptor(Boolean state);
    void openSpecificTrackFragment(int mode, String value);
    void updateOnlineQueue();
    void hideSpecificTrackFragment();
    void updatedArtistFollowed();
    void downloadTrack(ServerizedTrackData serverizedTrackData);
}
