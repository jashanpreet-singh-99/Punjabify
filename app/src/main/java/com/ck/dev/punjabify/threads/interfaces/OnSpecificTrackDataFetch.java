package com.ck.dev.punjabify.threads.interfaces;

import com.ck.dev.punjabify.model.ServerizedTrackData;

import java.util.ArrayList;

public interface OnSpecificTrackDataFetch {
    void dataFetched(ArrayList<ServerizedTrackData> serverizedTrackData);
    void dataFetchingError();
}
