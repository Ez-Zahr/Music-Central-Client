package com.example.musiccommon;

parcelable SongInfo;

interface MusicCommon {
    List<SongInfo> getAllSongInfo();
    SongInfo getSongInfo(int id);
    String getSongUrl(int id);
}