package com.ck.dev.punjabify.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import com.ck.dev.punjabify.R;
import com.ck.dev.punjabify.interfaces.OnRecyclerItemClick;
import com.ck.dev.punjabify.model.ServerizedTrackData;

import java.util.ArrayList;

public class ApprovalSongsListAdapter extends Adapter<ApprovalSongsListAdapter.ViewHolder> {

    private OnRecyclerItemClick onRecyclerItemClick;
    private ArrayList<ServerizedTrackData> data ;

    public ApprovalSongsListAdapter(OnRecyclerItemClick onRecyclerItemClick, ArrayList<ServerizedTrackData> data) {
        this.onRecyclerItemClick = onRecyclerItemClick;
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final int pos = position;
        holder.titleTxt.setText(data.get(position).getTitle());
        holder.infoTxt.setText(data.get(position).getArtist());
        switch (data.get(pos).getOriginal()) {
            case 0:
                holder.iconImage.setBackgroundResource(R.drawable.rounded_gradient_20_red);
                break;
            case 1:
                holder.iconImage.setBackgroundResource(R.drawable.rounded_btn_20);
                break;
            case 2:
                holder.iconImage.setBackgroundResource(R.drawable.rounded_gradient_20_blast);
                break;
            default:
                holder.iconImage.setBackgroundResource(R.drawable.rounded_gradient_20_gray);
                break;
        }
        holder.iconImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRecyclerItemClick.updateData(data.get(pos));
            }
        });
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

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            iconImage = itemView.findViewById(R.id.dir_image);
            titleTxt = itemView.findViewById(R.id.dir_title);
            infoTxt = itemView.findViewById(R.id.dir_info);
            visibilityBtn = itemView.findViewById(R.id.dir_visibility_mode);
        }

    }

}
