package com.example.musicclient;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musiccommon.SongInfo;

import java.io.IOException;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private final List<SongInfo> myList;
    private final MainActivity2.onClickCallback myListener;

    public MyAdapter(List<SongInfo> list, MainActivity2.onClickCallback listener) {
        myList = list;
        myListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View movieItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_item, parent, false);
        return new ViewHolder(movieItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyAdapter.ViewHolder holder, int position) {
        holder.songCover.setImageBitmap(myList.get(position).myCover);
        holder.songLabel.setText(myList.get(position).myLabel);
        holder.songArtist.setText(myList.get(position).myArtist);
    }

    @Override
    public int getItemCount() {
        return myList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ImageView songCover;
        public TextView songLabel, songArtist;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            songCover = itemView.findViewById(R.id.song_cover);
            songLabel = itemView.findViewById(R.id.song_label);
            songArtist = itemView.findViewById(R.id.song_artist);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int song = getAdapterPosition();
            myListener.playSong(myList.get(song).myUrl);
            myListener.setSong(song);
        }
    }
}
