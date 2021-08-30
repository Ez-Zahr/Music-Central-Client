package com.example.musicclient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.musiccommon.MusicCommon;
import com.example.musiccommon.SongInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MusicCommon musicCommonService;
    private boolean isBound = false;
    private int listSize;
    private int songSelected, songPlaying = -1, curPos;
    private TextView serviceStatus;
    private EditText editText;
    private Spinner songsMenu;
    private Button bindButton, unbindButton, songButton, songsButton, playButton;

    private final ServiceConnection myConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicCommonService = MusicCommon.Stub.asInterface(service);
            isBound = true;

            try {
                // Initialize the spinner with all songs
                List<SongInfo> myList = musicCommonService.getAllSongInfo();
                listSize = myList.size();
                List<String> songsList = new ArrayList<>();
                for (int i = 0; i < myList.size(); i++) {
                    songsList.add((i+1) + ". " + myList.get(i).myLabel);
                }
                songsMenu.setAdapter(new ArrayAdapter<>(getApplicationContext(),
                        R.layout.support_simple_spinner_dropdown_item, songsList));

                // Restore the MediaPlayer's state if it was playing before configuration change
                if (songPlaying != -1) {
                    MediaPlayerSingleton.play(getAssets().openFd("Songs/" +
                                    myList.get(songPlaying).myUrl), curPos);
                    playButton.setText(R.string.stop);
                }
            } catch (RemoteException | IOException e) {
                Log.i("Tag", "Failed in onServiceConnected()");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicCommonService = null;
            MediaPlayerSingleton.release();
            toggleButtons();
            playButton.setText(R.string.play);
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        serviceStatus = findViewById(R.id.service_status);
        serviceStatus.setText(R.string.statusOff);

        editText = findViewById(R.id.edit_text);

        songsMenu = findViewById(R.id.spinner);
        songsMenu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                songSelected = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        bindButton = findViewById(R.id.bind_button);
        unbindButton = findViewById(R.id.unbind_button);
        songButton = findViewById(R.id.song_button);
        songsButton = findViewById(R.id.songs_button);
        playButton = findViewById(R.id.playButton);

        bindButton.setOnClickListener(v -> {
            if (!isBound) {
                bind();
            }
        });

        unbindButton.setOnClickListener(v -> {
            if (isBound) {
                unbindService(myConnection);
                MediaPlayerSingleton.release();
                playButton.setText(R.string.play);
                serviceStatus.setText(R.string.statusOff);
                toggleButtons();
                isBound = false;
            }
        });

        songButton.setOnClickListener(v -> {
            int n;
            try {
                n = Integer.parseInt((editText).getText().toString()) - 1;
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show();
                return;
            }

            if (n >= 0 && n < listSize) {
                try {
                    // Get the requested song, put it in a List, and pass it to MainActivity2
                    ArrayList<SongInfo> list = new ArrayList<>();
                    list.add(musicCommonService.getSongInfo(n));
                    startActivityTwo(list);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "Invalid song ID", Toast.LENGTH_SHORT).show();
            }
        });

        songsButton.setOnClickListener(v -> {
            try {
                // Get a List of all songs and pass it to MainActivity2
                startActivityTwo(musicCommonService.getAllSongInfo());
            } catch (RemoteException e) {
                Log.i("Tag", "Failed to get all songs");
            }
        });

        playButton.setOnClickListener(v -> {
            // Play the selected song from the spinner, or stop the MediaPlayer if a song is playing
            if (MediaPlayerSingleton.isPlaying()) {
                MediaPlayerSingleton.stop();
                playButton.setText(R.string.play);
                songPlaying = -1;
            } else {
                try {
                    MediaPlayerSingleton.play(getAssets().openFd("Songs/" +
                                    musicCommonService.getSongUrl(songSelected)), 0);
                    playButton.setText(R.string.stop);
                    songPlaying = songSelected;
                } catch (IOException | RemoteException e) {
                    Log.i("Tag", "Failed to play song");
                }
            }
        });

        if (savedInstanceState != null && savedInstanceState.getBoolean("isBound")) {
            bind();
            songPlaying = savedInstanceState.getInt("songPlaying");
            curPos = savedInstanceState.getInt("curPos");
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isBound", isBound);
        outState.putInt("songPlaying", songPlaying);
        outState.putInt("curPos", MediaPlayerSingleton.getPosition());
    }

    private void bind() {
        Intent intent = new Intent(MusicCommon.class.getName());
        ResolveInfo info = getPackageManager().resolveService(intent, 0);
        intent.setComponent(new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name));

        if (bindService(intent, myConnection, BIND_AUTO_CREATE)) {
            serviceStatus.setText(R.string.statusOn);
            toggleButtons();
        }
    }

    private void startActivityTwo(List<SongInfo> list) {
        // Stop the MediaPlayer before going to MainActivity2
        if (MediaPlayerSingleton.isPlaying()) {
            MediaPlayerSingleton.stop();
            playButton.setText(R.string.play);
        }

        Intent intent = new Intent(MainActivity.this, MainActivity2.class);
        // Pass the list through a static field in a wrapper class, because of the list's big size.
        // Cannot be passed via an Intent
        MyListHolder.myList = list;
        startActivity(intent);
    }

    private void toggleButtons() {
        // Enable/Disable buttons based on the service connection status
        bindButton.setEnabled(isBound);
        editText.setEnabled(!isBound);
        unbindButton.setEnabled(!isBound);
        songButton.setEnabled(!isBound);
        songsButton.setEnabled(!isBound);
        playButton.setEnabled(!isBound);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (isBound) {
            unbindService(myConnection);
            MediaPlayerSingleton.release();
            playButton.setText(R.string.play);
            toggleButtons();
            isBound = false;
        }
    }
}