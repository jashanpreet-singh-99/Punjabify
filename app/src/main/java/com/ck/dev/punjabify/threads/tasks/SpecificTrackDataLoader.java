package com.ck.dev.punjabify.threads.tasks;

import com.ck.dev.punjabify.model.ServerizedTrackData;
import com.ck.dev.punjabify.threads.interfaces.OnSpecificTrackDataFetch;
import com.ck.dev.punjabify.utils.Config;
import com.ck.dev.punjabify.utils.ServerizedManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class SpecificTrackDataLoader implements Runnable {

    private WeakReference<ServerizedManager> serverizedManagerWeakReference;
    private WeakReference<OnSpecificTrackDataFetch> onSpecificTrackDataFetchWeakReference;
    private int MODE;
    private String extra;

    private ArrayList<ServerizedTrackData> specificTracks = new ArrayList<>();

    @Override
    public void run() {
        if (serverizedManagerWeakReference != null && serverizedManagerWeakReference.get() != null) {
            switch (MODE) {
                case Config.SPECIFIC_GENRE_MODE:
                    specificTracks.clear();
                    specificTracks.addAll(serverizedManagerWeakReference.get().getGenreAllTracks(extra));
                    break;
                case Config.SPECIFIC_ARTIST_MODE:
                    specificTracks.clear();
                    specificTracks.addAll(serverizedManagerWeakReference.get().getArtistAllTracks(extra));
                    break;
                case Config.SPECIFIC_PLAYLIST_MODE:
                    specificTracks.clear();
                    specificTracks.addAll(serverizedManagerWeakReference.get().getYearSpecificTrack(extra));
                    break;
                default:
                    break;
            }
            if (onSpecificTrackDataFetchWeakReference != null && onSpecificTrackDataFetchWeakReference.get() != null) {
                onSpecificTrackDataFetchWeakReference.get().dataFetched(specificTracks);
            }
        } else {
            if (onSpecificTrackDataFetchWeakReference != null && onSpecificTrackDataFetchWeakReference.get() != null) {
                onSpecificTrackDataFetchWeakReference.get().dataFetchingError();
            }
        }
    }

    public void setMetaData(OnSpecificTrackDataFetch onSpecificTrackDataFetch, ServerizedManager serverizedManager, int MODE, String extra) {
        this.onSpecificTrackDataFetchWeakReference = new WeakReference<>(onSpecificTrackDataFetch);
        this.serverizedManagerWeakReference = new WeakReference<>(serverizedManager);
        this.MODE = MODE;
        this.extra = extra;
    }
}
