package com.zero.tunea.classes;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Song {

    public int id;
    public String title;
    public String artist;
    public String album;
    public long albumId;
    public String genre;
    public String path;
    public long duration;
    public byte[] image;

    @Override
    public String toString()
    {
        return title;
    }

    public Bitmap albumArt(){
        if (image == null) return Const.defaultArt();
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

/*
    public Bitmap albumArt(Context context){
        if (image == null){
            return BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_white);
        }
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }
*/
}