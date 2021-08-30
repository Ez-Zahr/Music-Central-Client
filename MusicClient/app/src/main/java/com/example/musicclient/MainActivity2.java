package com.example.musicclient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.musiccommon.SongInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity2 extends AppCompatActivity {

    interface onClickCallback {
        void setSong(int song);
        void playSong(String song);
    }

    private List<SongInfo> myList;
    private int songPlaying = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        // Retrieve the list from the holder class. If no list is passed, create an empty one
        myList = MyListHolder.myList;
        MyListHolder.myList = null;
        if (myList == null) {
            myList = new ArrayList<>();
        }

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setAdapter(new MyAdapter(myList, new onClickCallback() {
            @Override
            public void setSong(int song) {
                songPlaying = song;
            }

            @Override
            public void playSong(String song) {
                try {
                    MediaPlayerSingleton.play(getAssets().openFd("Songs/" + song), 0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        if (savedInstanceState != null && savedInstanceState.getBoolean("isPlaying")) {
            try {
                songPlaying = savedInstanceState.getInt("songPlaying");
                MediaPlayerSingleton.play(getAssets().openFd(myList.get(songPlaying).myUrl),
                        savedInstanceState.getInt("curPos"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        MyListHolder.myList = myList;
        outState.putBoolean("isPlaying", MediaPlayerSingleton.isPlaying());
        outState.putInt("songPlaying", songPlaying);
        outState.putInt("curPos", MediaPlayerSingleton.getPosition());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (MediaPlayerSingleton.isPlaying()) {
            MediaPlayerSingleton.release();
        }
    }
}