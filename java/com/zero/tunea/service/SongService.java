package com.zero.tunea.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.media.session.MediaSessionCompat;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.zero.tunea.MainActivity;
import com.zero.tunea.R;
import com.zero.tunea.classes.Const;
import com.zero.tunea.classes.MediaPlayerCustom;
import com.zero.tunea.classes.Song;
import com.zero.tunea.receiver.NotificationReceiver;

@SuppressWarnings("deprecation")
public class SongService extends Service {

    public static Handler handler;

    MediaPlayerCustom mediaPlayer;
    public static final String NOTIFY_PREVIOUS = "com.zero.tunea.previous";
    public static final String NOTIFY_DELETE = "com.zero.tunea.delete";
    public static final String NOTIFY_PLAY_PAUSE = "com.zero.tunea.play_pause";
    public static final String NOTIFY_NEXT = "com.zero.tunea.next";

    private RemoteControlClient remoteControlClient;
    private AudioManager audioManager;
    private Bitmap mDummyAlbumArt;
    private static boolean currentVersionSupportBigNotification = false;
    private static boolean currentVersionSupportLockScreenControls = false;

    public SongService() {}

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("HandlerLeak")
    @Override
    public void onCreate() {

        handler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case Const.SONG_CHANGED:
                        songChange();
                        break;
                    case Const.PLAY_PAUSE:
                        playPause();
                        break;
                    case Const.SEEK_SONG:
                        seekSong((int) msg.obj);
                        break;
                    case Const.PLAY_PREVIOUS:
                        Const.previous();
                        songChange();
                        break;
                    case Const.PLAY_NEXT:
                        Const.next();
                        songChange();
                        break;
                }
            }
        };

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        currentVersionSupportBigNotification = Const.currentVersionSupportBigNotification();
        currentVersionSupportLockScreenControls = Const.currentVersionSupportLockScreenControls();

        super.onCreate();
    }

    @SuppressLint("NewApi")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (currentVersionSupportLockScreenControls) RegisterRemoteClient();
        initMedia();
