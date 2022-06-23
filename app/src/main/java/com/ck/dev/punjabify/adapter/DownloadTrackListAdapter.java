package com.ck.dev.punjabify.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import com.ck.dev.punjabify.R;
import com.ck.dev.punjabify.interfaces.OnDownloadQueueClicks;
import com.ck.dev.punjabify.interfaces.OnRecyclerItemClick;
import com.ck.dev.punjabify.model.DownloadData;
import com.ck.dev.punjabify.threads.ThreadConfig;
import com.ck.dev.punjabify.threads.ThreadPoolManager;
import com.ck.dev.punjabify.threads.tasks.AlbumArtLoader;
import com.ck.dev.punjabify.utils.Config;
import com.ck.dev.punjabify.view.CircularProgress;

import java.util.ArrayList;
import java.util.Locale;

public class DownloadTrackListAdapter extends Adapter<DownloadTrackListAdapter.ViewHolder> {

    private Activity context;
    private ArrayList<DownloadData> data ;
    private OnDownloadQueueClicks onDownloadQueueClicks;
    private int mode = 2;

    public DownloadTrackListAdapter(Activity context, OnDownloadQueueClicks onDownloadQueueClicks, ArrayList<DownloadData> data) {
        this.context = context;
        this.onDownloadQueueClicks = onDownloadQueueClicks;
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_download_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.titleTrackTxt.setText(data.get(position).getTrackData().getTitle());
        mode = 0;
        holder.albumArt.setBackgroundResource(R.color.colorAccent);
        AlbumArtLoader albumArtLoader = new AlbumArtLoader();
        albumArtLoader.setMetaData(
                context,
                holder.albumArt,
                context.getCacheDir() + Config.ART_DIR,
                data.get(position).getTrackData().getArtist(),
                data.get(position).getTrackData().getTitle(),
                mode
        );
        ThreadPoolManager.getInstance().addCallable(albumArtLoader, ThreadConfig.IMAGE_LOAD);
        holder.cancelBtn.setOnClickListener(v -> onDownloadQueueClicks.onTrackCanceled(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView    titleTrackTxt;
        private final ImageButton cancelBtn;
        private final ImageButton albumArt;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTrackTxt = itemView.findViewById(R.id.track_title);
            cancelBtn     = itemView.findViewById(R.id.cancel_btn);
            albumArt      = itemView.findViewById(R.id.album_art_btn);
        }

    }

}
