package com.zero.tunea.ui.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;

import com.zero.tunea.MainActivity;
import com.zero.tunea.R;
import com.zero.tunea.classes.Adapter;
import com.zero.tunea.classes.Const;
import com.zero.tunea.classes.Song;

public class FragAll extends Fragment {

    public static Handler handler;
    private Context context;

    private TextView empty;
    private ListView listView;

    public FragAll(Context context){
        this.context = context;
    }

    @SuppressLint("HandlerLeak")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //noinspection deprecation
        handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                int command = msg.what;
                switch (command){
                    case Const.SONG_LIST_CHANGE:
                        initSongs();
                        break;
                    case Const.SONG_LIST_EMPTY:
                        empty.setText(R.string.EMPTY);
                        break;
                }
            }
        };
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        @SuppressLint("InflateParams")
        View root = inflater.inflate(R.layout.list, null);
        empty = root.findViewById(R.id.textViewEmpty);
        empty.setText(R.string.LOADING);
        listView = root.findViewById(R.id.list);
        if(Const.CURRENT_SONGS_LIST.size() > 0){
            initSongs();
        }
        return root;
    }

    private void playItem(int index){
        Const.PLAY_WHEN_START = true;
        Const.SONG_PAUSED = false;
        Const.CURRENT_SONG_NUMBER = index;
        MainActivity.handler.obtainMessage(Const.START_SERVICE).sendToTarget();
    }

    private void addItemToPlayList(int index){
        Song song = Const.CURRENT_SONGS_LIST.get(index);
        MainActivity.handler.obtainMessage(Const.ADD_ITEM_TO_PLAYLIST,song).sendToTarget();
    }

    private void addItemToFavorite(int index){
        Song song = Const.CURRENT_SONGS_LIST.get(index);
        MainActivity.handler.obtainMessage(Const.ADD_ITEM_TO_FAVORITE,song).sendToTarget();
    }

    private void deleteItem(int index){
        Song song = Const.CURRENT_SONGS_LIST.get(index);
        MainActivity.handler.obtainMessage(Const.DELETE_ITEM,song).sendToTarget();
    }

    private void initSongs(){
        empty.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);
        listView.setAdapter(new Adapter(context, Const.CURRENT_SONGS_LIST));
        listView.setOnItemClickListener((adapterView, view, index, id) -> playItem(index));
        listView.setOnItemLongClickListener((adapterView, view, i, l) -> {

            PopupMenu popup = new PopupMenu(context, view);
            popup.getMenuInflater().inflate(R.menu.more_menu, popup.getMenu());
            popup.show();

            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId())
                {
                    case R.id.popup_play:
                        playItem(i);
                        break;
                    case R.id.popup_addToPlaylist:
                        addItemToPlayList(i);
                        break;
                    case R.id.popup_addToFavorite:
                        addItemToFavorite(i);
                        break;
                    case R.id.popup_delete:
                        deleteItem(i);
                        break;
                    default:
                        break;
                }
                return true;
            });
            //End
            return true;
        });
    }

}
