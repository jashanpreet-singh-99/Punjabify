package com.ck.dev.punjabify.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import com.ck.dev.punjabify.R;
import com.ck.dev.punjabify.interfaces.HomeToOnlineFragment;
import com.ck.dev.punjabify.utils.Config;
import com.ck.dev.punjabify.utils.GenreConfig;

public class GenreRoundedListAdapter extends Adapter<GenreRoundedListAdapter.ViewHolder> {

    private HomeToOnlineFragment  homeToOnlineFragment;
    private String[] data ;

    public GenreRoundedListAdapter(Context context, String[] data) {
        this.homeToOnlineFragment  = (HomeToOnlineFragment) context;
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_genre_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.genreBtn.setBackgroundResource(GenreConfig.getGenreResource(data[position]));
        holder.genreBtn.setText(data[position]);
        holder.genreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homeToOnlineFragment.openSpecificTrackFragment(Config.SPECIFIC_GENRE_MODE, data[position]);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.length;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private Button genreBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            genreBtn = itemView.findViewById(R.id.genre_btn);
        }

    }

}
