package com.example.musiccommon;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class SongInfo implements Parcelable {

    public String myLabel, myArtist, myUrl;
    public Bitmap myCover;

    public SongInfo(String label, String artist, String url, Bitmap cover) {
        myLabel = label;
        myArtist = artist;
        myUrl = url;
        myCover = cover;
    }

    protected SongInfo(Parcel in) {
        myLabel = in.readString();
        myArtist = in.readString();
        myUrl = in.readString();
        myCover = in.readParcelable(Bitmap.class.getClassLoader());
    }

    public static final Creator<SongInfo> CREATOR = new Creator<SongInfo>() {
        @Override
        public SongInfo createFromParcel(Parcel in) {
            return new SongInfo(in);
        }

        @Override
        public SongInfo[] newArray(int size) {
            return new SongInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(myLabel);
        dest.writeString(myArtist);
        dest.writeString(myUrl);
        dest.writeParcelable(myCover, flags);
    }
}
