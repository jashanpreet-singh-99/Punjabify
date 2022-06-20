package com.ck.dev.punjabify.threads.tasks;

import com.ck.dev.punjabify.model.ServerizedTrackData;
import com.ck.dev.punjabify.threads.ThreadConfig;
import com.ck.dev.punjabify.threads.ThreadPoolManager;
import com.ck.dev.punjabify.threads.interfaces.OnServerizedDataFetchCompleted;
import com.ck.dev.punjabify.utils.Config;
import com.ck.dev.punjabify.utils.GenreConfig;
import com.ck.dev.punjabify.utils.ServerizedManager;
import com.google.firebase.database.DataSnapshot;

import java.lang.ref.WeakReference;
import java.util.Objects;

/**
 * Convert Downloaded Data to Local usable Format
 * Take DataSnapshot (Tracks Json) as input
 */
public class ServerizedDataFetcher implements Runnable {

    private DataSnapshot dataSnapshot;
    private ServerizedManager serverizedManager;
    private String path;
    private WeakReference<OnServerizedDataFetchCompleted> onServerizedDataFetchCompletedWeakReference;

    @Override
    public void run() {
        //  Iterate over each Artist
        for (DataSnapshot data: dataSnapshot.getChildren()) {
            String artist = data.getKey();
            // Insert into All artist Database
            serverizedManager.insertArtistData(Objects.requireNonNull(artist).replace("_", " "));
            artist = Objects.requireNonNull(artist).replace(" ", "_");
            // Create Artist Specific Table
            serverizedManager.createArtistTable(artist);
            // Loop Over Each Artist's tracks
            for (DataSnapshot trackData: data.getChildren()) {
                ServerizedTrackData serverizedTrackData = new ServerizedTrackData(
                        0,
                        trackData.child("album").getValue(String.class),
                        trackData.child("artist").getValue(String.class),
                        Objects.requireNonNull(trackData.child("gedi").getValue(Integer.class)),
                        trackData.child("gender").getValue(String.class),
                        Objects.requireNonNull(trackData.child("hip hop").getValue(Integer.class)),
                        Objects.requireNonNull(trackData.child("jattism").getValue(Integer.class)),
                        Objects.requireNonNull(trackData.child("legend").getValue(Integer.class)),
                        trackData.child("link").getValue(String.class),
                        Objects.requireNonNull(trackData.child("long drive").getValue(Integer.class)),
                        Objects.requireNonNull(trackData.child("mahfil").getValue(Integer.class)),
                        Objects.requireNonNull(trackData.child("original").getValue(Integer.class)),
                        Objects.requireNonNull(trackData.child("parental").getValue(Integer.class)),
                        Objects.requireNonNull(trackData.child("party").getValue(Integer.class)),
                        Objects.requireNonNull(trackData.child("pro").getValue(Integer.class)),
                        Objects.requireNonNull(trackData.child("rap").getValue(Integer.class)),
                        trackData.child("release").getValue(String.class),
                        Objects.requireNonNull(trackData.child("romantic").getValue(Integer.class)),
                        Objects.requireNonNull(trackData.child("sad").getValue(Integer.class)),
                        trackData.getKey()
                );
                int id = serverizedManager.insertTrack(serverizedTrackData);
                insertGenreIfBelong(serverizedTrackData.getGedi(), GenreConfig.GENRE_GEDI, serverizedTrackData.getRelease(), id);
                insertGenreIfBelong(serverizedTrackData.getHipHop(), GenreConfig.GENRE_HIP_HOP, serverizedTrackData.getRelease(), id);
                insertGenreIfBelong(serverizedTrackData.getJattism(), GenreConfig.GENRE_JATTISM, serverizedTrackData.getRelease(), id);
                insertGenreIfBelong(serverizedTrackData.getLegend(), GenreConfig.GENRE_LEGEND, serverizedTrackData.getRelease(), id);
                insertGenreIfBelong(serverizedTrackData.getLongDrive(), GenreConfig.GENRE_LONG_DRIVE, serverizedTrackData.getRelease(), id);
                insertGenreIfBelong(serverizedTrackData.getMahfil(), GenreConfig.GENRE_MAHFIL, serverizedTrackData.getRelease(), id);
                insertGenreIfBelong(serverizedTrackData.getOriginal(), GenreConfig.GENRE_ORIGINAL, serverizedTrackData.getRelease(), id);
                insertGenreIfBelong(serverizedTrackData.getParental(), GenreConfig.GENRE_PARENTAL, serverizedTrackData.getRelease(), id);
                insertGenreIfBelong(serverizedTrackData.getParty(), GenreConfig.GENRE_PARTY, serverizedTrackData.getRelease(), id);
                insertGenreIfBelong(serverizedTrackData.getPro(), GenreConfig.GENRE_PRO, serverizedTrackData.getRelease(), id);
                insertGenreIfBelong(serverizedTrackData.getRap(), GenreConfig.GENRE_RAP, serverizedTrackData.getRelease(), id);
                insertGenreIfBelong(serverizedTrackData.getRomantic(), GenreConfig.GENRE_ROMANTIC, serverizedTrackData.getRelease(), id);
                insertGenreIfBelong(serverizedTrackData.getSad(), GenreConfig.GENRE_SAD, serverizedTrackData.getRelease(), id);
                serverizedManager.insertArtistTrackData(artist, serverizedTrackData.getRelease(), id);
                UpdateDownloadedTracks updateDownloadedTracks = new UpdateDownloadedTracks();
                updateDownloadedTracks.setMetaData(
                        path,
                        serverizedTrackData.getArtist(),
                        serverizedTrackData.getTitle(),
                        id,
                        serverizedManager
                );
                ThreadPoolManager.getInstance().addCallable(updateDownloadedTracks, ThreadConfig.LOCAL_TRACK_LOADER);
                Config.LOG(Config.TAG_MEDIA_ONLINE, "Added Online Track : " + serverizedTrackData.getTitle() + id, false);
            } // Tracks of one of the artist are scanned completely.
            if (onServerizedDataFetchCompletedWeakReference != null && onServerizedDataFetchCompletedWeakReference.get() != null) {
                onServerizedDataFetchCompletedWeakReference.get().singleArtistCompleted(artist);
            }
        }
        if (onServerizedDataFetchCompletedWeakReference != null && onServerizedDataFetchCompletedWeakReference.get() != null) {
            onServerizedDataFetchCompletedWeakReference.get().onDataCompleted();
        }
    }

    private void insertGenreIfBelong(int b, String genre, String release, int id) {
        if (b == 1) {
            serverizedManager.insertGenreData(genre, release, id);
        }
    }

    public void setMetaData(DataSnapshot dataSnapshot, ServerizedManager serverizedManager, String path, OnServerizedDataFetchCompleted onServerizedDataFetchCompleted) {
        this.dataSnapshot = dataSnapshot;
        this.serverizedManager = serverizedManager;
        this.path = path;
        this.onServerizedDataFetchCompletedWeakReference = new WeakReference<>(onServerizedDataFetchCompleted);
    }
}
