/*
 * Copyright (C) 2019  All rights reserved for FaraSource (ABBAS GHASEMI)
 * https://farasource.com
 */
package ghasemi.abbas.book.components;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;

import androidx.annotation.Nullable;

import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import androidx.appcompat.widget.AppCompatImageView;
import ghasemi.abbas.book.R;
import ghasemi.abbas.book.general.AndroidUtilities;
import ghasemi.abbas.book.general.FileLogs;
import ghasemi.abbas.book.service.MusicPlayerUtilities;
import ghasemi.abbas.book.support.MediaHttp;

public class MusicPlayer extends FrameLayout {

    private final PlayPauseButton playPauseButton;
    private final TextView timeLast;
    private final TextView info;
    private final TextView time;
    private final TextView artistText;
    private final SeekBar seekBar;
    private MediaPlayer mediaPlayer;
    private String path;
    private boolean isStart, moved, loading, isPause = true, downloading;
    private final ProgressBar progressBar;
    private Handler handler;
    private final View download;
    private final AppCompatImageView image;
    private OnChangeListener onChangeListener;
    private MediaHttp mediaHttp;
    private String errorMessage;

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (handler != null) {
                if (!moved) {
                    int pos = mediaPlayer.getCurrentPosition();
                    MusicPlayerUtilities.MediaInfo.setPosition(pos);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        seekBar.setProgress(pos / 1000, true);
                    } else {
                        seekBar.setProgress(pos / 1000);
                    }
                    int minutes = pos / 1000 / 60;
                    int seconds = pos / 1000 - minutes * 60;
                    timeLast.setText(String.format("%02d:%02d", minutes, seconds));
                    if (onChangeListener != null) {
                        onChangeListener.onTimeMusic(String.format("%02d:%02d", minutes, seconds));
                    }
                }
                handler.postDelayed(this, 1000);
            }
        }
    };
    // info
    private String title, album, artist;
    private Bitmap bitmap = null;
    private long duration;

    public String getTimeLast() {
        return timeLast.getText().toString();
    }

    private void musicInfo(boolean report) {
        if (!TextUtils.isEmpty(title)) {
            if (report) {
                MusicPlayerUtilities.MediaInfo.setMediaInfo(title, album, artist, bitmap, duration);
            }
            return;
        }
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        try {
//            if ("https".equals(scheme) || "http".equals(scheme)) {
//                mmr.setDataSource(path, new HashMap<String, String>());
//            }
            int id = getContext().getResources().getIdentifier(path, "raw", getContext().getPackageName());
            if (id == 0) {
                mmr.setDataSource(path);
            } else {
                AssetFileDescriptor afd = getResources().openRawResourceFd(id);
                mmr.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            }
        } catch (Exception e) {
            //
        }
        artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
        String d = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        byte[] data = mmr.getEmbeddedPicture();
        if (!TextUtils.isEmpty(d)) {
            duration = Long.parseLong(d);
        }
        mmr.release();
        if (TextUtils.isEmpty(artist)) {
            artist = "<Unknown artist>";
        }
        StringBuilder builder = new StringBuilder();
        if (TextUtils.isEmpty(title)) {
            title = "<Unknown title>";
        } else {
            builder.append(title);
        }
        if (TextUtils.isEmpty(album)) {
            album = "<Unknown album>";
        } else {
            if (builder.length() != 0) {
                builder.append(" - ");
            }
            builder.append(album);
        }
        if (data != null) {
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            image.setImageBitmap(bitmap);
        }
        artistText.setText(artist);
        info.setText(builder.toString());
        int minutes = (int) (duration / 1000 / 60);
        int seconds = (int) (duration / 1000 - minutes * 60);
        time.setText(String.format("%02d:%02d", minutes, seconds));
        if (report) {
            MusicPlayerUtilities.MediaInfo.setMediaInfo(title, album, artist, bitmap, duration);
        }
    }

    public MusicPlayer(Context context) {
        this(context, null);
    }

    public MusicPlayer(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MusicPlayer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        addView(LayoutInflater.from(context).inflate(R.layout.post_music_player, null));

        seekBar = findViewById(R.id.seekBar);
        seekBar.setEnabled(false);
        download = findViewById(R.id.download);
        image = findViewById(R.id.image);
        playPauseButton = findViewById(R.id.play_pause);
        timeLast = findViewById(R.id.time_last);
        time = findViewById(R.id.time);
        info = findViewById(R.id.info);
        artistText = findViewById(R.id.artist);
        progressBar = findViewById(R.id.progressBar);
        int m = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && m == Configuration.UI_MODE_NIGHT_YES) {
            seekBar.setProgressBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        }
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (moved) {
                    mediaPlayer.seekTo(progress * 1000);

                    int minutes = progress / 60;
                    int seconds = progress - minutes * 60;
                    timeLast.setText(String.format("%02d:%02d", minutes, seconds));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                moved = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                moved = false;
                if (handler == null) {
                    int minutes = mediaPlayer.getCurrentPosition() / 1000 / 60;
                    int seconds = mediaPlayer.getCurrentPosition() / 1000 - minutes * 60;
                    timeLast.setText(String.format("%02d:%02d", minutes, seconds));
                }
                MusicPlayerUtilities.MediaInfo.setPosition(mediaPlayer.getCurrentPosition());
                createNotification();
            }
        });

