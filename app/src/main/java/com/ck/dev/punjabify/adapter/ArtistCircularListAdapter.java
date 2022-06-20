package com.ck.dev.punjabify.adapter;

import android.content.Context;
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
import com.ck.dev.punjabify.interfaces.HomeToOnlineFragment;
import com.ck.dev.punjabify.tasks.LoadLocalImage;
import com.ck.dev.punjabify.utils.Config;

import java.util.ArrayList;

public class ArtistCircularListAdapter extends Adapter<ArtistCircularListAdapter.ViewHolder> {

    private Context context;
    private HomeToOnlineFragment  homeToOnlineFragment;
    private ArrayList<String> data ;

    public ArtistCircularListAdapter(Context context, ArrayList<String> data) {
        this.context = context;
        this.homeToOnlineFragment  = (HomeToOnlineFragment) context;
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_artist_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        new LoadLocalImage(context, holder.artistBtn, holder.artistProgressBar).execute(data.get(position));
        holder.artistNameTxt.setText(data.get(position));
        holder.artistBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homeToOnlineFragment.openSpecificTrackFragment(Config.SPECIFIC_ARTIST_MODE, data.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageButton artistBtn;
        private ProgressBar artistProgressBar;
        private TextView    artistNameTxt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            artistBtn         = itemView.findViewById(R.id.artist_image_btn);
            artistProgressBar = itemView.findViewById(R.id.artist_loading_bar);
            artistNameTxt     = itemView.findViewById(R.id.artist_name_txt);
        }
    }

}
