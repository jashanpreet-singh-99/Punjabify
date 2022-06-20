package com.ck.dev.punjabify.adapter;

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
import com.ck.dev.punjabify.interfaces.HomeToOnlineFragment;
import com.ck.dev.punjabify.interfaces.OnServerizedItemClick;
import com.ck.dev.punjabify.model.ServerizedTrackData;
import com.ck.dev.punjabify.utils.Config;
import com.ck.dev.punjabify.utils.GenreConfig;

import java.util.ArrayList;

public class ServerizedAllSongsListAdapter extends Adapter<ServerizedAllSongsListAdapter.ViewHolder> {

    private HomeToOnlineFragment homeToOnlineFragment;
    private ArrayList<String> data ;
    private ArrayList<Integer> rand;

    public ServerizedAllSongsListAdapter(HomeToOnlineFragment homeToOnlineFragment, ArrayList<String> data, ArrayList<Integer> rand) {
        this.homeToOnlineFragment = homeToOnlineFragment;
        this.data = data;
        this.rand = rand;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_year_hit_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.playListName.setText(data.get(position));
        holder.playListBackground.setBackgroundResource(GenreConfig.getGenreResource(GenreConfig.getGenres()[rand.get(position)]));
        holder.playListBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homeToOnlineFragment.openSpecificTrackFragment(Config.SPECIFIC_PLAYLIST_MODE, data.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private RelativeLayout playListBackground;
        private TextView       playListName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            playListBackground = itemView.findViewById(R.id.playlist_btn);
            playListName       = itemView.findViewById(R.id.playlist_name);
        }

    }

}
