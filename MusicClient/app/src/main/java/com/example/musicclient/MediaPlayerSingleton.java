package com.example.musicclient;

import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;

public class MediaPlayerSingleton {

    private static MediaPlayer mediaPlayer = null;

    private MediaPlayerSingleton() {}

    private static void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build());
        mediaPlayer.setLooping(false);
    }

    public static void play(AssetFileDescriptor dataSource, int position) throws IOException {
        if (mediaPlayer == null) {
            initMediaPlayer();
        }

        mediaPlayer.reset();
        mediaPlayer.setDataSource(dataSource.getFileDescriptor(), dataSource.getStartOffset(),
                dataSource.getLength());
        mediaPlayer.prepare();
        mediaPlayer.seekTo(position);
        mediaPlayer.start();
    }

    public static boolean isPlaying() {
        if (mediaPlayer == null) {
            return false;
        }

        return mediaPlayer.isPlaying();
    }

    public static int getPosition() {
        if (mediaPlayer == null) {
            return -1;
        }

        return mediaPlayer.getCurrentPosition();
    }

    public static void stop() {
        if (mediaPlayer == null) {
            return;
        }

        mediaPlayer.stop();
    }

    public static void release() {
        if (mediaPlayer == null) {
            return;
        }

        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
    }
}