//        playPauseButton.setColor(getResources().getColor(R.color.colorAccent));

        image.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (mediaPlayer == null) {
//                    setPath(path);
//                }
                if (!isStart) {
                    AndroidUtilities.toast(errorMessage);
                    playPauseButton.setPlayed(false);
                    return;
                }
                if (downloading) {
                    return;
                }
                if (mediaPlayer.isPlaying()) {
                    playPauseButton.setPlayed(false);
                    pause();
                } else {
                    if (onChangeListener != null) {
                        onChangeListener.onRequestPlay(MusicPlayer.this);
                    }
                    start();
                }
            }
        });
        playPauseButton.setOnControlStatusChangeListener(new PlayPauseButton.OnControlStatusChangeListener() {
            @Override
            public void onStatusChange(View view, boolean state) {

            }
        });
    }

    private void createNotification() {
        MusicPlayerUtilities.createServiceNotification();
    }

    public void setOnChangeListener(OnChangeListener onChangeListener) {
        this.onChangeListener = onChangeListener;
    }

    private void home() {
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (handler != null) {
                    handler.removeCallbacks(runnable);
                    handler = null;
                }
                playPauseButton.setPlayed(false);
                mediaPlayer.seekTo(0);
                MusicPlayer.this.timeLast.setText("00:00");
                isPause = true;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    seekBar.setProgress(0, true);
                } else {
                    seekBar.setProgress(0);
                }
                MusicPlayerUtilities.MediaInfo.setPlaying(false);
                MusicPlayerUtilities.MediaInfo.setPosition(0);
                createNotification();
                if (onChangeListener != null) {
                    onChangeListener.onPlayMusic(false);
                    onChangeListener.onTimeMusic("00:00");
                }
            }
        });
    }

    private void setAudioFromName() {
        int id = getContext().getResources().getIdentifier(path, "raw", getContext().getPackageName());
        if (id == 0) {
            isStart = false;
            errorMessage = "فایلی برای پخش یافت نشد.";
        } else {
            musicInfo(false);
            mediaPlayer = MediaPlayer.create(getContext(), id);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setVolume(100, 100);
            isStart = true;
            home();
            int time = mediaPlayer.getDuration() / 1000;

            int minutes = time / 60;
            int seconds = time - minutes * 60;
            this.time.setText(String.format("%02d:%02d", minutes, seconds));

            seekBar.setMax(time);
            seekBar.setEnabled(true);
        }
    }

    private void setAudioUri() {
        isStart = true;
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setVolume(100, 100);
        String exists = new MediaHttp.Builder().setUrl(path).setMediaType(MediaHttp.MediaType.MUSIC).exists();
        if (exists == null) {
            time.setText("--:--");
            download.setVisibility(VISIBLE);
            playPauseButton.setVisibility(GONE);
            artistText.setVisibility(GONE);
            seekBar.setVisibility(VISIBLE);
        } else {
            String _path = path;
            path = exists;
            musicInfo(false);
            path = _path;
        }
        loading = true;
        home();
    }

    public void start() {
        if (!isStart) {
            AndroidUtilities.toast(errorMessage);
            return;
        }
        if (!isPause) {
            return;
        }
        isPause = false;
        MusicPlayerUtilities.MediaInfo.setPlaying(true);
        playPauseButton.setPlayed(true);
        if (loading) {
            playPauseButton.setVisibility(GONE);
            progressBar.setVisibility(VISIBLE);
            download.setVisibility(GONE);
            readOnline();
            return;
        }
        readOffline();
    }

    public boolean isLoading() {
        return loading;
    }

    private void readOffline() {
        seekBar.setVisibility(VISIBLE);
        artistText.setVisibility(GONE);
        musicInfo(true);
        if (isStart) {
            mediaPlayer.start();
            loadTimer();
//            playPauseButton.setPlayed(true);
        }
        if (onChangeListener != null) {
            onChangeListener.onPlayMusic(true);
        }
        createNotification();
    }

    private void readOnline() {
        if (downloading) {
            return;
        }
        downloading = true;
        String exists = new MediaHttp.Builder().setUrl(path).setMediaType(MediaHttp.MediaType.MUSIC).exists();
        if (exists != null) {
            onSuccessDownload(exists);
            return;
        }
        info.setText("");
        MusicPlayerUtilities.MediaInfo.setDownload("", "درحال بارگیری...", true);
        mediaHttp = new MediaHttp.Builder()
                .setUrl(path)
                .setMediaType(MediaHttp.MediaType.MUSIC)
                .request(new MediaHttp.ResponseConnection() {
                    @Override
                    public void onProgress(long length, long received) {
                        String r = AndroidUtilities.getTotalSpace(received);
                        String t = AndroidUtilities.getTotalSpace(length);
                        timeLast.setText(r);
                        time.setText(t);
                    }

                    @Override
                    public void onSuccess(String _path) {
                        onSuccessDownload(_path);
                    }

                    @Override
                    public void onError(final String error) {
                        isPause = true;
                        playPauseButton.setPlayed(false);
                        download.setVisibility(VISIBLE);
                        progressBar.setVisibility(GONE);
                        switch (error) {
                            case "Not found file":
                                isStart = false;
                                errorMessage = "فایلی برای پخش یافت نشد.";
                                break;
                            case "No space left on device":
                                isStart = false;
                                errorMessage = "حافظه ی دستگاه شما کافی نیست.";
                                break;
                            case "Download canceled":
                                errorMessage = "بارگیری لغو شده است.";
                                break;
                            default:
                                errorMessage = "شبکه در دسترس نیست.";
                                break;
                        }
                        MusicPlayerUtilities.MediaInfo.setPlaying(false);

                        MusicPlayerUtilities.MediaInfo.setDownload("خطا", errorMessage, false);
                        info.setText(errorMessage);

                        downloading = false;
                    }
                });
    }

    private void onSuccessDownload(String _path) {
        path = _path;
        loading = false;
        playPauseButton.setVisibility(VISIBLE);
        seekBar.setVisibility(VISIBLE);
        artistText.setVisibility(GONE);
        progressBar.setVisibility(GONE);
        //
        if (mediaPlayer != null) {
            try {
                musicInfo(false);
                mediaPlayer.setDataSource(path);
                mediaPlayer.prepare();
                if (isPause) {
                    playPauseButton.setPlayed(false);
                } else {
                    musicInfo(true);
                    if (onChangeListener != null) {
                        onChangeListener.onPlayMusic(true);
                    }
                    mediaPlayer.start();
                    loadTimer();
                }
                playPauseButton.setVisibility(VISIBLE);
                int time = mediaPlayer.getDuration() / 1000;

                int minutes = time / 60;
                int seconds = time - minutes * 60;

                MusicPlayer.this.time.setText(String.format("%02d:%02d", minutes, seconds));

                seekBar.setMax(time);
                seekBar.setEnabled(true);
                createNotification();
            } catch (Exception e) {
                isStart = false;
                isPause = true;
                playPauseButton.setPlayed(false);
                download.setVisibility(VISIBLE);
                errorMessage = "فایلی برای پخش یافت نشد.";
                MusicPlayerUtilities.MediaInfo.setPlaying(false);
                MusicPlayerUtilities.MediaInfo.setDownload("خطا", errorMessage, false);
                info.setText(errorMessage);
                FileLogs.e(e);
            }
        }
        downloading = false;
    }

    public void stop(boolean justCallBack) {
        if (justCallBack) {
            if (onChangeListener != null) {
                onChangeListener.onStopMusic();
            }
            return;
        }
        if (handler != null) {
            handler.removeCallbacks(runnable);
            handler = null;
        }
        isPause = true;
        try {
            mediaPlayer.stop();
            mediaPlayer.release();
            playPauseButton.setPlayed(false);
            timeLast.setText(String.format("%02d:%02d", 0, 0));
            MusicPlayerUtilities.MediaInfo.setPosition(0);
            if (onChangeListener != null) {
                onChangeListener.onStopMusic();
            }
        } catch (Exception e) {
            FileLogs.e(e);
        }
    }

    public void destroy() {
        stop(false);
        mediaPlayer = null;
        isStart = false;
    }

    public void pause() {
        if (!isStart) {
            AndroidUtilities.toast(errorMessage);
            return;
        }
        if (downloading) {
            if (mediaHttp != null) {
                mediaHttp.disconnect();
                mediaHttp = null;
            }
            downloading = false;
            return;
        }
        if (isPause) {
            return;
        }
        isPause = true;
        MusicPlayerUtilities.MediaInfo.setPlaying(false);
        if (mediaPlayer.isPlaying()) {
            try {
                if (handler != null) {
                    handler.removeCallbacks(runnable);
                    handler = null;
                }
                mediaPlayer.pause();
                playPauseButton.setPlayed(false);
                if (onChangeListener != null) {
                    onChangeListener.onPlayMusic(false);
                }
            } catch (Exception e) {
                FileLogs.e(e);
            }
        }
        if (!loading) createNotification();
    }

    private void loadTimer() {
        if (handler != null) {
            handler.removeCallbacks(runnable);
            handler = null;
        }
        handler = new Handler(Looper.getMainLooper());
        runnable.run();
    }

    @Override
    public void setBackground(Drawable background) {

        findViewById(R.id.back).setBackground(background);

    }

    public void setPath(String path) {
        this.path = path;
        if (path.startsWith("https://") || path.startsWith("http://")) {
            setAudioUri();
        } else {
            setAudioFromName();
        }
    }

    public boolean showStatusBar() {
        return !loading && seekBar.getVisibility() == VISIBLE;
    }

    public void hideProgressBar() {
        seekBar.setVisibility(GONE);
        artistText.setVisibility(VISIBLE);
    }

    public void seekTo(long pos) {
        if (!isStart) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mediaPlayer.seekTo(pos, MediaPlayer.SEEK_PREVIOUS_SYNC);
        } else {
            mediaPlayer.seekTo((int) pos);
        }
        MusicPlayerUtilities.MediaInfo.setPosition(pos);
        createNotification();
    }

    public interface OnChangeListener {
        void onRequestPlay(MusicPlayer musicPlayer);

        void onStopMusic();

        void onPlayMusic(boolean play);

        void onTimeMusic(String time);
    }
}
