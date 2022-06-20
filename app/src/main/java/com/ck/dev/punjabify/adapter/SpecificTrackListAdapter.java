package com.ck.dev.punjabify.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import com.ck.dev.punjabify.R;
import com.ck.dev.punjabify.interfaces.OnServerizedItemClick;
import com.ck.dev.punjabify.model.ServerizedTrackData;
import com.ck.dev.punjabify.threads.ThreadConfig;
import com.ck.dev.punjabify.threads.ThreadPoolManager;
import com.ck.dev.punjabify.threads.tasks.AlbumArtLoader;
import com.ck.dev.punjabify.utils.Config;

import java.util.ArrayList;

public class SpecificTrackListAdapter extends Adapter<SpecificTrackListAdapter.ViewHolder> {

    private Activity context;
    private OnServerizedItemClick onServerizedItemClick;
    private ArrayList<ServerizedTrackData> data ;
    private ArrayList<Integer> resourceBackground;

    public SpecificTrackListAdapter(Activity context, OnServerizedItemClick onServerizedItemClick, ArrayList<ServerizedTrackData> data, ArrayList<Integer> resourceBackground) {
        this.context = context;
        this.onServerizedItemClick = onServerizedItemClick;
        this.data = data;
        this.resourceBackground = resourceBackground;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.iconImage.setImageBitmap(null);
        holder.titleTxt.setText(data.get(position).getTitle());
//        if (data.get(position).getIndex() == PreferenceManager.getInt(context, PreferenceConfig.KEY_CURRENT_TRACK_INDEX_ONLINE)) {
//            holder.titleTxt.setTextColor(context.getResources().getColor(R.color.colorAccent));
//        }  else {
//            holder.titleTxt.setTextColor(context.getResources().getColor(R.color.colorTextPrimary));
//        }
        holder.infoTxt.setText(data.get(position).getArtist());
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Config.LOG(Config.TAG_MEDIA_ONLINE, "Link " + data.get(position).getLink(), false);
                onServerizedItemClick.playTrack(data.get(position));
            }
        };
        holder.iconImage.setOnClickListener(onClickListener);
        holder.parentImageBox.setOnClickListener(onClickListener);
        holder.grandParentLayout.setOnClickListener(onClickListener);
        holder.iconImage.setBackgroundResource(resourceBackground.get(0));
        AlbumArtLoader albumArtLoader = new AlbumArtLoader();
        albumArtLoader.setMetaData(
                context,
                holder.iconImage,
                context.getCacheDir() + Config.ART_DIR,
                data.get(position).getArtist(),
                data.get(position).getTitle(),
                0
        );
        ThreadPoolManager.getInstance().addCallable(albumArtLoader, ThreadConfig.IMAGE_LOAD);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageButton iconImage;
        private TextView titleTxt;
        private TextView infoTxt;
        private ImageButton visibilityBtn;
        private RelativeLayout parentImageBox;
        private LinearLayout grandParentLayout;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            iconImage = itemView.findViewById(R.id.dir_image);
            titleTxt = itemView.findViewById(R.id.dir_title);
            infoTxt = itemView.findViewById(R.id.dir_info);
            visibilityBtn = itemView.findViewById(R.id.dir_visibility_mode);
            parentImageBox    = itemView.findViewById(R.id.item_click);
            grandParentLayout = itemView.findViewById(R.id.grand_parent_lay);
        }

    }

}
