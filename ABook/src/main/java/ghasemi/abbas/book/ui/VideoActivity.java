/*
 * Copyright (C) 2019  All rights reserved for FaraSource (ABBAS GHASEMI)
 * https://farasource.com
 */
package ghasemi.abbas.book.ui;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Looper;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import ghasemi.abbas.book.R;
import ghasemi.abbas.book.general.FileLogs;
import ghasemi.abbas.book.general.NotificationUtilities;
import ghasemi.abbas.book.service.MusicPlayerUtilities;
import ghasemi.abbas.book.support.com.universalvideoview.UniversalMediaController;
import ghasemi.abbas.book.support.com.universalvideoview.UniversalVideoView;

import ghasemi.abbas.book.general.AndroidUtilities;
import ghasemi.abbas.book.support.medialoader.MediaLoader;

public class VideoActivity extends AppCompatActivity {

    public static String path;
    public static String name;
    UniversalMediaController universalMediaController;
    private UniversalVideoView videoView;
    private int currentPosition;
    private boolean isFullscreen;
    private FrameLayout frameLayout;
    private MediaLoader mediaLoader;
    private boolean mediaPaused;
    private boolean allowStartMedia;

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt("seekTo", currentPosition);
        outState.putBoolean("mediaPaused", mediaPaused);
        outState.putBoolean("allowStartMedia", allowStartMedia);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentPosition = savedInstanceState.getInt("seekTo");
        mediaPaused = savedInstanceState.getBoolean("mediaPaused");
        allowStartMedia = savedInstanceState.getBoolean("allowStartMedia");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (!MusicPlayerUtilities.isEmpty() && !mediaPaused) {
            if (MusicPlayerUtilities.MediaInfo.isPlaying()) {
                MusicPlayerUtilities.get().pause();
                allowStartMedia = true;
            }
            mediaPaused = true;
            NotificationUtilities.deleteNotification(MusicPlayerUtilities.NOTIFY_MEDIA_PLAYER_ID);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(0xff000000);
            getWindow().setNavigationBarColor(0xff000000);
        } else {
            AndroidUtilities.setForceWindowFullScreen(this.getWindow());
        }
        super.onCreate(savedInstanceState);
        RelativeLayout relativeLayout = new RelativeLayout(this) {
            @Override
            public boolean onTouchEvent(MotionEvent event) {
                if (universalMediaController.isShowing())
                    event.setAction(MotionEvent.ACTION_CANCEL);
                universalMediaController.onTouchEvent(event);
                return super.onTouchEvent(event);
            }
        };
        relativeLayout.setGravity(Gravity.CENTER);
        relativeLayout.setBackgroundColor(0xff000000);
        relativeLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(relativeLayout);

        frameLayout = new FrameLayout(this);
        relativeLayout.addView(frameLayout, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, AndroidUtilities.dp(300)));
        universalMediaController = new UniversalMediaController(this);
        universalMediaController.setTitle(name);

        videoView = new UniversalVideoView(this);
        videoView.setFitXY(true);
        videoView.setAutoRotation(true);
        videoView.setMediaController(universalMediaController);
        videoView.setVideoViewCallback(new UniversalVideoView.VideoViewCallback() {
            @Override
            public void onScaleChange(boolean isFullscreen) {
                VideoActivity.this.isFullscreen = isFullscreen;

                if (isFullscreen) {
                    frameLayout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                } else {
                    frameLayout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, AndroidUtilities.dp(300)));
                }

            }

            @Override
            public void onPause(MediaPlayer mediaPlayer) {

            }

            @Override
            public void onStart(MediaPlayer mediaPlayer) {

            }

            @Override
            public void onBufferingStart(MediaPlayer mediaPlayer) {

            }

            @Override
            public void onBufferingEnd(MediaPlayer mediaPlayer) {

            }
        });

        if (path.startsWith("http://") || path.startsWith("https://")) {
            mediaLoader = MediaLoader.getInstance(this);
            videoView.setVideoURI(Uri.parse(mediaLoader
                    .getProxyUrl(path)));
        } else {
            videoView.setVideoPath("android.resource://" + getPackageName() + "/" + getResources().getIdentifier(name, "raw", getPackageName()));
        }

        frameLayout.addView(videoView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        frameLayout.addView(universalMediaController, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        try {
            PhoneStateListener phoneStateListener = new PhoneStateListener() {
                @Override
                public void onCallStateChanged(final int state, String incomingNumber) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (state == TelephonyManager.CALL_STATE_RINGING) {
                                videoView.pause();
                                currentPosition = videoView.getCurrentPosition();
                            } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                                videoView.start();
                                videoView.seekTo(currentPosition);
                            } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                                videoView.pause();
                                currentPosition = videoView.getCurrentPosition();
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

    @Override
    protected void onResume() {
        super.onResume();
        videoView.start();
        videoView.seekTo(currentPosition);
    }

    @Override
    protected void onDestroy() {
        if (mediaLoader != null) {
            mediaLoader.destroy();
            mediaLoader = null;
        }
        videoView.stopPlayback();
        videoView = null;
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoView.pause();
        currentPosition = videoView.getCurrentPosition();
    }

    @Override
    public void onBackPressed() {
        if (isFullscreen) {
            videoView.setFullscreen(false);
        } else {
            finish();
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        if (mediaPaused && !MusicPlayerUtilities.isEmpty() && !MusicPlayerUtilities.MediaInfo.isPlaying()) {
            if (allowStartMedia) {
                MusicPlayerUtilities.get().start();
            } else {
                MusicPlayerUtilities.createServiceNotification(false);
            }
        }
    }
}
