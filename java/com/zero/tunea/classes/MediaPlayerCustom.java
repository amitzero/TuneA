package com.zero.tunea.classes;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MediaPlayerCustom{

    private Context context;
    private ArrayList<Song> SONG_LIST;
    private int CURRENT_SONG_POSITION = 0;
    private MediaPlayerListener mediaPlayerListener;
    private boolean listener = false;
    private boolean playWhenPrepared = false;
    private Timer timer;

    private AudioManager audioManager;
    private boolean duck = false;
    private boolean play = false;
    private int vol = 0;

    private MyMediaPlayer myMediaPlayer;
    @SuppressWarnings("deprecation")
    @SuppressLint("HandlerLeak")
    private final Handler handler_duration = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            Integer[] i = {myMediaPlayer.getCurrentPosition(), myMediaPlayer.getDuration()};
            if(listener) mediaPlayerListener.onPositionChange(i);
        }
    };

    public MediaPlayerCustom(Context context) {
        super();
        this.context = context;
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        myMediaPlayer = new MyMediaPlayer();
    }

    public void setPlayList(ArrayList<Song> list){
        SONG_LIST = list;
        CURRENT_SONG_POSITION = 0;
        playWhenPrepared = false;
        prePareMediaPlayer();
    }

    private void prePareMediaPlayer(){
        if(getSong() == null) return;
        try {
            myMediaPlayer.reset();
            myMediaPlayer.setDataSource(getSong().path);
            myMediaPlayer.prepare();
        } catch (IOException e) {
            if(listener) mediaPlayerListener.onError(MediaPlayer.MEDIA_ERROR_IO);
        }
    }

    public void setMediaPlayerListener(MediaPlayerListener mediaPlayerListener){
        this.mediaPlayerListener = mediaPlayerListener;
        listener = true;
    }

    private Song getSong(){
        if(SONG_LIST.size() == 0) {
            Log.e("getSong()", "ListSize is 0");
            return null;
        }
        return Const.CURRENT_SONGS_LIST.get(CURRENT_SONG_POSITION);
    }

    private void play() {
        if (getAudioFocus()) {
            playWhenPrepared = true;
            prePareMediaPlayer();
        }
    }

    public void play(int position){
        CURRENT_SONG_POSITION = position;
        play();
    }

    public void pause(){
        pauseFadeOut();
    }

/*

    private void previous() throws IOException {
        if ( SONG_LIST == null || SONG_LIST.size() == 0) return;
        CURRENT_SONG_POSITION = CURRENT_SONG_POSITION == 0 ? CURRENT_SONG_POSITION = SONG_LIST.size() - 1 : CURRENT_SONG_POSITION--;
        playWhenPrepared = true;
        myMediaPlayer.setDataSource(getSong().path);
    }

    private void next() throws IOException {
        if ( SONG_LIST == null || SONG_LIST.size() == 0) return;
        CURRENT_SONG_POSITION = CURRENT_SONG_POSITION == SONG_LIST.size() - 1 ? 0 : CURRENT_SONG_POSITION++;
        playWhenPrepared = true;
        myMediaPlayer.setDataSource(getSong().path);
    }
*/

    public void seekTo(int position){
        myMediaPlayer.seekTo(position);
    }

    public void reset(){
        myMediaPlayer.stop();
        myMediaPlayer.release();
        SONG_LIST = new ArrayList<>();
    }

    private void playFadeIn() {
        final float deviceVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) / audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        @SuppressWarnings("deprecation")
        final Handler h = new Handler();
        h.postDelayed(new Runnable() {

            int duration = 600;
            private float time = 0.0f;
            private float volume = 0.0f;

            @Override
            public void run() {
                myMediaPlayer.setVolume(volume, volume);
                if(!myMediaPlayer.isPlaying()) myMediaPlayer.start();
                time += 100;
                volume = (deviceVolume * time) / duration;
                myMediaPlayer.setVolume(volume, volume);
                if (time < duration) {
                    h.postDelayed(this, 100);
                }
            }
        }, 100);
    }

    private void pauseFadeOut() {
        final float deviceVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) / audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        @SuppressWarnings("deprecation")
        final Handler h = new Handler();
        h.postDelayed(new Runnable() {

            final int duration = 600;
            private float time = duration;

            @Override
            public void run() {
                time -= 100;
                float volume = (deviceVolume * time) / duration;
                myMediaPlayer.setVolume(volume, volume);
                if (time > 0) {
                    h.postDelayed(this, 100);
                } else {
                    myMediaPlayer.pause();
                }
            }
        }, 100);
    }

    private boolean getAudioFocus() {
        int result = 0;
        if(audioManager != null)
            result = audioManager.requestAudioFocus(myMediaPlayer, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    public interface MediaPlayerListener{

        void onStart();
        void onPause();
        void onStop();
        void onRelease();
        void onComplete();
//        void onSongChange();
        void onPositionChange(Integer[] i);
        void onError(int what);
    }

    private class MyMediaPlayer extends MediaPlayer implements AudioManager.OnAudioFocusChangeListener{

        public MyMediaPlayer(){
            setListeners();
        }

        private void setListeners(){
            this.setOnPreparedListener(mp -> {
                if(playWhenPrepared)
                    playFadeIn();
                if(timer == null){
                    timer = new Timer();
                    timer.scheduleAtFixedRate(new TimerTask() {
                        public void run() {
                            handler_duration.sendEmptyMessage(0);
                        }
                    }, 0, 200);
                }
            });
            this.setOnCompletionListener(this::onCompletion);
            this.setOnErrorListener((mp, what, extra) -> {
                if(listener)
                    mediaPlayerListener.onError(what);
                return true;
            });
        }

        private void onCompletion(MediaPlayer mp) {
            if(listener)
                mediaPlayerListener.onComplete();
        }

        @Override
        public void release() {
            super.release();
            if(listener) mediaPlayerListener.onRelease();
        }

        @Override
        public void pause() throws IllegalStateException {
            super.pause();
            if(listener) mediaPlayerListener.onPause();
        }

        @Override
        public void stop() throws IllegalStateException {
            super.stop();
            if(listener) mediaPlayerListener.onStop();
        }

        @Override
        public void start() throws IllegalStateException {
            super.start();
            if(listener) mediaPlayerListener.onStart();
        }

        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    Const.SONG_PAUSED = false;
                    if (duck) {
                        duck = false;
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0);
                    }
                    if (play) {
                        if(listener) mediaPlayerListener.onStart();
                        this.start();
                        play = false;
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    Const.SONG_PAUSED = true;
                    if(listener) mediaPlayerListener.onPause();
                    this.pause();
                    play = true;
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    duck = true;
                    vol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    audioManager.setStreamSolo(AudioManager.STREAM_MUSIC, true);
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (Math.max(vol - 2, 0)), 0);
                    break;
                default:
                    break;
            }
        }
    }
}
