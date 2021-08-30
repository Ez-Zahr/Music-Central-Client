package com.example.musiccentral;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.musiccommon.MusicCommon;
import com.example.musiccommon.SongInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MusicCentral extends Service {

    private List<SongInfo> myList;

    private final MusicCommon.Stub myBinder = new MusicCommon.Stub() {
        @Override
        public synchronized List<SongInfo> getAllSongInfo() {
            return myList;
        }

        @Override
        public synchronized SongInfo getSongInfo(int id) {
            return myList.get(id);
        }

        @Override
        public synchronized String getSongUrl(int id) {
            return myList.get(id).myUrl;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("Songs.txt")));
            myList = new ArrayList<>();
            String line = reader.readLine();
            while (line != null) {
                String[] values = line.split(",");
                myList.add(new SongInfo(values[0], values[1], values[3],
                        BitmapFactory.decodeStream(getAssets().open("Covers/" + values[2]))));
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            Log.i("Tag", "Failed to read input file");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("channelID",
                    "Music player notification", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Music player's channel");
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(this, "channelID")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setOngoing(true)
                .setContentTitle("Playing Music")
                .setContentText("No content available")
                .setTicker("Music is playing...")
                .build();

        startForeground(1, notification);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }
}