//        newNotification();
        notification();
        return START_STICKY;
    }

    private String CHANNEL_ID = "MY_CHANNEL";

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "KOD Dev", NotificationManager.IMPORTANCE_LOW);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null){
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void notification(){
        createChannel();
        PendingIntent intent = PendingIntent.getBroadcast(getApplicationContext(), 1, new Intent("PLAY"), PendingIntent.FLAG_UPDATE_CURRENT);
        //show notification for only first time
        Notification notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_music_small_icon)
                .setContentTitle(Const.getCurrentSong().title)
                .setContentText(Const.getCurrentSong().artist)
                .setLargeIcon(BitmapFactory.decodeByteArray(Const.getCurrentSong().image, 0, Const.getCurrentSong().image.length))
                .setOnlyAlertOnce(true)//show notification for only first time
                .setShowWhen(false)
                .addAction(0, "Previous", intent)
                .addAction(androidx.appcompat.R.drawable.abc_btn_check_material, "Play", intent)
                .addAction(R.drawable.ic_music_small_icon, "Next", intent)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2)
                        .setMediaSession(new MediaSessionCompat(getApplicationContext(), "MY_NOTIFICATION").getSessionToken()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
        notificationManagerCompat.notify(1, notification);
    }

    public void initMedia(){
        mediaPlayer = new MediaPlayerCustom(getApplicationContext());
        mediaPlayer.setPlayList(Const.CURRENT_SONGS_LIST);
        mediaPlayer.setMediaPlayerListener(new MediaPlayerCustom.MediaPlayerListener() {

            @Override
            public void onStart() {
                Const.SONG_PAUSED = false;
                refreshMetadata();
            }

            @Override
            public void onPause() {
                Const.SONG_PAUSED = true;
                refreshUI();
            }

            @Override
            public void onStop() {
                Const.SONG_PAUSED = true;
                refreshMetadata();
            }

            @Override
            public void onRelease() {
                Const.SONG_PAUSED = true;
                refreshMetadata();
            }

            @Override
            public void onError(int what) {
                Const.SONG_PAUSED = true;
                refreshMetadata();
            }

            @Override
            public void onComplete() {
                Const.next();
                mediaPlayer.play(Const.CURRENT_SONG_NUMBER);
            }

            @Override
            public void onPositionChange(Integer[] i) {

            }
        });

        mediaPlayer.play(Const.CURRENT_SONG_NUMBER);
    }

    @SuppressLint("NewApi")
    private void newNotification() {
        Song song = Const.getCurrentSong();
        String songName = song.title;
        String albumName = song.album;

        RemoteViews simpleContentView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.custom_notification);
        RemoteViews expandedView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.big_notification);

        Intent m = new Intent(this, MainActivity.class);
        PendingIntent pm = PendingIntent.getActivity(this, 0, m, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationChannel notificationChannel = new NotificationChannel("68","68", NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.setLightColor(Color.BLUE);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.createNotificationChannel(notificationChannel);


        Notification notification = new NotificationCompat.Builder(getApplicationContext(), "68")
                .setSmallIcon(R.drawable.ic_music_small_icon)
                .setContentIntent(pm)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setContentTitle(songName).build();

        setNotificationListeners(simpleContentView);
        setNotificationListeners(expandedView);

        notification.contentView = simpleContentView;
        if (currentVersionSupportBigNotification) {
            notification.bigContentView = expandedView;
        }

        try {
            Bitmap albumArt = BitmapFactory.decodeByteArray(song.image, 0, song.image.length);
            if (albumArt != null) {
                notification.contentView.setImageViewBitmap(R.id.imageViewAlbumArt, albumArt);
                if (currentVersionSupportBigNotification) {
                    notification.bigContentView.setImageViewBitmap(R.id.imageViewAlbumArt, albumArt);
                }
            } else {
                notification.contentView.setImageViewResource(R.id.imageViewAlbumArt, R.mipmap.ic_launcher);
                if (currentVersionSupportBigNotification) {
                    notification.bigContentView.setImageViewResource(R.id.imageViewAlbumArt, R.mipmap.ic_launcher);
                }
            }
        } catch (Exception ignored) {
        }

        if (Const.SONG_PAUSED) {
            notification.contentView.setImageViewResource(R.id.btnPause, R.drawable.play);

            if (currentVersionSupportBigNotification) {
                notification.bigContentView.setImageViewResource(R.id.btnPause, R.drawable.play);
            }
        } else {
            notification.contentView.setImageViewResource(R.id.btnPause, R.drawable.pause);

            if (currentVersionSupportBigNotification) {
                notification.bigContentView.setImageViewResource(R.id.btnPause, R.drawable.pause);
            }
        }

        notification.contentView.setTextViewText(R.id.textSongName, songName);
        notification.contentView.setTextViewText(R.id.textAlbumName, albumName);
        if (currentVersionSupportBigNotification) {
            notification.bigContentView.setTextViewText(R.id.textSongName, songName);
            notification.bigContentView.setTextViewText(R.id.textAlbumName, albumName);
        }
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        int NOTIFICATION_ID = 68;
        startForeground(NOTIFICATION_ID, notification);
    }

    public void setNotificationListeners(RemoteViews view) {
        Intent previous = new Intent(NOTIFY_PREVIOUS);
        Intent delete = new Intent(NOTIFY_DELETE);
        Intent play_pause = new Intent(NOTIFY_PLAY_PAUSE);
        Intent next = new Intent(NOTIFY_NEXT);

        PendingIntent pDelete = PendingIntent.getBroadcast(getApplicationContext(), 0, delete, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnDelete, pDelete);

        PendingIntent pPrevious = PendingIntent.getBroadcast(getApplicationContext(), 0, previous, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnPrevious, pPrevious);

        PendingIntent pPlay_Pause = PendingIntent.getBroadcast(getApplicationContext(), 0, play_pause, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnPause, pPlay_Pause);

        PendingIntent pNext = PendingIntent.getBroadcast(getApplicationContext(), 0, next, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnNext, pNext);

    }

    @SuppressLint("NewApi")
    private void RegisterRemoteClient() {
        ComponentName remoteComponentName = new ComponentName(getApplicationContext(), NotificationReceiver.class.getName());
        try {
            if (remoteControlClient == null) {
                audioManager.registerMediaButtonEventReceiver(remoteComponentName);
                Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
                mediaButtonIntent.setComponent(remoteComponentName);
                PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);
                remoteControlClient = new RemoteControlClient(mediaPendingIntent);
                audioManager.registerRemoteControlClient(remoteControlClient);
            }
            remoteControlClient.setTransportControlFlags(
                    RemoteControlClient.FLAG_KEY_MEDIA_PLAY |
                            RemoteControlClient.FLAG_KEY_MEDIA_PAUSE |
                            RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE |
                            RemoteControlClient.FLAG_KEY_MEDIA_STOP |
                            RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS |
                            RemoteControlClient.FLAG_KEY_MEDIA_NEXT);
        } catch (Exception ignored) {
        }
    }


    private void songChange() {
        mediaPlayer.play(Const.CURRENT_SONG_NUMBER);
    }

    private void playPause() {
        if (mediaPlayer == null) return;

        Const.SONG_PAUSED = !Const.SONG_PAUSED;
        if (Const.SONG_PAUSED) {
            if (currentVersionSupportLockScreenControls) {
                remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
            }
            mediaPlayer.pause();
        } else {
            if (currentVersionSupportLockScreenControls) {
                remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
            }
            mediaPlayer.play(Const.CURRENT_SONG_NUMBER);
        }
    }

    private void seekSong(int position) {
        mediaPlayer.seekTo(position);
    }

    @Override
    public void onDestroy() {
        Const.SONG_PAUSED = true;
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer = null;
        }
        super.onDestroy();
    }

    public void refreshUI(){
        Const.message(Const.REFRESH_UI);
    }

    private void refreshMetadata() {
        Const.message(Const.REFRESH_METADATA);
        if (remoteControlClient == null) return;
        Song song = Const.getCurrentSong();
        RemoteControlClient.MetadataEditor metadataEditor = remoteControlClient.editMetadata(true);
        metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, song.album);
        metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, song.artist);
        metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_TITLE, song.title);
