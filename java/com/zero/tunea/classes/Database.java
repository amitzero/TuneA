package com.zero.tunea.classes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class Database extends SQLiteOpenHelper
{

    public final static String DATABASE_NAME = "TuneA.db";
    public final static String TABLE_NAME = "SONGS_LIST";
    public static final String ID = "ID";
    public static final String TITLE = "TITLE";
    public static final String ARTIST = "ARTIST";
    public static final String ALBUM = "ALBUM";
    public static final String ALBUM_ID = "ALBUM_ID";
    public static final String GENRE = "GENRE";
    public static final String PATH = "PATH";
    public static final String DURATION = "DURATION";
    public static final String IMAGE = "IMAGE";

    public static final String PLAYLISTS_TABLE = "PLAYLISTS_TABLE";
    public static final String PLAYLIST_NAME = "PLAYLIST_NAME";
    public static final String PLAYLIST_SONG_COUNT = "PLAYLIST_SONG_COUNT";

    public Database(Context context)
    {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String query = new StringBuilder().append("CREATE TABLE IF NOT EXISTS ").append(TABLE_NAME).append(" ( ").append(ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT, ").append(TITLE).append(" TEXT, ").append(ARTIST).append(" TEXT, ").append(ALBUM).append(" TEXT, ").append(ALBUM_ID).append(" LONG, ").append(GENRE).append(" TEXT, ").append(PATH).append(" TEXT, ").append(DURATION).append(" LONG, ").append(IMAGE).append(" BYTE[]);").toString();
        db.execSQL(query);
        String query2 = "CREATE TABLE IF NOT EXISTS " + PLAYLISTS_TABLE +
                " ( " + PLAYLIST_NAME + " TEXT, " + PLAYLIST_SONG_COUNT +
                " INTEGER);";
        db.execSQL(query2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void updateSongs()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertSongs(ContentValues cv)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.insert(TABLE_NAME, null, cv);
        return (result != -1);
    }

    public Cursor getCursorOfSongs()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }

    public boolean isEmptySongs()
    {
        Cursor cursor = getCursorOfSongs();
        int count = cursor.getCount();
        return (count < 1);
    }

    private ContentValues getSongInfo(int songID){
        ContentValues cv = new ContentValues();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + ID + "=" + songID, null);
        if(c.moveToFirst()){
            cv.put(TITLE, c.getString(c.getColumnIndex(Const.TITLE)));
            cv.put(ARTIST, c.getString(c.getColumnIndex(Const.ARTIST)));
            cv.put(ALBUM, c.getString(c.getColumnIndex(Const.ALBUM)));
            cv.put(ALBUM_ID, c.getLong(c.getColumnIndex(Const.ALBUM_ID)));
            cv.put(GENRE, c.getString(c.getColumnIndex(Const.GENRE)));
            cv.put(PATH, c.getString(c.getColumnIndex(Const.PATH)));
            cv.put(DURATION, c.getLong(c.getColumnIndex(Const.DURATION)));
            cv.put(IMAGE, c.getBlob(c.getColumnIndex(Const.IMAGE)));
        }
        c.close();
        return  cv;
    }

    public void createPlaylist(String playlistName){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(PLAYLIST_NAME, playlistName);
        cv.put(PLAYLIST_SONG_COUNT, 0);
        if(db.insert(PLAYLISTS_TABLE, null, cv) > 0){
            String query = "CREATE TABLE IF NOT EXISTS " + playlistName +
                    " ( " + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + TITLE +
                    " TEXT, " + ARTIST + " TEXT, " + ALBUM + " TEXT, " +
                    ALBUM_ID + " LONG, " + GENRE + " TEXT, " + PATH +
                    " TEXT, " + DURATION + " LONG, " + IMAGE + " BYTE[]);";
            db.execSQL(query);
        }
    }

    public int deletePlaylist(String playlistName){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + playlistName);
        return db.delete(PLAYLISTS_TABLE, PLAYLIST_NAME + "='" + playlistName + "'", null);
    }

    public long addToPlaylist(int songID, String playlist){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.query(PLAYLISTS_TABLE, new String[]{PLAYLIST_SONG_COUNT}, PLAYLIST_NAME + "='" + playlist + "'", null, null, null, null);
        int songs = 0;
        if(c.moveToFirst()){
            songs = Integer.parseInt(c.getString(c.getColumnIndex(PLAYLIST_SONG_COUNT))) + 1;
        }
        String query = "UPDATE "+ PLAYLISTS_TABLE + " SET " + PLAYLIST_SONG_COUNT + "='" + songs + "' WHERE " + PLAYLIST_NAME + "='" + playlist + "';";
        db.execSQL(query);
        return db.insert(playlist, null, getSongInfo(songID));
    }

    public int deleteFromPlaylist(int songID, String playlist){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.query(PLAYLISTS_TABLE, new String[]{PLAYLIST_SONG_COUNT}, PLAYLIST_NAME + "='" + playlist + "'", null, null, null, null);
        int songs = 0;
        if(c.moveToFirst()){
            songs = Integer.parseInt(c.getString(c.getColumnIndex(PLAYLIST_SONG_COUNT))) - 1;
        }
        String query = "UPDATE "+ PLAYLISTS_TABLE + " SET " + PLAYLIST_SONG_COUNT + "='" + songs + "' WHERE " + PLAYLIST_NAME + "='" + playlist + "';";
        db.execSQL(query);
        return db.delete(playlist, ID + "='" + songID + "'", null);
    }

    public ArrayList<String[]> getAllPlaylist(){

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + PLAYLISTS_TABLE, null);
        ArrayList<String[]> list = new ArrayList<>();
        if(c.moveToFirst())
        do {
            String playlistName = c.getString(c.getColumnIndex(PLAYLIST_NAME));
            String playlistSongCount = c.getString(c.getColumnIndex(PLAYLIST_SONG_COUNT));
            list.add(new String[]{playlistName, playlistSongCount});
        } while (c.moveToNext());
        c.close();
        return list;
    }
    public ArrayList<Song> getSongsOfPlaylist(String playlist){

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + playlist, null);
        ArrayList<Song> list = new ArrayList<>();
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
                list.add(songData);
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }
}
