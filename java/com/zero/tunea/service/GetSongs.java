package com.zero.tunea.service;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import androidx.annotation.RequiresApi;

import com.zero.tunea.MainActivity;
import com.zero.tunea.R;
import com.zero.tunea.classes.Const;
import com.zero.tunea.classes.Database;
import com.zero.tunea.classes.Song;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.util.ArrayList;

@SuppressWarnings("ALL")
public class GetSongs extends IntentService {

    Context context;

    @SuppressWarnings("deprecation")
    public GetSongs() {
        super("GettingSongsIntentService");
    }

    @SuppressLint("NewApi")
    @Override
    protected void onHandleIntent(Intent intent){
        this.context = getApplicationContext();
        Const.database = new Database(context);
        if(Const.database.isEmptySongs()){
            addSongs();
        } else {
            getSongsFromDatabase();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void addSongs()
    {
        ArrayList<Song> listOfSongs = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String[] columns = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID
        };

        Cursor c = getApplicationContext().getContentResolver().query(
                uri,
                columns,
                MediaStore.Audio.Media.IS_MUSIC + "!='0'",
                null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

        if(c != null && c.moveToFirst()) {
            do {
                Song songData = new Song();

                String title = c.getString(c.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String artist = c.getString(c.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String album = c.getString(c.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                long duration = c.getLong(c.getColumnIndex(MediaStore.Audio.Media.DURATION));
                String data = c.getString(c.getColumnIndex(MediaStore.Audio.Media.DATA));
                long albumId = c.getLong(c.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));

                MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
                String genre;
                byte[] image;

                metaRetriever.setDataSource(data);
                genre = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);
                image = metaRetriever.getEmbeddedPicture();

//            if (image == null) image = getAlbumart(albumId);

                songData.setTitle(title);
                songData.setArtist(artist);
                songData.setAlbum(album);
                songData.setAlbumId(albumId);
                songData.setGenre(genre);
                songData.setPath(data);
                songData.setDuration(duration);
                songData.setImageByte(image);
                listOfSongs.add(songData);
            }
            while (c.moveToNext());
            c.close();
        }

        if (listOfSongs.size() == 0 && MainActivity.handler != null){
            MainActivity.handler.obtainMessage(Const.SONG_LIST_EMPTY).sendToTarget();
        }
        if(listOfSongs.size() != Const.ALL_SONGS_LIST.size()){
            Const.ALL_SONGS_LIST.clear();
            Const.ALL_SONGS_LIST = listOfSongs;
            if(MainActivity.handler != null){
                MainActivity.handler.obtainMessage(Const.SONG_LIST_CHANGE).sendToTarget();
            }
            Const.database.updateSongs();
            addSongsToDataBase();
        }
    }

    private byte[] getAlbumart(Long album_id)
    {
        byte[] imageArray = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        try
        {
            final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
            Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
            ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
            if (pfd != null)
            {
                FileDescriptor fd = pfd.getFileDescriptor();
                Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fd, null, options);

                if (bitmap == null)
                    bitmap = getDefaultAlbumArt();

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                imageArray = stream.toByteArray();
            }
        }
        catch (Exception ignored)
        {}
        return imageArray;
    }

    private Bitmap getDefaultAlbumArt()
    {
        Bitmap bm = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        try
        {
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon, options);
        }
        catch (Exception ignored)
        {}
        return bm;
    }

    @SuppressLint("NewApi")
    private void getSongsFromDatabase()
    {
        Cursor c = Const.database.getCursorOfSongs();
        ArrayList<Song> listOfSongs = new ArrayList<>();
        if(c.moveToFirst()) {
            do {
                Song songData = new Song();
                int id = c.getInt(c.getColumnIndex(Const.ID));
                String title = c.getString(c.getColumnIndex(Const.TITLE));
                String artist = c.getString(c.getColumnIndex(Const.ARTIST));
                String album = c.getString(c.getColumnIndex(Const.ALBUM));
                long album_id = c.getLong(c.getColumnIndex(Const.ALBUM_ID));
                String genre = c.getString(c.getColumnIndex(Const.GENRE));
                String path = c.getString(c.getColumnIndex(Const.PATH));
                long duration = c.getLong(c.getColumnIndex(Const.DURATION));
                byte[] image = c.getBlob(c.getColumnIndex(Const.IMAGE));

                songData.id = id;
                songData.setTitle(title);
                songData.setArtist(artist);
                songData.setAlbum(album);
                songData.setAlbumId(album_id);
                songData.setGenre(genre);
                songData.setPath(path);
                songData.setDuration(duration);
                songData.setImageByte(image);
                listOfSongs.add(songData);
            } while (c.moveToNext());
        }
        c.close();
        Const.ALL_SONGS_LIST.clear();
        Const.ALL_SONGS_LIST = listOfSongs;

        if (listOfSongs.size() < 1 && MainActivity.handler != null){
            MainActivity.handler.obtainMessage(Const.SONG_LIST_EMPTY).sendToTarget();
        } else if(MainActivity.handler != null){
            MainActivity.handler.obtainMessage(Const.SONG_LIST_CHANGE).sendToTarget();
        }
        addSongs();
    }

    private void addSongsToDataBase(){

        for(Song song : Const.ALL_SONGS_LIST){
            ContentValues songData = new ContentValues();
            songData.put(Const.TITLE, song.title);
            songData.put(Const.ARTIST, song.artist);
            songData.put(Const.ALBUM, song.album);
            songData.put(Const.ALBUM_ID, song.albumId);
            songData.put(Const.GENRE, song.genre);
            songData.put(Const.PATH, song.path);
            songData.put(Const.DURATION, song.duration);
            songData.put(Const.IMAGE, song.image);
            Const.database.insertSongs(songData);
        }
    }
}