//        mDummyAlbumArt = UtilFunctions.getAlbumArt(getApplicationContext(), song.albumId);
        if (mDummyAlbumArt == null) {
            mDummyAlbumArt = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        }
        metadataEditor.putBitmap(RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK, mDummyAlbumArt);
        metadataEditor.apply();

    }


//ExoPlayer
//
//    private void initExo(Song song){
//        RenderersFactory renderersFactory = new DefaultRenderersFactory(this,
//                DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF);
//        TrackSelector trackSelector = new DefaultTrackSelector();
//        simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(
//                this,
//                renderersFactory,
//                trackSelector);
//        String userAgent = Util.getUserAgent(this, "TuneA");
//        MediaSource mediaSource = new ExtractorMediaSource(
//                Uri.parse("file://"+song.path),
//                new DefaultDataSourceFactory(this, userAgent),
//                new DefaultExtractorsFactory(),
//                null,
//                null);
//        simpleExoPlayer.prepare(mediaSource);
//        simpleExoPlayer.setPlayWhenReady(true);
//
//
//        PlayerNotificationManager playerNotificationManager;
//        int notificationId = 1234;
//        PlayerNotificationManager.MediaDescriptionAdapter mediaDescriptionAdapter = new PlayerNotificationManager.MediaDescriptionAdapter() {
//            @Override
//            public String getCurrentSubText(Player player) {
//                return "Sub text";
//            }
//
//            @Override
//            public String getCurrentContentTitle(@NonNull Player player) {
//                return song.title;
//            }
//
//            @Override
//            public PendingIntent createCurrentContentIntent(@NonNull Player player) {
//                return null;
//            }
//
//            @Override
//            public String getCurrentContentText(@NonNull Player player) {
//                return song.artist;
//            }
//
//            @Override
//            public Bitmap getCurrentLargeIcon(@NonNull Player player, @NonNull PlayerNotificationManager.BitmapCallback callback) {
//
//                return BitmapFactory.decodeByteArray(song.image, 0, song.image.length);
//            }
//        };
//
//            playerNotificationManager = PlayerNotificationManager.createWithNotificationChannel(this, "My_channel_id", R.string.app_name, notificationId, mediaDescriptionAdapter, new PlayerNotificationManager.NotificationListener() {
//                @Override
//                public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {
//                }
//
//                @Override
//                public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
//                }
//            });
//            playerNotificationManager.setUseNavigationActionsInCompactView(true);
//            playerNotificationManager.setUsePlayPauseActions(true);
//            playerNotificationManager.setControlDispatcher(new ControlDispatcher() {
//                @Override
//                public boolean dispatchSetPlayWhenReady(Player player, boolean playWhenReady) {
//                    return false;
//                }
//
//                @Override
//                public boolean dispatchSeekTo(Player player, int windowIndex, long positionMs) {
//                    return false;
//                }
//
//                @Override
//                public boolean dispatchPrevious(Player player) {
//                    return false;
//                }
//
//                @Override
//                public boolean dispatchNext(Player player) {
//                    return false;
//                }
//
//                @Override
//                public boolean dispatchRewind(Player player) {
//                    return false;
//                }
//
//                @Override
//                public boolean dispatchFastForward(Player player) {
//                    return false;
//                }
//
//                @Override
//                public boolean dispatchSetRepeatMode(Player player, int repeatMode) {
//                    return false;
//                }
//
//                @Override
//                public boolean dispatchSetShuffleModeEnabled(Player player, boolean shuffleModeEnabled) {
//                    return false;
//                }
//
//                @Override
//                public boolean dispatchStop(Player player, boolean reset) {
//                    return false;
//                }
//
//                @Override
//                public boolean isRewindEnabled() {
//                    return false;
//                }
//
//                @Override
//                public boolean isFastForwardEnabled() {
//                    return false;
//                }
//            });
//            playerNotificationManager.setPlayer(simpleExoPlayer);
//
//
//        @Override
//        public void onDestroy() {
//            super.onDestroy();
//            if (playerNotificationManager != null) {
//                playerNotificationManager.setPlayer(null);
//            }
//            if (exoPlayer != null) {
//                exoPlayer.release();
//                exoPlayer = null;
//            }
//    }
/*
    private void initExo(){
        simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(this, new DefaultTrackSelector());
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.CONTENT_TYPE_SPEECH)
                .build();
        simpleExoPlayer.setAudioAttributes(audioAttributes, true);

        //Monitor ExoPlayer event
        simpleExoPlayer.addListener(new Player.EventListener() {
            @Override
            public void onPlaybackStateChanged(int state) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {

            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

            }
        });

        //setup notification and media session
        PlayerNotificationManager playerNotificationManager = PlayerNotificationManager.createWithNotificationChannel(
                getApplicationContext(),
                "notificationChannelId",
                R.string.notificationChannelName,
                101,
                new PlayerNotificationManager.MediaDescriptionAdapter() {
                    @Override
                    public CharSequence getCurrentContentTitle(Player player) {
                        return null;
                    }

                    @Nullable
                    @Override
                    public PendingIntent createCurrentContentIntent(Player player) {
                        return null;
                    }

                    @Nullable
                    @Override
                    public CharSequence getCurrentContentText(Player player) {
                        return null;
                    }

                    @Nullable
                    @Override
                    public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
                        return null;
                    }
                },
                new PlayerNotificationManager.NotificationListener() {
                    @Override
                    public void onNotificationStarted(int notificationId, Notification notification) {

                    }

                    @Override
                    public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {

                    }
                });
        playerNotificationManager.setUseNavigationActions(false);
        playerNotificationManager.setUseStopAction(true);
        playerNotificationManager.setPlayer(simpleExoPlayer);

        // Show lock screen controls and let apps like Google assistant manager playback.
        MediaSessionCompat mediaSession = new MediaSessionCompat(getApplicationContext(), "MEDIA_SESSION_TAG");
        mediaSession.setActive(true);
        playerNotificationManager.setMediaSessionToken(mediaSession.getSessionToken());

        MediaSessionConnector mediaSessionConnector = new MediaSessionConnector(mediaSession);
        mediaSessionConnector.setQueueNavigator(new TimelineQueueNavigator(mediaSession) {
            @Override
            public MediaDescriptionCompat getMediaDescription(Player player, int windowIndex) {
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
                Bundle extras = new Bundle();
                extras.putParcelable(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap);
                extras.putParcelable(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, bitmap);
                return new MediaDescriptionCompat.Builder()
                        .setIconBitmap(bitmap)
                        .setTitle("my title")
                        .setExtras(extras)
                        .build();
            }
        });
        playerNotificationManager.setPlayer(simpleExoPlayer);
    }

    private void playOnExoPlayer(String path){
        String userAgent = Util.getUserAgent(getApplicationContext(), BuildConfig.APPLICATION_ID);
        MediaSource mediaSource = new ExtractorMediaSource(
                Uri.fromFile(new File(path)),
                new DefaultDataSourceFactory(getApplicationContext(), userAgent),
                new DefaultExtractorsFactory(),null, null);
        simpleExoPlayer.prepare(mediaSource);
        simpleExoPlayer.setPlayWhenReady(true);
    }*/
