package com.zero.tunea;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.zero.tunea.classes.Const;
import com.zero.tunea.classes.Song;
import com.zero.tunea.service.GetSongs;
import com.zero.tunea.service.SongService;
import com.zero.tunea.ui.main.FragAlbum;
import com.zero.tunea.ui.main.FragAll;
import com.zero.tunea.ui.main.FragArtist;
import com.zero.tunea.ui.main.FragGenre;
import com.zero.tunea.ui.main.FragPlaylist;
import com.zero.tunea.ui.main.SectionsPagerAdapter;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    public static Handler handler;

    Toolbar toolbar;
    ViewPager viewPager;
    SearchView searchView;
    SectionsPagerAdapter sectionsPagerAdapter;

//    Object of mini player View
    private LinearLayout player;
    private ImageView albumArt, playPrevious, playPause, playNext;
    private TextView songTitle, artist, album, genre, currentPosition, duration;
    private ProgressBar seekBar;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getReadStoragePermission();
        init();
    }

    @SuppressLint("SetTextI18n")
    private void addItemToPlaylist(Song song){
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View root = LayoutInflater.from(this).inflate(R.layout.playlist_dialog_view, null);
        ListView listView = root.findViewById(R.id.dialog_listView);
        ArrayList<String[]> playlists = Const.database.getAllPlaylist();
        listView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return playlists.size();
            }

            @Override
            public String getItem(int position) {
                return playlists.get(position)[0];
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                @SuppressLint("ViewHolder")
                View root = LayoutInflater.from(getApplicationContext()).inflate(R.layout.text_view, null);
                TextView tv = root.findViewById(R.id.textView);
                tv.setText(playlists.get(position)[0]);
                return tv;
            }
        });
        listView.setOnItemClickListener((adapterView, view, j, id) ->{
            Const.database.addToPlaylist(song.id, playlists.get(j)[0]);
            FragPlaylist.handler.obtainMessage(Const.RESTART_FRAGMENT).sendToTarget();
            dialog.dismiss();
        });
        EditText editText = root.findViewById(R.id.dialog_editText);
        TextView textView = root.findViewById(R.id.dialog_textView);
        textView.setOnClickListener(view ->{
            ((TextView)view).setText("OK");
            ((TextView)view).setTextColor(Color.GREEN);
            listView.setVisibility(View.GONE);
            editText.setVisibility(View.VISIBLE);
            view.setOnClickListener(v ->{
                String playlist = editText.getText().toString().trim();
                Const.database.createPlaylist(playlist);
                Const.database.addToPlaylist(song.id, playlist);
                FragPlaylist.handler.obtainMessage(Const.RESTART_FRAGMENT).sendToTarget();
                dialog.dismiss();
            });
        });
        dialog.setCancelable(true);
        dialog.setContentView(root);

        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private void addItemToFavorite(Song song){

    }

    private void deleteItem(Song song){

    }

    @SuppressLint("HandlerLeak")
    protected void init(){
        setContentView(R.layout.activity_main);
        //noinspection deprecation
        handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                int command = msg.what;
                switch (command){
                    case Const.START_SERVICE:
                        startService(new Intent(MainActivity.this, SongService.class));
                    case Const.SONG_LIST_CHANGE:
                    case Const.SONG_LIST_EMPTY:
                        FragAll.handler.obtainMessage(command).sendToTarget();
                        break;
                    case Const.ADD_ITEM_TO_PLAYLIST:
                        addItemToPlaylist((Song)msg.obj);
                        break;
                    case Const.ADD_ITEM_TO_FAVORITE:
                        addItemToFavorite((Song)msg.obj);
                        break;
                    case Const.DELETE_ITEM:
                        deleteItem((Song)msg.obj);
                        break;
                    default:
                        break;
                }
            }
        };
        sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        viewPager.setCurrentItem(1);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        searchView = findViewById(R.id.search_View);

        findViewById(R.id.imageViewSettings).setOnClickListener(view -> {
//                startActivity(new Intent(MainActivity.this, SettingsActivity.class));

        });

        player = findViewById(R.id.mini_player);
        albumArt = findViewById(R.id.album_art);
        playPrevious = findViewById(R.id.bt_prev);
        playPause = findViewById(R.id.bt_play_pause);
        playNext = findViewById(R.id.bt_next);
        songTitle = findViewById(R.id.title);
        artist = findViewById(R.id.artist_name);
        album = findViewById(R.id.album);
        genre = findViewById(R.id.genre);
        currentPosition = findViewById(R.id.current_position);
        duration = findViewById(R.id.duration);
        seekBar = findViewById(R.id.seekBar);
        initPlayer();
    }

    private void initPlayer(){
        if (!Const.isServiceRunning(SongService.class.getName(), this))
            return;
        if(Const.SONG_PAUSED){
            playPause.setImageResource(R.drawable.play);
        } else {
            playPause.setImageResource(R.drawable.pause);
        }
    }

    private void playerVisibility(boolean visible){

        Animation anim = AnimationUtils.loadAnimation(this, visible ? android.R.anim.fade_in : android.R.anim.fade_out);
        anim.setDuration(2000);
        player.startAnimation(anim);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                player.setVisibility(visible ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private static final String[] PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.SYSTEM_ALERT_WINDOW};
    private static final int PERMISSION_CODE = 4216;

    @SuppressLint("NewApi")
    private void getReadStoragePermission(){
        if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(PERMISSIONS,PERMISSION_CODE);
        } else if(!Settings.canDrawOverlays(this)){
            getOverlayPermission();
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(getApplicationContext(), "Read external storage permission not granted", Toast.LENGTH_SHORT).show();
            ((ActivityManager)(this.getSystemService(ACTIVITY_SERVICE))).clearApplicationUserData();
        } else {
            Intent getSongs = new Intent(this, GetSongs.class);
            startService(getSongs);
            getOverlayPermission();
        }
    }

    @SuppressLint("InlinedApi")
    private void getOverlayPermission(){
        Intent overlayPermission = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
        startActivityForResult(overlayPermission, PERMISSION_CODE);
    }

    @SuppressLint("NewApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PERMISSION_CODE && resultCode == Activity.RESULT_OK && Settings.canDrawOverlays(this)){
            Toast.makeText(getApplicationContext(), "Overlay Permission Granted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        initPlayer();
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if(viewPager.getCurrentItem() == 0 && FragPlaylist.handler != null && Const.SHOWING_INNER_LIST_PLAYLIST){
            FragPlaylist.handler.obtainMessage(Const.RESTART_FRAGMENT).sendToTarget();
        } else if (viewPager.getCurrentItem() == 2 && FragArtist.handler != null && Const.SHOWING_INNER_LIST_ARTIST){
            FragArtist.handler.obtainMessage(Const.RESTART_FRAGMENT).sendToTarget();
        } else if (viewPager.getCurrentItem() == 3 && FragAlbum.handler != null && Const.SHOWING_INNER_LIST_ALBUM){
            FragAlbum.handler.obtainMessage(Const.RESTART_FRAGMENT).sendToTarget();
        } else if (viewPager.getCurrentItem() == 4 && FragGenre.handler != null && Const.SHOWING_INNER_LIST_GENRE){
            FragGenre.handler.obtainMessage(Const.RESTART_FRAGMENT).sendToTarget();
        } else {
            finishAffinity();
        }
    }
}