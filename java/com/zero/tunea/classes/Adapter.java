package com.zero.tunea.classes;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zero.tunea.R;

import java.util.ArrayList;

public class Adapter extends BaseAdapter {

    private Context context;
    private ArrayList<Song> songs;

    public Adapter(Context context, ArrayList<Song> songList){
        this.context = context;
        songs = songList;
    }

    @Override
    public int getCount() {
        return songs != null ? songs.size() : 0;
    }

    @Override
    public Object getItem(int i) {
        return songs != null ? songs.get(i) : null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int index, View view, ViewGroup parent) {
        @SuppressLint({"ViewHolder", "InflateParams"})
        View item = LayoutInflater.from(context).inflate(R.layout.item_view, null);
        ImageView albumArt = item.findViewById(R.id.imageView_album_of_song);
        TextView songName = item.findViewById(R.id.textView_title_of_song);
        TextView artistName = item.findViewById(R.id.textView_artist_of_song);

        byte[] imageByte = songs.get(index).image;
        if( imageByte != null && imageByte.length > 0){
            albumArt.setImageBitmap(BitmapFactory.decodeByteArray(imageByte, 0 , imageByte.length));
        }

        songName.setText(songs.get(index).title);
        artistName.setText(songs.get(index).artist);
        songName.setSelected(true);
        artistName.setSelected(true);
        return item;
    }
}
