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
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
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

    @SuppressWarnings("rawtypes")
    BottomSheetBehavior bottomSheetBehavior;
    LinearLayout bottomSheetLayout;
    RelativeLayout mini_player;
    LinearLayout full_player;

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
            findViewById(R.id.player_container).setTranslationZ(10);
        });
        findViewById(R.id.exit_full_player).setOnClickListener(v -> findViewById(R.id.player_container).setTranslationZ(-10));

//        initBottomSheet();
    }

    @SuppressWarnings("deprecation")
    private void initBottomSheet(){
        bottomSheetLayout = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
        bottomSheetBehavior.setHideable(false);
        mini_player = findViewById(R.id.mini_player);
        full_player = findViewById(R.id.full_player);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        full_player.setVisibility(View.GONE);
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        full_player.setVisibility(View.VISIBLE);
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

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