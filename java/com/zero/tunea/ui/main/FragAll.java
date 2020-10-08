package com.zero.tunea.ui.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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

public class FragAll extends Fragment {

    public static Handler handler;
    private Context context;

    private View root;
    private TextView empty;
    private ListView listView;

    public FragAll(Context context){
        this.context = context;
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
                    case Const.SONG_LIST_CHANGE:
                        initSongs();
                        break;
                    case Const.SONG_LIST_EMPTY:
                        empty.setText("Empty");
                        break;
                }
            }
        };
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.list, null);
        empty = root.findViewById(R.id.textViewEmpty);
        empty.setText("Loading...");
        listView = root.findViewById(R.id.list);
        if(Const.CURRENT_SONGS_LIST.size() > 0){
            initSongs();
        }
        return root;
    }

    private void initSongs(){
        empty.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);
        listView.setAdapter(new Adapter(context, Const.CURRENT_SONGS_LIST));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long id) {
                Const.SONG_PAUSED = false;
                Const.CURRENT_SONG_NUMBER = index;
                MainActivity.handler.obtainMessage(Const.START_SERVICE).sendToTarget();
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                PopupMenu popup = new PopupMenu(context, view);
                popup.getMenuInflater().inflate(R.menu.more_menu, popup.getMenu());
                popup.show();

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
                {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId())
                        {
                            case R.id.play_item:

                                break;
                            case R.id.delete:

                                break;
                            case R.id.More:
                            default:
                                break;
                        }

                        return true;
                    }
                });
                //End
                return true;
            }
        });
    }

}
