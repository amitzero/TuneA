package com.zero.tunea.ui.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.zero.tunea.R;
import com.zero.tunea.classes.Adapter;
import com.zero.tunea.classes.Const;
import com.zero.tunea.classes.Song;

import java.util.ArrayList;

@SuppressWarnings("ALL")
public class FragGenre extends Fragment {

    public static Handler handler;

    private Context context;
    private ListView listView;
    private ImageView genreArt;

    ArrayList<String[]> list;

    private  int index_old = 0;

    public  FragGenre(Context context){
        this.context = context;
        list = getGenre();
    }

    @SuppressLint("HandlerLeak")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                int command = msg.what;
                switch (command){
                    case Const.RESTART_FRAGMENT:
                        setGenreView();
                        break;
                    default:
                        break;
                }
            }
        };
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.list, null);
        genreArt = root.findViewById(R.id.ArtistOrAlbumArt);
        TextView empty = root.findViewById(R.id.textViewEmpty);
        if(list.size() > 0) {
            empty.setVisibility(View.GONE);
            listView = root.findViewById(R.id.list);
            listView.setVisibility(View.VISIBLE);
            setGenreView();
        }
        return root;
    }


    private void setGenreView(){
        Const.SHOWING_INNER_LIST_GENRE = false;
        genreArt.setVisibility(View.GONE);
        BaseAdapter adapter = new BaseAdapter() {
            @Override
            public int getCount() {
                if(list != null) return list.size();
                else return 0;
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
                if(list == null){
                    list = getGenre();
                }

                @SuppressLint("ViewHolder")
                View item = LayoutInflater.from(context).inflate(R.layout.item_view, null);
                ImageView artistArt = item.findViewById(R.id.imageView_album_of_song);
                TextView artistName = item.findViewById(R.id.textView_title_of_song);
                TextView albumNumber = item.findViewById(R.id.textView_artist_of_song);

                /*
                //For Image of Artist
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

                String[] columns = {
                        MediaStore.Audio.Media.DATA
                };

                Cursor c = context.getContentResolver().query(uri, columns, MediaStore.Audio.Media.ARTIST + "='" + list.get(index)[0] +"'", null, null);
                assert c != null;
                c.moveToFirst();

                byte[] image = null;

                do {
                    String data = c.getString(c.getColumnIndex(MediaStore.Audio.Media.DATA));

                    MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
                    metaRetriever.setDataSource(data);
                    String genre = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);

                    try {
                        image = metaRetriever.getEmbeddedPicture();
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
                while (image == null && c.moveToNext());*/

                //if (image != null) artistArt.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.length));

                artistName.setText(list.get(index)[0]);
                albumNumber.setText(list.get(index)[1]);
                artistName.setSelected(true);
                albumNumber.setSelected(true);
                return item;
            }
        };
        listView.setOnItemClickListener((adapterView, view, i, l) -> setSongView(i));
        listView.setAdapter(adapter);
        listView.setSelection(index_old);
    }

    private  void  setSongView(int index){
        index_old = index;
        Const.SHOWING_INNER_LIST_GENRE = true;
        genreArt.setVisibility(View.VISIBLE);
        ArrayList<Song> songsOfArtist = new ArrayList<>();
        boolean artistArtNotAdded = true;
        for(Song song : Const.ALL_SONGS_LIST){
            if(song.genre != null && song.genre.equalsIgnoreCase(list.get(index)[0])) {
                songsOfArtist.add(song);
                if(artistArtNotAdded && song.image != null){
                    genreArt.setImageBitmap(BitmapFactory.decodeByteArray(song.image, 0, song.image.length));
                    artistArtNotAdded = false;
                }
            }
        }
        listView.setAdapter(new Adapter(getContext(), songsOfArtist));
        listView.setOnItemClickListener((adapterView, view, index1, id) -> Toast.makeText(context, ""+ index1, Toast.LENGTH_SHORT).show());
    }

    private ArrayList<String[]> getGenre(){

        ArrayList<String[]> list = new ArrayList<>();
        Uri uri = MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI;
        final String[] columns = {
                MediaStore.Audio.Genres._ID,
                MediaStore.Audio.Genres.NAME
        };

        Cursor c = context.getContentResolver().query(uri, columns, null, null, null);
        assert c != null;
        if(!c.moveToFirst()){
            Toast.makeText(context,"No Data", Toast.LENGTH_SHORT).show();
        }else {
            do {
                String name = c.getString(c.getColumnIndex(MediaStore.Audio.Genres.NAME));
                String song = " ";
                list.add(new String[]{name, song});
            }
            while (c.moveToNext());
        }
        c.close();
        return list;
    }
}