//    static public SimpleExoPlayer exoPlayer;
//
//    static class MyPlayer extends SimpleExoPlayer{
//
//        static MyPlayer player;
//        Song song;
//
//        public static MyPlayer newPlayerIntense(Song song, SimpleExoPlayer simpleExoPlayer1){
//            player = (MyPlayer) simpleExoPlayer1;
//            player.song = song;
//            return player;
//        }
//
//        protected MyPlayer(Context context, RenderersFactory renderersFactory, TrackSelector trackSelector, MediaSourceFactory mediaSourceFactory, LoadControl loadControl, BandwidthMeter bandwidthMeter, AnalyticsCollector analyticsCollector, boolean useLazyPreparation, Clock clock, Looper applicationLooper) {
//            super(context, renderersFactory, trackSelector, mediaSourceFactory, loadControl, bandwidthMeter, analyticsCollector, useLazyPreparation, clock, applicationLooper);
////            simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(
////                    SongService.this,
////                    renderersFactory,
////                    trackSelector);
////            String userAgent = Util.getUserAgent(SongService.this, "TuneA");
////            MediaSource mediaSource = new ExtractorMediaSource(
////                    Uri.parse("file://"+"data"),
////                    new DefaultDataSourceFactory(SongService.this, userAgent),
////                    new DefaultExtractorsFactory(),
////                    null,
////                    null);
//        }
//    }

}
