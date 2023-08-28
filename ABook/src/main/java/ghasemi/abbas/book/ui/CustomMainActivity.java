/*
 * Copyright (C) 2019  All rights reserved for FaraSource (ABBAS GHASEMI)
 * https://farasource.com
 */
package ghasemi.abbas.book.ui;

import android.app.ActivityOptions;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AlertDialog;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.Date;

import ghasemi.abbas.book.R;
import ghasemi.abbas.book.ui.components.MusicPlayer;
import ghasemi.abbas.book.ui.components.PlayPauseButton;
import ghasemi.abbas.book.ui.components.TextView;
import ghasemi.abbas.book.general.AndroidUtilities;
import ghasemi.abbas.book.ApplicationLoader;
import ghasemi.abbas.book.BuildApp;
import ghasemi.abbas.book.service.MusicPlayerUtilities;
import ghasemi.abbas.book.service.StudyTime;

public class CustomMainActivity extends BaseActivity implements MusicPlayer.OnChangeListener {
    long preTime;
    private DrawerLayout drawerLayout;
    //
    private RelativeLayout music;
    private TextView time;
    private PlayPauseButton playPauseButton;

    public static Bundle getAnimate() {
        ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(ApplicationLoader.context, R.anim.activity_top_in, R.anim.activity_top_out);
        return activityOptions.toBundle();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_custom_main);

        initNav();
        initMain();
    }

    private void initMain() {
        ImageView search = findViewById(R.id.search);
        search.setColorFilter(Color.WHITE);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CustomMainActivity.this, SearchActivity.class), getAnimate());
            }
        });

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CustomMainActivity.this, SeasonActivity.class);
                intent.putExtra("list_type", BuildApp.FIRST_SEASON_LIST_TYPE);
                startActivity(intent, getAnimate());
            }
        });

        findViewById(R.id.setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CustomMainActivity.this, SettingsActivity.class), getAnimate());
            }
        });
        findViewById(R.id.fav).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CustomMainActivity.this, FavoriteActivity.class), getAnimate());
            }
        });
        findViewById(R.id.about_us).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CustomMainActivity.this, PostActivity.class);
                intent.putExtra("post_type", "about_us");
                startActivity(intent, getAnimate());
            }
        });
        findViewById(R.id.comment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AndroidUtilities.commentApp(CustomMainActivity.this);
            }
        });
        findViewById(R.id.ref).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reference();
            }
        });

        music = findViewById(R.id.music);
        findViewById(R.id.close_music).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicPlayerUtilities.stopService(CustomMainActivity.this);
                MusicPlayerUtilities.destroy(true);
                music.setVisibility(View.GONE);
            }
        });
        time = findViewById(R.id.music_time);
        playPauseButton = findViewById(R.id.play_pause);
        findViewById(R.id.play_pause_click).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playPauseButton.isPlayed()) {
                    MusicPlayerUtilities.get().pause();
                } else {
                    MusicPlayerUtilities.get().start();
                }
            }
        });
    }

    private void initNav() {
        drawerLayout = findViewById(R.id.drawer_layout);
        findViewById(R.id.nav_setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CustomMainActivity.this, SettingsActivity.class), getAnimate());
                drawerLayout.closeDrawer(GravityCompat.END);
            }
        });

        findViewById(R.id.nav_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CustomMainActivity.this, SearchActivity.class), getAnimate());
                drawerLayout.closeDrawer(GravityCompat.END);
            }
        });
        findViewById(R.id.nav_fav).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CustomMainActivity.this, FavoriteActivity.class), getAnimate());
                drawerLayout.closeDrawer(GravityCompat.END);
            }
        });
        findViewById(R.id.nav_comment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AndroidUtilities.commentApp(CustomMainActivity.this);
                drawerLayout.closeDrawer(GravityCompat.END);
            }
        });
        findViewById(R.id.nav_about_us).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CustomMainActivity.this, PostActivity.class);
                intent.putExtra("post_type", "about_us");
                startActivity(intent, getAnimate());
                drawerLayout.closeDrawer(GravityCompat.END);
            }
        });
        findViewById(R.id.nav_ref).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reference();
                drawerLayout.closeDrawer(GravityCompat.END);
            }
        });
        findViewById(R.id.nav_time).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                studyTime();
                drawerLayout.closeDrawer(GravityCompat.END);
            }
        });

        findViewById(R.id.nav_exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawer(GravityCompat.END);
                finish();
            }
        });

        findViewById(R.id.menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.END);
            }
        });
    }

    private void studyTime() {
        String time = new StudyTime().getStudyTime();
        AndroidUtilities.setCustomFontDialog(new AlertDialog.Builder(this)
                .setTitle(getString(R.string.app_name))
                .setMessage(time)
                .setPositiveButton("باشه", null)
                .setNegativeButton("ریست", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new StudyTime().reset();
                    }
                })
                .show());
    }

    private void reference() {
        Intent intent = new Intent(CustomMainActivity.this, PostActivity.class);
        intent.putExtra("post_type", "references");
        startActivity(intent, getAnimate());
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
            return;
        }
        if (BuildApp.JUST_CLICK_FOR_EXIT) {
            finish();
            return;
        }
        if (BuildApp.DOUBLE_CLICK_FOR_EXIT) {
            long currentTime = new Date().getTime();
            if ((currentTime - preTime) > 2000) {
                preTime = currentTime;
            } else {
                finishAffinity();
            }
        } else {
            AndroidUtilities.setCustomFontDialog(new AlertDialog.Builder(this)
                    .setTitle("خروج از برنامه")
                    .setMessage("آیا می خواهید از برنامه خارج شوید؟")
                    .setPositiveButton("بله", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finishAffinity();
                        }
                    })
                    .setNegativeButton("خیر", null)
                    .show());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkMusic();
    }

    private void checkMusic() {
        for (MusicPlayerUtilities.ListMusicPlayer listMusicPlayer : MusicPlayerUtilities.getMusicPlayers()) {
            if (listMusicPlayer.musicPlayer.showStatusBar()) {
                playPauseButton.setPlayed(MusicPlayerUtilities.MediaInfo.isPlaying());
                music.setVisibility(View.VISIBLE);
                onTimeMusic(listMusicPlayer.musicPlayer.getTimeLast());
            }
            listMusicPlayer.musicPlayer.setOnChangeListener(this);
        }
    }

    @Override
    public void onRequestPlay(MusicPlayer musicPlayer) {

    }

    @Override
    public void onStopMusic() {
        music.setVisibility(View.GONE);
    }

    @Override
    public void onPlayMusic(boolean play) {
        playPauseButton.setPlayed(play);
        if (music.getVisibility() == View.GONE) {
            music.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onTimeMusic(String _time) {
        time.setText(_time);
    }
}