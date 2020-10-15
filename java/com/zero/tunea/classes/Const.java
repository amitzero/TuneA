package com.zero.tunea.classes;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import com.zero.tunea.MainActivity;
import com.zero.tunea.R;
import com.zero.tunea.service.SongService;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Const {

    @SuppressLint("StaticFieldLeak")
    private static Context context;
    public static Handler handler;

    //public static final int FRAG_PLAYLIST = 1;
    //public static final int FRAG_ALL = 2;
    //public static final int FRAG_ARTIST = 3;
    public static final int ADD_ITEM_TO_PLAYLIST = 4;
    public static final int ADD_ITEM_TO_FAVORITE = 5;
    public static final int DELETE_ITEM = 6;
    public static final int RESTART_FRAGMENT = 7;

    public static boolean PLAY_WHEN_START = false;
    public static boolean SHOWING_INNER_LIST_PLAYLIST = false;
    public static boolean SHOWING_INNER_LIST_ARTIST = false;
    public static boolean SHOWING_INNER_LIST_ALBUM = false;
    public static boolean SHOWING_INNER_LIST_GENRE = false;

    //public final static String DATABASE_NAME = "song_list.db";
    //public final static String TABLE_NAME = "SONGS_LIST";
    public static final String ID = "ID";
    public static final String TITLE = "TITLE";
    public static final String ARTIST = "ARTIST";
    public static final String ALBUM = "ALBUM";
    public static final String ALBUM_ID = "ALBUM_ID";
    public static final String GENRE = "GENRE";
    public static final String PATH = "PATH";
    public static final String DURATION = "DURATION";
    public static final String IMAGE = "IMAGE";

    public static final String ACTION_PLAY = "zero.tunea.play";
    public static final String ACTION_PLAY_PAUSE = "zero.tunea.play_pause";
    public static final String ACTION_PREVIOUS = "zero.tunea.previous";
    public static final String ACTION_NEXT = "zero.tunea.next";

    public static Database database = null;

    public static final int SONG_LIST_CHANGE = 8;
    public static final int SONG_LIST_EMPTY = 9;

    public static ArrayList<Song> CURRENT_SONGS_LIST = new ArrayList<>();
    public static ArrayList<Song> ALL_SONGS_LIST = new ArrayList<>();
    public static int CURRENT_SONG_NUMBER = 0;
    public static boolean SONG_PAUSED = true;
    public static final int SONG_CHANGED = 10;
    public static final int PLAY_PAUSE = 11;
    public static final int PLAY = 12;
    public static final int PAUSE = 13;
    public static final int PLAY_PREVIOUS = 14;
    public static final int PLAY_NEXT = 15;
    public static final int SEEK_SONG = 16;
    public static final int PROGRESS = 17;
    public static final int REFRESH_UI = 18;
    public static final int REFRESH_METADATA = 19;
    public static final int START_SERVICE = 20;

    public static void intense(Context context){
        Const.context = context;
        handler = getHandler();
    }

    public static void message(int what){
        if(handler == null) handler = getHandler();
        handler.obtainMessage(what).sendToTarget();
    }
    public static void message(int what, Object object){
        if(handler == null) handler = getHandler();
        handler.obtainMessage(what, object).sendToTarget();
    }
    @SuppressWarnings("deprecation")
    @SuppressLint("HandlerLeak")
    private static Handler getHandler(){
        return new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case REFRESH_UI:
                        refreshUI();
                        break;
                    case REFRESH_METADATA:
                        refreshMetadata();
                        break;
                    case PLAY_PREVIOUS:
                        playPrevious((Context) msg.obj);
                        break;
                    case PLAY_PAUSE:
                        playPause((Context) msg.obj);
                        break;
                    case PLAY_NEXT:
                        playNext((Context) msg.obj);
                    case PROGRESS:

                }
            }
        };
    }

    public static Song getCurrentSong(){
        return ALL_SONGS_LIST.get(CURRENT_SONG_NUMBER);
    }

    public static void next(){
        if(ALL_SONGS_LIST == null || ALL_SONGS_LIST.size() > 2) return;
        CURRENT_SONG_NUMBER = CURRENT_SONG_NUMBER == ALL_SONGS_LIST.size() - 1 ? 0 : CURRENT_SONG_NUMBER++;
    }

    public static  void previous(){
        if(ALL_SONGS_LIST == null || ALL_SONGS_LIST.size() > 2) return;
        CURRENT_SONG_NUMBER = CURRENT_SONG_NUMBER == 0 ? ALL_SONGS_LIST.size() - 1 : CURRENT_SONG_NUMBER--;
    }

    public static void playPrevious(Context context){
        if(isServiceRunning(SongService.class.getName(), context) && ALL_SONGS_LIST.size() > 0){
            CURRENT_SONG_NUMBER = ((CURRENT_SONG_NUMBER > 0) ? CURRENT_SONG_NUMBER-- : ALL_SONGS_LIST.size() - 1);
            SongService.handler.obtainMessage(SONG_CHANGED).sendToTarget();
            SONG_PAUSED = false;
        }
    }

    public static void playPause(Context context)
    {
        if(!isServiceRunning(SongService.class.getName(), context) && ALL_SONGS_LIST.size() > 0){
            MainActivity.handler.obtainMessage(START_SERVICE, context);
        }
        SongService.handler.obtainMessage(Const.PLAY_PAUSE).sendToTarget();
    }

    public static void playNext(Context context) {
        if(!isServiceRunning(SongService.class.getName(), context) && ALL_SONGS_LIST.size() > 0){
            CURRENT_SONG_NUMBER = ((CURRENT_SONG_NUMBER < (ALL_SONGS_LIST.size() - 1)) ? CURRENT_SONG_NUMBER++ : 0);
            SongService.handler.obtainMessage(SONG_CHANGED).sendToTarget();
            SONG_PAUSED = false;
        }
    }


    private static void refreshUI()
    {
        //MainActivity.updateState();
        //AudioPlayerActivity.updateState();
        //FloatingViewService.updateState();
    }

    private static void refreshMetadata()
    {
        //MainActivity.updateInfo();
        //AudioPlayerActivity.updateInfo();
        //FloatingViewService.updateInfo();
    }

    public static boolean currentVersionSupportBigNotification() {
        int sdkVersion = Build.VERSION.SDK_INT;
        return sdkVersion >= Build.VERSION_CODES.JELLY_BEAN;
    }

    public static boolean currentVersionSupportLockScreenControls() {
        int sdkVersion = Build.VERSION.SDK_INT;
        return sdkVersion >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    public static boolean isServiceRunning(String serviceName, Context context)
    {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if (serviceName.equals(service.service.getClassName()))
            {
                return true;
            }
        }
        return false;
    }

    public static Bitmap defaultArt(){
        return BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_white);
    }

    @SuppressLint("DefaultLocale")
    public static String duration(long milliseconds)
    {
        /*
        long sec = (milliseconds / 1000) % 60;
        long min = (milliseconds / (60 * 1000)) % 60;
        long hour = milliseconds / (60 * 60 * 1000);

        String s = (sec < 10) ? "0" + sec : "" + sec;
        String m = (min < 10) ? "0" + min : "" + min;
        String h = "" + hour;

        String time = "";
        if (hour > 0)
        {
            time = h + ":" + m + ":" + s;
        }
        else
        {
            time = m + ":" + s;
        }
         */

        return String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(milliseconds), TimeUnit.MILLISECONDS.toSeconds(milliseconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)) );
    }

}
