/*
 * Copyright (C) 2019  All rights reserved for FaraSource (ABBAS GHASEMI)
 * https://farasource.com
 */
package ghasemi.abbas.book.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import ghasemi.abbas.book.PostActivity;
import ghasemi.abbas.book.R;
import ghasemi.abbas.book.components.MusicPlayer;
import ghasemi.abbas.book.general.ApplicationLoader;
import ghasemi.abbas.book.general.FileLogs;
import ghasemi.abbas.book.general.NotificationUtilities;
import ghasemi.abbas.book.support.NotificationDelegate;

public class MusicPlayerUtilities extends Service implements NotificationDelegate {
    private static boolean started;
    private static NotificationDelegate notificationDelegate;
    private final BroadcastReceiver headsetPlugReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                if (musicPlayerID != -1 && musicPlayers.size() > musicPlayerID) {
                    get().pause();
                }
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationDelegate = this;
        registerReceiver(headsetPlugReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
    }

    private static boolean startService(Context context) {
        if (started || context == null || isEmpty()) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context, MusicPlayerUtilities.class));
        } else {
            context.startService(new Intent(context, MusicPlayerUtilities.class));
        }
        return true;
    }

    public static void stopService(Context context) {
        if (started && context != null) {
            context.stopService(new Intent(context, MusicPlayerUtilities.class));
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (isEmpty()) {
            stopForeground(true);
        } else {
            started = true;
            startForeground(NOTIFY_MEDIA_PLAYER_ID, createNotification());
            try {
                PhoneStateListener phoneStateListener = new PhoneStateListener() {
                    @Override
                    public void onCallStateChanged(final int state, String incomingNumber) {

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                if (state == TelephonyManager.CALL_STATE_RINGING) {
                                    get().pause();
                                } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                                    if (musicPlayerID != -1 && !get().isLoading()) {
                                        get().start();
                                    }
                                } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                                    get().pause();
                                }
                            }
                        });
                    }
                };
                TelephonyManager mgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                if (mgr != null) {
                    mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
                }
            } catch (Exception e) {
                FileLogs.e(e);
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(headsetPlugReceiver);
        super.onDestroy();
        started = false;
        if (PostActivity.runningPostActivity) {
            MusicPlayerUtilities.get().hideProgressBar();
            MusicPlayerUtilities.get().stop(true);
        } else {
            destroy(true);
        }
    }

    @Override
    public void delegateCreate(boolean start) {
        create(start);
    }

    public static class ListMusicPlayer {

        public final MusicPlayer musicPlayer;
        private final String tag;

        public ListMusicPlayer(MusicPlayer musicPlayer, String tag) {
            this.musicPlayer = musicPlayer;
            this.tag = tag;
        }
    }

    private static final List<ListMusicPlayer> musicPlayers = new ArrayList<>();
    private static final List<ListMusicPlayer> musicPlayers2 = new ArrayList<>();

    private static int musicPlayerID = -1;

    public static ListMusicPlayer hasTag(String tag) {
        for (ListMusicPlayer listMusicPlayer : musicPlayers) {
            if (listMusicPlayer.tag.equals(tag)) {
                return listMusicPlayer;
            }
        }
        return null;
    }

    public static void add(ListMusicPlayer listMusicPlayer) {
        musicPlayers2.add(listMusicPlayer);
    }


    public static boolean isEmpty() {
        return musicPlayers.isEmpty();
    }

    public static void rebuild() {
        if (musicPlayers2.isEmpty()) {
            return;
        }
        if (musicPlayerID != -1 && musicPlayers.size() > musicPlayerID) {
            get().pause();
            get().hideProgressBar();
        }
        musicPlayerID = -1;
        for (ListMusicPlayer l : musicPlayers) {
            if (!musicPlayers2.contains(l)) {
                l.musicPlayer.destroy();
            }
        }
        musicPlayers.clear();
        musicPlayers.addAll(musicPlayers2);
        musicPlayers2.clear();
    }

    public static MusicPlayer get() {
        return musicPlayers.get(musicPlayerID).musicPlayer;
    }

    public static List<ListMusicPlayer> getMusicPlayers() {
        return musicPlayers;
    }

    public static int getMusicPlayerID() {
        return musicPlayerID;
    }

    public static void setMusicPlayer(MusicPlayer musicPlayer) {
        for (int i = 0; i < musicPlayers.size(); i++) {
            if (musicPlayers.get(i).musicPlayer == musicPlayer) {
                musicPlayerID = i;
                break;
            }
        }
    }

    public static void destroy(boolean all) {
        if (all) {
            NotificationUtilities.deleteNotification(NOTIFY_MEDIA_PLAYER_ID);
            for (ListMusicPlayer listMusicPlayer : musicPlayers) {
                listMusicPlayer.musicPlayer.destroy();
            }
            musicPlayers.clear();
            musicPlayerID = -1;
        }
        musicPlayers2.clear();
    }

    public static void setBackground(Drawable backMP) {
        for (ListMusicPlayer listMusicPlayer : musicPlayers) {
            listMusicPlayer.musicPlayer.setBackground(backMP);
        }
        for (ListMusicPlayer listMusicPlayer : musicPlayers2) {
            listMusicPlayer.musicPlayer.setBackground(backMP);
        }
    }

    public static class MediaInfo {

        private static String title;
        private static String album;
        private static String artist;
        private static Bitmap image;
        private static boolean isPlaying;
        private static long duration;
        private static long position;
        private static boolean downloadMode;
        private static boolean downloading;

        public static boolean isDownloading() {
            return downloading;
        }

        public static boolean isDownloadMode() {
            return downloadMode;
        }

        public static void setMediaInfo(String title, String album, String artist, Bitmap image, long duration) {
            downloadMode = false;
            downloading = false;
            MediaInfo.title = title;
            MediaInfo.album = album;
            MediaInfo.artist = artist;
            MediaInfo.image = image;
            MediaInfo.duration = duration;
        }

        public static void setDownload(String title, String content, boolean downloading) {
            downloadMode = true;
            MediaInfo.downloading = downloading;
            duration = 0;
            MediaInfo.album = title;
            MediaInfo.title = content;
            MediaInfo.artist = "";
            createServiceNotification();
        }

        public static void setPosition(long position) {
            MediaInfo.position = position;
        }

        public static void setPlaying(boolean playing) {
            isPlaying = playing;
        }

        public static boolean isPlaying() {
            return isPlaying;
        }

        private static Bitmap getImage() {
            if (image == null) {
                MediaInfo.image = BitmapFactory.decodeResource(ApplicationLoader.context.getResources(), R.drawable.ic_library_music);
            }
            return image;
        }

        private static String getAlbum() {
            return album;
        }

        private static String getArtist() {
            return artist;
        }

        private static String getTitle() {
            return title;
        }

        private static long getDuration() {
            return duration;
        }

        private static long getPosition() {
            return position;
        }
    }

    public static void onReceiveIntent(Intent intent) {
        if (isEmpty()) {
            stopService(ApplicationLoader.context.getApplicationContext());
            destroy(true);
            return;
        }

        if (intent.getAction().equals(ACTION_PREVIOUS)) {
            if (musicPlayers.size() == 1) {
                return;
            }
            get().pause();
            get().hideProgressBar();
            if (getMusicPlayerID() > 0) {
                musicPlayerID = getMusicPlayerID() - 1;
            } else {
                musicPlayerID = musicPlayers.size() - 1;
            }
            get().start();
        } else if (intent.getAction().equals(ACTION_NEXT)) {
            if (musicPlayers.size() == 1) {
                return;
            }
            get().pause();
            get().hideProgressBar();
            if (musicPlayers.size() > getMusicPlayerID() + 1) {
                musicPlayerID = getMusicPlayerID() + 1;
            } else {
                musicPlayerID = 0;
            }
            get().start();
        } else if (intent.getAction().equals(ACTION_PAUSE)) {
            get().pause();
        } else if (intent.getAction().equals(ACTION_PLAY)) {
            get().start();
        } else if (intent.getAction().equals(ACTION_DELETE)) {
            if (PostActivity.runningPostActivity) {
                MusicPlayerUtilities.get().hideProgressBar();
                MusicPlayerUtilities.get().stop(true);
            } else {
                stopService(ApplicationLoader.context.getApplicationContext());
                destroy(true);
            }
        } else if (intent.getAction().equals(ACTION_OPEN)) {
//            ApplicationLoader.context.startActivity(new Intent(ApplicationLoader.context, CustomMainActivity.class));
        }
    }

    public final static int NOTIFY_MEDIA_PLAYER_ID = 1;
    private final static String ACTION_PREVIOUS = ApplicationLoader.context.getPackageName() + ".intent.action.PREVIOUS_BUTTON";
    private final static String ACTION_PAUSE = ApplicationLoader.context.getPackageName() + ".intent.action.PAUSE_BUTTON";
    private final static String ACTION_PLAY = ApplicationLoader.context.getPackageName() + ".intent.action.PLAY_BUTTON";
    private final static String ACTION_NEXT = ApplicationLoader.context.getPackageName() + ".intent.action.NEXT_BUTTON";
    private final static String ACTION_OPEN = ApplicationLoader.context.getPackageName() + ".intent.action.OPEN_CLICK";
    private final static String ACTION_DELETE = ApplicationLoader.context.getPackageName() + ".intent.action.DELETE_NOTIFICATION";
    private final static MediaSessionCompat mediaSession = new MediaSessionCompat(ApplicationLoader.context, "GhasemiMediaPlayer");

    private void create(boolean start) {
        if (isEmpty()) {
            return;
        }
        if (start && MusicPlayerUtilities.startService(this.getApplicationContext())) {
            startForeground(NOTIFY_MEDIA_PLAYER_ID, createNotification());
        } else {
            NotificationUtilities.notificationManager().notify(NOTIFY_MEDIA_PLAYER_ID, createNotification());
        }
    }

    private Notification createNotification() {
        boolean isPlaying = MediaInfo.isPlaying();
        if (!isPlaying && !MediaInfo.isDownloading() && started) {
            stopForeground(false);
            started = false;
        }
        createMediaSession(isPlaying);
        NotificationUtilities.createChannel(NotificationUtilities.MEDIA_PLAYER_CHANNEL_ID, "notification media player");
        PendingIntent pendingIntentPrevious = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_PREVIOUS).setComponent(new ComponentName(this, NotificationReceiver.class)), PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent pendingIntentPause = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_PAUSE).setComponent(new ComponentName(this, NotificationReceiver.class)), PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent pendingIntentPlay = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_PLAY).setComponent(new ComponentName(this, NotificationReceiver.class)), PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent pendingIntentNext = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_NEXT).setComponent(new ComponentName(this, NotificationReceiver.class)), PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent pendingIntentOpen = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_OPEN).setComponent(new ComponentName(this, NotificationReceiver.class)), PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent pendingIntentDelete = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_DELETE).setComponent(new ComponentName(this, NotificationReceiver.class)), PendingIntent.FLAG_CANCEL_CURRENT);
        int id;
        String action = (isPlaying || MediaInfo.isDownloading()) ? ACTION_PAUSE : ACTION_PLAY;
        if (MediaInfo.isDownloadMode()) {
            id = MediaInfo.isDownloading() ? R.drawable.ic_round_pause_24 : R.drawable.ic_round_arrow_downward_24;
        } else {
            id = isPlaying ? R.drawable.ic_round_pause_24 : R.drawable.ic_round_play_arrow_24;
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NotificationUtilities.MEDIA_PLAYER_CHANNEL_ID)
                .setOngoing(isPlaying || MediaInfo.isDownloading())
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(MediaInfo.isDownloading() ? R.drawable.ic_round_arrow_downward_24 : R.drawable.ic_round_play_circle_filled_24)
                .setContentTitle(MediaInfo.getTitle())
                .setSubText(MediaInfo.getAlbum())
                .setContentIntent(pendingIntentOpen)
                .addAction(R.drawable.ic_round_skip_previous_24, ACTION_PREVIOUS, pendingIntentPrevious)
                .addAction(id, action, isPlaying ? pendingIntentPause : pendingIntentPlay)
                .addAction(R.drawable.ic_round_skip_next_24, ACTION_NEXT, pendingIntentNext)
                .setDeleteIntent(pendingIntentDelete)
                .setShowWhen(false)
                .setContentText(MediaInfo.getArtist())
                .setLargeIcon(MediaInfo.getImage())
                .setPriority(Notification.PRIORITY_MAX)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2)
                        .setMediaSession(mediaSession.getSessionToken())
                );
        return builder.build();
    }

    private void createMediaSession(boolean isPlaying) {
        MediaMetadataCompat mediaMetadata = new MediaMetadataCompat.Builder()
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, MediaInfo.getImage())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, MediaInfo.getAlbum())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, MediaInfo.getTitle())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, MediaInfo.getArtist())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, MediaInfo.getAlbum())
                .putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, MediaInfo.getImage())
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, MediaInfo.getDuration())
                .build();
        PlaybackStateCompat.Builder state = new PlaybackStateCompat.Builder()
                .setActions(isPlaying ? PlaybackStateCompat.ACTION_PAUSE : PlaybackStateCompat.ACTION_PLAY)
                .setState(isPlaying ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED,
                        MediaInfo.getPosition(),
                        isPlaying ? 1 : 0,
                        SystemClock.elapsedRealtime());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            long actions = PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_PLAY |
                    PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
            if (isPlaying) {
                actions |= PlaybackStateCompat.ACTION_SEEK_TO;
            }
            state.setActions(actions);
            mediaSession.setCallback(new MediaSessionCompat.Callback() {
                @Override
                public void onPlay() {
                    onReceiveIntent(new Intent(ACTION_PLAY));
                }

                @Override
                public void onPause() {
                    onReceiveIntent(new Intent(ACTION_PAUSE));
                }

                @Override
                public void onSkipToNext() {
                    onReceiveIntent(new Intent(ACTION_NEXT));
                }

                @Override
                public void onSkipToPrevious() {
                    onReceiveIntent(new Intent(ACTION_PREVIOUS));
                }

                @Override
                public void onSeekTo(long pos) {
                    if (isEmpty()) {
                        stopService(ApplicationLoader.context.getApplicationContext());
                        destroy(true);
                        return;
                    }
                    get().seekTo(pos);
                }
            });
            mediaSession.setActive(true);
        }
        mediaSession.setMetadata(mediaMetadata);
        mediaSession.setPlaybackState(state.build());
    }

    public static void createServiceNotification() {
        createServiceNotification(true);
    }

    public static void createServiceNotification(boolean start) {
        if (start && notificationDelegate == null) {
            startService(ApplicationLoader.context.getApplicationContext());
        } else {
            notificationDelegate.delegateCreate(start);
        }
    }
}