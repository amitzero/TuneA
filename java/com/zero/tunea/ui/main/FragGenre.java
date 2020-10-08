package com.zero.tunea.ui.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.zero.tunea.R;

import java.util.ArrayList;

public class FragGenre extends Fragment {

    public static Handler handler;

    @SuppressLint("HandlerLeak")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                int command = msg.what;
            }
        };
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        @SuppressLint("InflateParams")
        View root = inflater.inflate(R.layout.list, null);
        TextView empty = root.findViewById(R.id.textViewEmpty);
        empty.setVisibility(View.GONE);
        ListView listView = root.findViewById(R.id.list);
        listView.setVisibility(View.VISIBLE);
        BaseAdapter adapter = new BaseAdapter() {
            ArrayList<String> list = getGenre();
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
            public View getView(int i, View view, ViewGroup viewGroup) {
                if(list == null){
                    list = getGenre();
                }
                TextView tv = new TextView(getContext());
                tv.setText(list.get(i));
                return tv;
            }
        };
        listView.setAdapter(adapter);
        return root;
    }


    private ArrayList<String> getGenre(){

        ArrayList<String> list = new ArrayList<>();
        Uri uri = MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI;
        final String[] columns = {
                MediaStore.Audio.Genres._ID,
                MediaStore.Audio.Genres.NAME,
        };

        Context context = getContext();
        assert  context != null;
        Cursor c = context.getContentResolver().query(uri, columns, null, null, null);
        assert c != null;
        if(!c.moveToFirst()){
            Toast.makeText(getContext(),"No Data", Toast.LENGTH_SHORT).show();
        }else {
            do {
                String name = c.getString(c.getColumnIndex(MediaStore.Audio.Genres.NAME));
                //String albums = c.getString(c.getColumnIndex(MediaStore.Audio.Genres.CONTENT_TYPE));
                //Toast.makeText(getContext(),""+c.getColumnCount()+name, Toast.LENGTH_SHORT).show();
                list.add(name);//+"\nType:"+albums);
            }
            while (c.moveToNext());
        }
        c.close();
        return list;
    }
}
