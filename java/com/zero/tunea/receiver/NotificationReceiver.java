package com.zero.tunea.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import com.zero.tunea.classes.Const;
import com.zero.tunea.service.SongService;

import java.util.Objects;

public class NotificationReceiver extends BroadcastReceiver {

    @Override

    public void onReceive(Context context, Intent intent)
    {
        if (Objects.equals(intent.getAction(), Intent.ACTION_MEDIA_BUTTON))
        {
            KeyEvent keyEvent = (KeyEvent) Objects.requireNonNull(intent.getExtras()).get(Intent.EXTRA_KEY_EVENT);
            assert keyEvent != null;
            if (keyEvent.getAction() != KeyEvent.ACTION_DOWN)
                return;

            switch (keyEvent.getKeyCode())
            {
                case KeyEvent.KEYCODE_HEADSETHOOK:
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    Const.SONG_PAUSED = !Const.SONG_PAUSED;
                    Const.message(Const.REFRESH_UI);
                    Const.message(Const.PLAY_PAUSE, context);
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    Const.message(Const.PLAY_PREVIOUS, context);
                    break;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    Const.message(Const.PLAY_NEXT, context);
                    break;
                case KeyEvent.KEYCODE_MEDIA_STOP:
                    break;
            }
        }
        else if (Objects.equals(intent.getAction(), android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY))
        {
            Const.SONG_PAUSED = true;
            Const.message(Const.REFRESH_UI);
            Const.message(Const.PLAY_PAUSE, context);
        }
        else
        {
            switch (intent.getAction()) {
                case SongService.NOTIFY_PREVIOUS:
                    Const.message(Const.PLAY_PREVIOUS, context);
                    break;
                case SongService.NOTIFY_PLAY_PAUSE:
                    Const.SONG_PAUSED = !Const.SONG_PAUSED;
                    Const.message(Const.REFRESH_UI);
                    Const.message(Const.PLAY_PAUSE, context);
                    break;
                case SongService.NOTIFY_NEXT:
                    Const.message(Const.PLAY_NEXT, context);
                    break;
                case SongService.NOTIFY_DELETE:
                    Intent i = new Intent(context, SongService.class);
                    //MainActivity.player.setVisibility(View.GONE);
                    context.stopService(i);
                    break;
            }
        }
    }
}
