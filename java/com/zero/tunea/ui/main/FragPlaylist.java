package com.zero.tunea.ui.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.zero.tunea.MainActivity;
import com.zero.tunea.R;
import com.zero.tunea.classes.Adapter;
import com.zero.tunea.classes.Const;
import com.zero.tunea.classes.Song;

import java.util.ArrayList;

public class FragPlaylist extends Fragment {

    public static Handler handler;

    private Context context;
    private ListView listView;
    private ImageView genreArt;

    ArrayList<String[]> playList;

    private  int index_old = 0;

    public  FragPlaylist(Context context){
        this.context = context;
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("HandlerLeak")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what){
                    case Const.RESTART_FRAGMENT:
                        setPlaylistView();
                        break;
                    default:
                        break;
                }
            }
        };
    }

    @Nullable
    @Override
    @SuppressLint("InflateParams")
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.list, null);
        genreArt = root.findViewById(R.id.ArtistOrAlbumArt);
        TextView empty = root.findViewById(R.id.textViewEmpty);
        listView = root.findViewById(R.id.list);
        playList = Const.database.getAllPlaylist();
        if(playList.size() > 0){
            setPlaylistView();
            listView.setVisibility(View.VISIBLE);
            empty.setVisibility(View.GONE);
        }
        return root;
    }

    private void setPlaylistView(){
        playList = Const.database.getAllPlaylist();
        Const.SHOWING_INNER_LIST_PLAYLIST = false;
        genreArt.setVisibility(View.GONE);
        BaseAdapter adapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return playList.size();
            }

            @Override
            public Object getItem(int i) {
                return null;
            }

            @Override
            public long getItemId(int i) {
                return 0;
            }

            @Override
            public View getView(int index, View view, ViewGroup viewGroup) {

                @SuppressLint("ViewHolder")
                View item = LayoutInflater.from(context).inflate(R.layout.item_view, null);
                ImageView artistArt = item.findViewById(R.id.imageView_album_of_song);
                TextView playListTitle = item.findViewById(R.id.textView_title_of_song);
                TextView songCount = item.findViewById(R.id.textView_artist_of_song);

                playListTitle.setText(playList.get(index)[0]);
                songCount.setText(playList.get(index)[1]);

                playListTitle.setSelected(true);
                songCount.setSelected(true);
                return item;
            }
        };
        listView.setOnItemLongClickListener((adapterView, view, index, l) -> {
            PopupMenu menu = new PopupMenu(context, listView);
            menu.inflate(R.menu.playlist_menu);
            menu.show();
            menu.setOnMenuItemClickListener(item -> {
                if(item.getItemId() == R.id.playlist_delete){
                    Const.database.deletePlaylist(playList.get(index)[0]);
                    handler.obtainMessage(Const.RESTART_FRAGMENT).sendToTarget();
                    return true;
                }
                return false;
            });
            return true;
        });
        listView.setOnItemClickListener((adapterView, view, i, l) -> setSongView(i));
        listView.setAdapter(adapter);
        listView.setSelection(index_old);
    }

    private void addItemToPlayList(Song song){
        MainActivity.handler.obtainMessage(Const.ADD_ITEM_TO_PLAYLIST,song).sendToTarget();
    }

    private void addItemToFavorite(Song song){
        MainActivity.handler.obtainMessage(Const.ADD_ITEM_TO_FAVORITE,song).sendToTarget();
    }

    private void removeFromPlaylist(Song song, String playlist){
        Const.database.deleteFromPlaylist(song.id, playlist);
        setSongView(index_old);
    }

    private void deleteItem(Song song){
        MainActivity.handler.obtainMessage(Const.DELETE_ITEM,song).sendToTarget();
    }

    @SuppressLint("NonConstantResourceId")
    private  void  setSongView(int index){
        index_old = index;
        Const.SHOWING_INNER_LIST_PLAYLIST = true;
        genreArt.setVisibility(View.VISIBLE);
        ArrayList<Song> innerList = Const.database.getSongsOfPlaylist(playList.get(index)[0]);
        for(Song song : innerList){
            if(song.image != null) {
                genreArt.setImageBitmap(BitmapFactory.decodeByteArray(song.image, 0, song.image.length));
                break;
            }
        }
        listView.setAdapter(new Adapter(getContext(), innerList));
        listView.setOnItemLongClickListener((adapterView, view, i, id) -> {
            Song song = innerList.get(i);
            PopupMenu menu = new PopupMenu(context, listView);
            menu.inflate(R.menu.playlist_item_menu);
            menu.show();
            menu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()){
                    case R.id.playlist_item_add_to_favorite:
                        addItemToFavorite(song);
                        break;
                    case R.id.playlist_item_add_to_playlist:
                        addItemToPlayList(song);
                        break;
                    case R.id.playlist_item_remove:
                        removeFromPlaylist(song, playList.get(index)[0]);
                        break;
                    case R.id.playlist_item_delete:
                        deleteItem(song);
                        break;
                }
                return false;
            });
            return true;
        });
        listView.setOnItemClickListener((adapterView, view, j, id) ->{
                Toast.makeText(context, ""+ j, Toast.LENGTH_SHORT).show();
        });
    }
}
