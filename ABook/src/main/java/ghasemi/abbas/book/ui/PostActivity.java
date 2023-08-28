/*
 * Copyright (C) 2019  All rights reserved for FaraSource (ABBAS GHASEMI)
 * https://farasource.com
 */
package ghasemi.abbas.book.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;

import android.os.Looper;
import android.text.Layout;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.kyleduo.switchbutton.SwitchButton;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ghasemi.abbas.book.R;
import ghasemi.abbas.book.ui.components.Button;
import ghasemi.abbas.book.ui.components.GifView;
import ghasemi.abbas.book.ui.components.Slider;
import ghasemi.abbas.book.ui.components.VideoPlayer;
import ghasemi.abbas.book.general.AndroidUtilities;
import ghasemi.abbas.book.general.FileLogs;
import ghasemi.abbas.book.BuildApp;
import ghasemi.abbas.book.service.MusicPlayerUtilities;
import ghasemi.abbas.book.general.TinyData;
import ghasemi.abbas.book.service.StudyTime;
import ghasemi.abbas.book.sqlite.BookDB;
import ghasemi.abbas.book.sqlite.ResultRequest;
import ghasemi.abbas.book.sqlite.SqlModel;
import ghasemi.abbas.book.ui.components.MusicPlayer;
import ghasemi.abbas.book.ui.components.PlayPauseButton;
import ghasemi.abbas.book.ui.components.TextView;
import ghasemi.abbas.book.support.Glide;
import ghasemi.abbas.book.support.customtabs.CustomTabsIntent;

public class PostActivity extends BaseActivity implements MusicPlayer.OnChangeListener {

    private ImageView fav;
    private boolean isFav;
    private FrameLayout frameLayout;
    private LinearLayout linearLayout;
    private final List<TextView> textViews = new ArrayList<>();
    private final List<View> dividers = new ArrayList<>();
    private View progressBar;
    private int color = Color.BLACK, divider;
    //
    private BottomSheetDialog bottomSheetDialog;
    private TextView font, tFontSize, tSpace;
    private Drawable backMP;
    private IndicatorSeekBar fontSize, space;
    private SwitchButton filter;
    private View settingsDialog;
    private NestedScrollView nestedScrollView;
    private View white, dark, sepia;
    private StudyTime studyTime;
    private boolean finishPars;
    private String id;
    //
    private RelativeLayout music;
    private RelativeLayout search;
    private TextView time;
    private TextView searchText;
    private PlayPauseButton playPauseButton;

    private ArrayList<String> query;

    private View blueLightFilter;
    public static boolean runningPostActivity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        String post_type = getIntent().getStringExtra("post_type");
        if (post_type == null) {
            AndroidUtilities.setWindowFullScreen(this);
        }
        fav = findViewById(R.id.fav);
        fav.setColorFilter(Color.WHITE);

        TextView title = findViewById(R.id.title);
        frameLayout = findViewById(R.id.frame);
        linearLayout = findViewById(R.id.linearLayout);
        progressBar = findViewById(R.id.progressBar);
        blueLightFilter = findViewById(R.id.blue_light_filter);
        ImageView setting = findViewById(R.id.setting);
        setting.setColorFilter(Color.WHITE);
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (finishPars) {
                    settingView();
                }
            }
        });

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        switch (TinyData.getInstance().getString("postBackground", "0")) {
            case "0":
                backMP = getResources().getDrawable(R.drawable.music_player_bg);
                frameLayout.setBackgroundColor(0xffffffff);
                progressBar.setBackgroundColor(0xffffffff);
                divider = 0xffe2e2e2;
                break;
            case "1":
                backMP = getResources().getDrawable(R.drawable.music_player_sepia_bg);
                frameLayout.setBackgroundColor(0xffFCF1D1);
                progressBar.setBackgroundColor(0xffFCF1D1);
                divider = 0XFFFFCCBC;
                break;
            case "2":
                backMP = getResources().getDrawable(R.drawable.music_player_dark_bg);
                frameLayout.setBackgroundColor(0xff333333);
                progressBar.setBackgroundColor(0xff333333);
                color = Color.WHITE;
                divider = 0xFF797979;
                break;
            default:

        }

        if (TinyData.getInstance().getBool("windowFilterScreen")) {
            startServiceMode();
        }

        music = findViewById(R.id.music);
        search = findViewById(R.id.search);
        findViewById(R.id.close_music).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicPlayerUtilities.get().pause();
                MusicPlayerUtilities.get().hideProgressBar();
                MusicPlayerUtilities.stopService(PostActivity.this);
                music.setVisibility(View.GONE);
                nestedScrollView.setPadding(0, 0, 0, 0);
            }
        });
        findViewById(R.id.close_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cleanSpanFromText();
                search.setVisibility(View.GONE);
                if (music.getVisibility() == View.GONE)
                    nestedScrollView.setPadding(0, 0, 0, 0);
            }
        });
        time = findViewById(R.id.music_time);
        searchText = findViewById(R.id.search_text);
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
        nestedScrollView = findViewById(R.id.nestedScrollView);
        if (post_type == null) {
            if (BuildApp.ACCESS_SHARE) {
                ImageView share = findViewById(R.id.share);
                share.setVisibility(View.VISIBLE);
                share.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (finishPars) {
                            StringBuilder ts = new StringBuilder();
                            for (TextView textView : textViews) {
                                ts.append(textView.getText()).append("\n");
                            }
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.putExtra(Intent.EXTRA_TEXT, ts.toString());
                            intent.setType("text/*");
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(Intent.createChooser(intent, "اشتراک گذاری متن ..."));
                        }
                    }
                });
            }
            id = getIntent().getExtras().getString("id");
            fav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (finishPars) {
                        Animation animation = AnimationUtils.loadAnimation(PostActivity.this, R.anim.up);
                        if (isFav) {
                            isFav = false;
                            fav.setImageResource(R.drawable.ic_favorite);
                            BookDB.getInstance().removePostFavorite(id);
                        } else {
                            isFav = true;
                            fav.setImageResource(R.drawable.ic_favorite_full);
                            BookDB.getInstance().addPostFavorite(id);
                        }
                        fav.startAnimation(animation);
                        if (FavoriteActivity.refFavorite != null) {
                            FavoriteActivity.refFavorite.onChange(isFav, getIntent().getExtras().getInt("fav_pos_id"));
                        }
                    }
                }
            });
            title.setText(getIntent().getExtras().getString("title"));
            BookDB.getInstance().post(id, new ResultRequest() {
                @Override
                public void onSuccess(final List<SqlModel> sqlModels) {
                    String q = getIntent().getStringExtra("query");
                    if (q != null) {
                        query = new ArrayList<>(Arrays.asList(q.split(",")));
                    }
                    PostActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            title.setText(sqlModels.get(0).getTitle());
                            fav.setVisibility(View.VISIBLE);
                            if (sqlModels.get(0).isFavorite()) {
                                isFav = true;
                                fav.setImageResource(R.drawable.ic_favorite_full);
                            }
                            (new Handler(Looper.getMainLooper())).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        checkContent(sqlModels.get(0).getContent());
                                        studyTime = new StudyTime();
                                        studyTime.startTask();
                                    } catch (Exception e) {
                                        FileLogs.e(e);
                                        AndroidUtilities.toast("خطایی رخ داد.");
                                        finish();
                                    }
                                }
                            }, 100);
                        }
                    });
                }

                @Override
                public void onFail() {
                    PostActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AndroidUtilities.toast("پیدا نشد.");
                            finish();
                        }
                    });
                }
            });
        } else {
            BookDB.getInstance().single(post_type, new ResultRequest() {
                @Override
                public void onSuccess(final List<SqlModel> sqlModels) {
                    PostActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            title.setText(sqlModels.get(0).getTitle());
                            (new Handler(Looper.getMainLooper())).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        checkContent(sqlModels.get(0).getContent());
                                    } catch (Exception e) {
                                        FileLogs.e(e);
                                    }
                                }
                            }, 100);
                        }
                    });
                }

                @Override
                public void onFail() {
                    PostActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AndroidUtilities.toast("پیدا نشد.");
                            finish();
                        }
                    });
                }
            });
        }
    }

    private void startServiceMode() {
        blueLightFilter.setVisibility(View.VISIBLE);
    }

    private void destroyServiceMode() {
        blueLightFilter.setVisibility(View.GONE);
    }

    @SuppressLint("WrongConstant")
    private void startPars(String src) {
        String type = AndroidUtilities.getViewModel(src);
        switch (type) {
            case "img": {
                src = AndroidUtilities.cleanTag(src, type);
                View view = LayoutInflater.from(this).inflate(R.layout.post_image, null);
                ImageView imageView = view.findViewById(R.id.image_id);
                ImageView.ScaleType scaleType = null;
                if (src.contains("[o]")) {
                    src = src.replace("[o]", "");
                    scaleType = ImageView.ScaleType.CENTER_INSIDE;
                }
                if (src.startsWith("[z]")) {
                    src = src.substring(3);
                    Intent intent = new Intent(PostActivity.this, ghasemi.abbas.book.ui.components.ImageView.class);
                    intent.putExtra("urlOrId", AndroidUtilities.getSrcFromTag(src));
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(intent);
                        }
                    });
                }
                String urlOrId = AndroidUtilities.getSrcFromTag(src);
                if (urlOrId.startsWith("http://") || urlOrId.startsWith("https://")) {
                    Glide.with(this).setScaleType(scaleType).load(urlOrId).into(imageView);
                } else {
                    if (scaleType != null) {
                        imageView.setAdjustViewBounds(false);
                        imageView.setScaleType(scaleType);
                    }
                    imageView.setImageResource(AndroidUtilities.getImageId(urlOrId));
                }
                linearLayout.addView(view);
                break;
            }
            case "slider":
                src = AndroidUtilities.cleanTag(src, type);
                Slider slider = new Slider(PostActivity.this, false);
                if (src.startsWith("[z]")) {
                    src = src.substring(3).trim();
                    slider.setZoom();
                }
                ArrayList<String> arrayList = new ArrayList<>();
                while (src.startsWith("[")) {
                    int i = src.indexOf("]");
                    if (i == -1) {
                        throw new RuntimeException("[slider]--> " + src);
                    }
                    arrayList.add(src.substring(1, i));
                    src = src.substring(i + 1).trim();
                }
                slider.addItem(arrayList);
                slider.create();
                linearLayout.addView(slider.toView());
                break;
            case "gif": {
                src = AndroidUtilities.getSrcFromTag(AndroidUtilities.cleanTag(src, type));
                View view = LayoutInflater.from(this).inflate(R.layout.post_gif, null);
                GifView gifView = view.findViewById(R.id.gif_id);
                gifView.setImageResource(AndroidUtilities.getImageId(src));
                linearLayout.addView(view);
                break;
            }
            case "voice":
                src = AndroidUtilities.getSrcFromTag(AndroidUtilities.cleanTag(src, type));
                MusicPlayerUtilities.ListMusicPlayer listMusicPlayer = MusicPlayerUtilities.hasTag(src);
                MusicPlayer musicPlayer;
                if (listMusicPlayer == null) {
                    musicPlayer = (MusicPlayer) LayoutInflater.from(this).inflate(R.layout.post_voice, null);
                    musicPlayer.setPath(src);
                    MusicPlayerUtilities.add(new MusicPlayerUtilities.ListMusicPlayer(musicPlayer, src));
                } else {
                    musicPlayer = listMusicPlayer.musicPlayer;
                    try {
                        ((ViewGroup) musicPlayer.getParent()).removeView(musicPlayer);
                    } catch (Exception e) {
                        //
                    }
                    MusicPlayerUtilities.add(listMusicPlayer);
                }
                musicPlayer.setBackground(backMP);
                musicPlayer.setOnChangeListener(this);
                linearLayout.addView(musicPlayer);
                break;
            case "video": {
                src = AndroidUtilities.getSrcFromTag(AndroidUtilities.cleanTag(src, type));
                String[] part = AndroidUtilities.split(src, "\\|");
                View view = LayoutInflater.from(this).inflate(R.layout.post_video, null);
                VideoPlayer videoView = view.findViewById(R.id.video_id);
                videoView.setUri(part[0]);
                if (part.length > 1) {
                    videoView.setName(part[1]);
                }
                linearLayout.addView(view);
                break;
            }
            case "divider": {
                src = AndroidUtilities.getSrcFromTag(AndroidUtilities.cleanTag(src, type));
                View view = LayoutInflater.from(this).inflate(R.layout.post_divider, null);
                View divider_view = view.findViewById(R.id.divider_id);
                Drawable drawable;
                if (src.equals("0")) {
                    drawable = getResources().getDrawable(R.drawable.divider);
                } else {
                    drawable = getResources().getDrawable(R.drawable.divider_dash);
                }
                drawable.setColorFilter(divider, PorterDuff.Mode.SRC_IN);
                divider_view.setBackground(drawable);
                dividers.add(divider_view);
                linearLayout.addView(view);
                break;
            }
            case "ref": {
                src = AndroidUtilities.getSrcFromTag(AndroidUtilities.cleanTag(src, type));
                final String[] part = AndroidUtilities.split(src, "\\|");
                View view = LayoutInflater.from(this).inflate(R.layout.post_button, null);
                Button reference = view.findViewById(R.id.button_id);
                reference.setMinimumHeight(AndroidUtilities.dp(55));
                reference.setText(part[0]);
                reference.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        switch (part[2]) {
                            case "send":
                                intent.setAction(Intent.ACTION_SEND);
                                if (part[1].startsWith("id:")) {
                                    try {
                                        StringBuilder text = new StringBuilder();
                                        for (String s : part[1].substring(3).split(",")) {
                                            text.append(textViews.get(Integer.parseInt(s) - 1).getText()).append("\n");
                                        }
                                        intent.putExtra(Intent.EXTRA_TEXT, text.toString());
                                    } catch (Exception e) {
                                        FileLogs.e(e);
                                        AndroidUtilities.toast("not found id");
                                    }
                                } else {
                                    intent.putExtra(Intent.EXTRA_TEXT, part[1]);
                                }
                                intent.setType("text/*");
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                if (part.length > 4) {
                                    intent = Intent.createChooser(intent, part[4]);
                                }
                                break;
                            case "web":
                            case "view":
                                if (BuildApp.OPEN_LINK_IN_APP && part[2].equals("web")) {
                                    Uri uri = Uri.parse(part[1]);
                                    CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();
                                    intentBuilder.setShowTitle(true);
                                    intentBuilder.setToolbarColor(getResources().getColor(R.color.colorPrimary));
                                    intentBuilder.addDefaultShareMenuItem();
                                    intentBuilder.enableUrlBarHiding();
                                    CustomTabsIntent customTabsIntent = intentBuilder.build();
                                    customTabsIntent.setUseNewTask();
                                    customTabsIntent.launchUrl(PostActivity.this, uri);
                                    intent = null;
                                } else {
                                    intent.setAction(Intent.ACTION_VIEW);
                                    if (part[1].startsWith("sms")) {
                                        intent.putExtra("address", new String[]{part[1].substring(6)});
                                    }
                                    intent.setData(Uri.parse(part[1]));
                                    if (part.length > 4) {
                                        String[] sub = AndroidUtilities.split(part[4], "\\^");
                                        intent.putExtra(sub[0], sub[1]);
                                    }
                                }
                                break;
                            case "open":
                                intent = getPackageManager().getLaunchIntentForPackage(part[3]);
                                if (intent == null) {
                                    AndroidUtilities.toast("خطا: برنامه هدف پیدا نشد.");
                                }
                                break;
                            case "edit":
                                intent.setAction(Intent.ACTION_EDIT);
                                intent.setData(Uri.parse(part[1]));
                                break;
                            default:
                                intent.setAction(part[2]);
                                if (!part[1].isEmpty()) {
                                    intent.setData(Uri.parse(part[1]));
                                }
                        }

                        if (intent != null) {
                            if (part.length > 3 && !part[3].isEmpty()) {
                                intent.setPackage(part[3]);
                            }
                            try {
                                startActivity(intent);
                            } catch (Exception e) {
                                FileLogs.e(e);
                                AndroidUtilities.toast("خطا: برنامه هدف پیدا نشد.");
                            }
                        }
                    }
                });
                linearLayout.addView(view);
                break;
            }
            case "dialog": {
                src = AndroidUtilities.getSrcFromTag(AndroidUtilities.cleanTag(src, type));
                final String[] part = AndroidUtilities.split(src, "\\|");
                View view = LayoutInflater.from(this).inflate(R.layout.post_button, null);
                Button dialog = view.findViewById(R.id.button_id);
                dialog.setMinimumHeight(AndroidUtilities.dp(55));
                dialog.setText(part[0]);
                dialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AndroidUtilities.setCustomFontDialog(new AlertDialog.Builder(PostActivity.this)
                                .setTitle(part[1])
                                .setMessage(part[2])
                                .setPositiveButton("باشه", null)
                                .show());
                    }
                });
                linearLayout.addView(view);
                break;
            }
            case "toast": {
                src = AndroidUtilities.getSrcFromTag(AndroidUtilities.cleanTag(src, type));
                final String[] part = AndroidUtilities.split(src, "\\|");
                View view = LayoutInflater.from(this).inflate(R.layout.post_button, null);
                Button toast = view.findViewById(R.id.button_id);
                toast.setMinimumHeight(AndroidUtilities.dp(55));
                toast.setText(part[0]);
                toast.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AndroidUtilities.toast(part[1]);
                    }
                });
                linearLayout.addView(view);
                break;
            }
            case "jump": {
                src = AndroidUtilities.getSrcFromTag(AndroidUtilities.cleanTag(src, type));
                final String[] part = AndroidUtilities.split(src, "\\|");
                View view = LayoutInflater.from(this).inflate(R.layout.post_button, null);
                Button jump = view.findViewById(R.id.button_id);
                jump.setMinimumHeight(AndroidUtilities.dp(55));
                jump.setText(part[0]);
                jump.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BookDB.getInstance().requestTypeFromId(part[1], new ResultRequest() {
                            @Override
                            public void onSuccess(List<SqlModel> sqlModels) {
                                if (sqlModels.get(0).getPostType().equals("jump")) {
                                    AndroidUtilities.toast("خطا: از حالت جامپ نمی توان به جامپ دیگری رفت.");
                                    return;
                                }
                                SeasonActivity.openActionPost(PostActivity.this, sqlModels.get(0), 0, true);
//                                    if (MusicPlayerUtilities.getMusicPlayerID() != -1) {
//                                        MusicPlayerUtilities.get().pause();
//                                    }
                            }

                            @Override
                            public void onFail() {
                                AndroidUtilities.toast(String.format("خطا: ای دی %s پیدا نشد.", part[1]));
                            }
                        });
                    }
                });
                linearLayout.addView(view);
                break;
            }
            case "alert": {
                src = AndroidUtilities.cleanTag(src, type);
                View view = LayoutInflater.from(this).inflate(R.layout.post_alert, null);
                View back = view.findViewById(R.id.back_id);
                ImageView img = view.findViewById(R.id.image_id);
                switch (AndroidUtilities.getViewModel(src)) {
                    case "green":
                        src = AndroidUtilities.cleanTag(src, "green");
                        back.setBackgroundResource(R.drawable.alert_message_green);
                        img.setImageResource(R.drawable.ic_checked);
                        break;
                    case "blue":
                        src = AndroidUtilities.cleanTag(src, "blue");
                        back.setBackgroundResource(R.drawable.alert_message_blue);
                        img.setImageResource(R.drawable.ic_info);
                        break;
                    case "red":
                        src = AndroidUtilities.cleanTag(src, "red");
                        back.setBackgroundResource(R.drawable.alert_message_red);
                        img.setImageResource(R.drawable.ic_danger);
                        break;
                    default:
                        src = AndroidUtilities.cleanTag(src, "orange");
                        back.setBackgroundResource(R.drawable.alert_message_orange);
                        img.setImageResource(R.drawable.ic_warning);
                        break;
                }
                TextView textView = view.findViewById(R.id.message_id);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, AndroidUtilities.getTextSize());
                textView.setText(AndroidUtilities.getSrcFromTag(src).trim());
                textView.disableNormalColor();
                textView.setTextBold();
                textView.setTypeTextView("bold");
                textViews.add(textView);
                linearLayout.addView(view);
                break;
            }
            case "copy": {
                src = AndroidUtilities.getSrcFromTag(AndroidUtilities.cleanTag(src, type));
                final String[] part = AndroidUtilities.split(src, "\\|");
                View view = LayoutInflater.from(this).inflate(R.layout.post_button, null);
                Button copy = view.findViewById(R.id.button_id);
                copy.setText(part[0]);
                copy.setMinimumHeight(AndroidUtilities.dp(55));
                copy.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (part[1].startsWith("id:")) {
                            try {
                                StringBuilder text = new StringBuilder();
                                for (String s : part[1].substring(3).split(",")) {
                                    text.append(textViews.get(Integer.parseInt(s) - 1).getText()).append("\n");
                                }
                                AndroidUtilities.addToClipboard(text.toString());
                            } catch (Exception e) {
                                FileLogs.e(e);
                                AndroidUtilities.toast("not found id");
                            }
                        } else {
                            AndroidUtilities.addToClipboard(part[1]);
                        }
                        AndroidUtilities.toast("کپی شد.");
                    }
                });
                linearLayout.addView(view);
                break;
            }
            case "tipbox": {
                src = AndroidUtilities.getSrcFromTag(AndroidUtilities.cleanTag(src, type));
                final String[] part = AndroidUtilities.split(src, "\\|");
                View view = LayoutInflater.from(this).inflate(R.layout.post_tip_box, null);
                CardView cardView = view.findViewById(R.id.card_id);
                ImageView imageView = view.findViewById(R.id.image_id);
                TextView title = view.findViewById(R.id.title_id);
                title.disableNormalColor();
                title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, AndroidUtilities.getTextSize());
                TextView message = view.findViewById(R.id.message_id);
                message.setTextSize(TypedValue.COMPLEX_UNIT_DIP, AndroidUtilities.getTextSize());
                message.disableNormalColor();
                switch (part[0]) {
                    case "blue":
                    case "red":
                    case "green":
                    case "yellow":
                    case "orange":
                    case "purple":
                        cardView.setCardBackgroundColor(AndroidUtilities.getColorByName(part[0])[0]);
                        break;
                    default:
                        cardView.setCardBackgroundColor(Color.parseColor(part[0]));
                }
                imageView.setImageResource(AndroidUtilities.getImageId(part[1]));
                title.setText(part[2]);
                title.setTextBold();
                title.setTypeTextView("bold");
                message.setText(part[3]);
                textViews.add(title);
                textViews.add(message);
                linearLayout.addView(view);
                break;
            }
            default: {
                LinearLayout view = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.post_text, null);
                final TextView textView = view.findViewById(R.id.text_id);
                textView.setTextIsSelectable(BuildApp.ACCESS_COPY);
                textView.setTypeface(AndroidUtilities.getLightTypeFace());
                textView.setLineSpacing(0, SettingsActivity.getSpace());
                textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, AndroidUtilities.getTextSize());
                boolean html = false;
                int marginH = 0;
                while (src.startsWith("[")) {
                    String type2 = AndroidUtilities.getViewModel(src);
                    switch (type2) {
                        case "blue":
                        case "red":
                        case "green":
                        case "yellow":
                        case "orange":
                        case "purple":
                            src = AndroidUtilities.cleanTag(src, type2);
                            textView.disableNormalColor();
                            textView.setTextColors(AndroidUtilities.getColorByName(type2));
                            textView.colorDayNight(!TinyData.getInstance().getString("postBackground", "0").equals("2"));
                            break;
                        case "l":
                            src = AndroidUtilities.cleanTag(src, type2);
                            textView.setGravity(Gravity.LEFT);
                            break;
                        case "p":
                            src = AndroidUtilities.cleanTag(src, type2);
                            marginH += 10;
                            break;
                        case "r":
                            src = AndroidUtilities.cleanTag(src, type2);
                            textView.setGravity(Gravity.RIGHT);
                            break;
                        case "c":
                            src = AndroidUtilities.cleanTag(src, type2);
                            textView.setGravity(Gravity.CENTER);
                            break;
                        case "j":
                            src = AndroidUtilities.cleanTag(src, type2);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                textView.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
                            }
                            break;
                        case "b":
                            src = AndroidUtilities.cleanTag(src, type2);
                            textView.setTextBold();
                            textView.setTypeTextView("bold");
                            break;
                        case "alm":
                            src = AndroidUtilities.cleanTag(src, type2);
                            textView.setAutoLinkMask(Linkify.ALL);
                            break;
                        case "html":
                            src = AndroidUtilities.cleanTag(src, type2);
                            textView.setMovementMethod(LinkMovementMethod.getInstance());
                            html = true;
                            break;
                        case "sh":
                            src = AndroidUtilities.cleanTag(src, type2);
                            ImageView share = view.findViewById(R.id.share_id);
                            ImageView copy = view.findViewById(R.id.copy_id);
                            share.setVisibility(View.VISIBLE);
                            copy.setVisibility(View.VISIBLE);
                            share.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(Intent.ACTION_SEND);
                                    intent.putExtra(Intent.EXTRA_TEXT, textView.getText().toString());
                                    intent.setType("text/*");
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    try {
                                        startActivity(intent);
                                    } catch (Exception e) {
                                        FileLogs.e(e);
                                        AndroidUtilities.toast("خطا: برنامه هدف پیدا نشد.");
                                    }
                                }
                            });
                            copy.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    AndroidUtilities.addToClipboard(textView.getText().toString());
                                    AndroidUtilities.toast("کپی شد.");
                                }
                            });
                            share.setColorFilter(getResources().getColor(R.color.colorAccent));
                            copy.setColorFilter(getResources().getColor(R.color.colorAccent));
                            break;
                        default:
                            if (src.startsWith("[#")) {
                                int i = src.indexOf("]");
                                String color = src.substring(1, i);
                                src = src.substring(i + 1);
                                textView.disableNormalColor();
                                String[] p = AndroidUtilities.split(color, "\\|");
                                if (p.length == 2) {
                                    textView.setTextColors(new int[]{
                                            Color.parseColor(p[0]),
                                            Color.parseColor(p[1])
                                    });
                                    textView.colorDayNight(!TinyData.getInstance().getString("postBackground", "0").equals("2"));
                                } else {
                                    textView.setTextColor(Color.parseColor(color));
                                }
                                textView.setTextColor(Color.parseColor(color));
                            } else if (src.startsWith("[s:")) {
                                int i = src.indexOf(']');
                                int s = Integer.parseInt(src.substring(3, i));
                                textView.setDifferentTextSize(s);
                                src = src.substring(i + 1);
                            } else if (src.startsWith("[f:")) {
                                int i = src.indexOf(']');
                                try {
                                    textView.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/" + src.substring(3, i) + ".ttf"));
                                    textView.setTypeTextView("customFont");
                                    src = src.substring(i + 1);
                                } catch (Exception e) {
                                    FileLogs.print("not found font");
                                    break;
                                }
                            } else {
                                int i = src.indexOf(']');
                                if (i != -1) {
                                    src = src.substring(i + 1);
                                } else if (src.length() > 1) {
                                    src = src.substring(1);
                                } else {
                                    src = "";
                                }
                                break;
                            }
                            break;
                    }
                    src = src.trim();
                }
                if (textView.isNormalColor()) {
                    textView.setTextColor(color);
                }
                if (html) {
                    textView.setTextHtml(src);
                } else {
                    textView.setText(src);
                }
                if (marginH > 0) {
                    marginH = AndroidUtilities.dp(marginH);
                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) textView.getLayoutParams();
                    layoutParams.rightMargin = marginH;
                    layoutParams.leftMargin = marginH;
                }
                linearLayout.addView(view);
                textViews.add(textView);
                break;
            }
        }
    }

    private void checkContent(String content) {
        AndroidUtilities.split(content, new AndroidUtilities.Response() {
            @Override
            public void onSrc(String src) {
                startPars(src);
            }
        });
        checkMusic();
        checkQuerySearch();
        checkLocationScroll();
        cancelProgress();
        finishPars = true;
    }

    private void cancelProgress() {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Animation fade_out = AnimationUtils.loadAnimation(PostActivity.this, R.anim.fade_out);
                fade_out.setDuration(500);
                fade_out.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                progressBar.startAnimation(fade_out);
            }
        }, 0);
    }

    private void checkQuerySearch() {
        if (query != null) {
            int i = 0;
            for (TextView textView : textViews) {
                for (String part : query) {
                    i += textView.setHighLighted(part);
                }
            }
            if (i != 0) {
                searchText.setText(String.format("عبارت '%s' %s بار یافت شد.", query.get(0), i));
                if (search.getVisibility() == View.GONE) {
                    search.setVisibility(View.VISIBLE);
                    nestedScrollView.setPadding(0, AndroidUtilities.dp(32), 0, 0);
                }
            }
        }
    }

    private void checkMusic() {
        for (MusicPlayerUtilities.ListMusicPlayer listMusicPlayer : MusicPlayerUtilities.getMusicPlayers()) {
            if (listMusicPlayer.musicPlayer.showStatusBar()) {
                playPauseButton.setPlayed(MusicPlayerUtilities.MediaInfo.isPlaying());
                music.setVisibility(View.VISIBLE);
                nestedScrollView.setPadding(0, AndroidUtilities.dp(32), 0, 0);
                onTimeMusic(listMusicPlayer.musicPlayer.getTimeLast());
                listMusicPlayer.musicPlayer.setOnChangeListener(this);
                break;
            }
        }
    }

    private void checkLocationScroll() {
        if (TinyData.getInstance().getBool("loadLastLocationRead", true)) {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    nestedScrollView.setScrollY(TinyData.getInstance().getInt("scroll_" + id));
                    nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
                        @Override
                        public void onScrollChange(NestedScrollView nestedScrollView, int i, int i1, int i2, int i3) {
                            TinyData.getInstance().putInt("scroll_" + id, i1);
                        }
                    });
                }
            }, 0);
        }
    }

    @Override
    protected void onResume() {
        if (studyTime != null) {
            studyTime.startTask();
        }
        runningPostActivity = true;
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (studyTime != null) {
            studyTime.finish();
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (studyTime != null) {
            studyTime.finish();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (studyTime != null) {
            studyTime.finish();
        }
        runningPostActivity = false;
        if (MusicPlayerUtilities.getMusicPlayerID() != -1 && !MusicPlayerUtilities.isEmpty() && MusicPlayerUtilities.get().showStatusBar()) {
            MusicPlayerUtilities.destroy(false);
        } else {
            MusicPlayerUtilities.stopService(this);
            MusicPlayerUtilities.destroy(true);
        }
        super.onDestroy();
    }

    private void settingView() {
        if ((bottomSheetDialog == null) || !bottomSheetDialog.isShowing()) {
            bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
            settingsDialog = getLayoutInflater().inflate(R.layout.dialog_setting, null);
            bottomSheetDialog.setContentView(settingsDialog);
            bottomSheetDialog.show();

            font = bottomSheetDialog.findViewById(R.id.font);
            tFontSize = bottomSheetDialog.findViewById(R.id.t_font_size);
            tSpace = bottomSheetDialog.findViewById(R.id.t_space);

            fontSize = bottomSheetDialog.findViewById(R.id.font_size);
            space = bottomSheetDialog.findViewById(R.id.space);

            filter = bottomSheetDialog.findViewById(R.id.light_filter);

            white = bottomSheetDialog.findViewById(R.id.white);
            dark = bottomSheetDialog.findViewById(R.id.dark);
            sepia = bottomSheetDialog.findViewById(R.id.sepia);

            initContentDialog();

            initOnClickDialog();
        }
    }

    private void dialogFont() {
        if ((bottomSheetDialog == null) || !bottomSheetDialog.isShowing()) {
            bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
            bottomSheetDialog.setContentView(getLayoutInflater().inflate(R.layout.dialog_select_font, null));
            bottomSheetDialog.show();
            bottomSheetDialog.findViewById(R.id.font_1).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TinyData.getInstance().putString("fontType", "0");
                    AndroidUtilities.setTypeFont();
                    font.setText(getResources().getString(R.string.font_name_1).replace(" ", " : "));
                    setTypeFont();
                }
            });

            bottomSheetDialog.findViewById(R.id.font_2).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TinyData.getInstance().putString("fontType", "1");
                    AndroidUtilities.setTypeFont();
                    setTypeFont();
                    font.setText(getResources().getString(R.string.font_name_2).replace(" ", " : "));
                }
            });

            bottomSheetDialog.findViewById(R.id.font_3).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TinyData.getInstance().putString("fontType", "2");
                    AndroidUtilities.setTypeFont();
                    font.setText(getResources().getString(R.string.font_name_3).replace(" ", " : "));
                    setTypeFont();
                }
            });

            bottomSheetDialog.findViewById(R.id.font_4).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TinyData.getInstance().putString("fontType", "3");
                    AndroidUtilities.setTypeFont();
                    font.setText(getResources().getString(R.string.font_name_4).replace(" ", " : "));
                    setTypeFont();
                }
            });
        }
    }

    private void initContentDialog() {

        switch (TinyData.getInstance().getString("fontType", "-1")) {
            case "0":
                font.setText(getResources().getString(R.string.font_name_1).replace(" ", " : "));
                break;
            case "1":
                font.setText(getResources().getString(R.string.font_name_2).replace(" ", " : "));
                break;
            case "2":
                font.setText(getResources().getString(R.string.font_name_3).replace(" ", " : "));
                break;
            case "3":
                font.setText(getResources().getString(R.string.font_name_4).replace(" ", " : "));
                break;
            default:
                font.setText(BuildApp.FONT_TYPE.getName());
        }

        fontSize.setMax(BuildApp.MAXIMUM_FONT_SIZE);
        fontSize.setMin(BuildApp.MINIMUM_FONT_SIZE);
        fontSize.setProgress(TinyData.getInstance().getInt("systemFontSize", BuildApp.DEFAULT_FONT_SIZE));
        space.setMax(10);
        space.setMin(1);
        space.setProgress(TinyData.getInstance().getInt("fontSpace", 1));

        filter.setChecked(TinyData.getInstance().getBool("windowFilterScreen", false));
        switch (TinyData.getInstance().getString("postBackground", "0")) {
            case "0":
                white.setBackgroundResource(R.drawable.white_sel_bg);
                break;
            case "1":
                sepia.setBackgroundResource(R.drawable.sepia_sel_bg);
                break;
            case "2":
                dark.setBackgroundResource(R.drawable.dark_sel_bg);
                break;
            default:

        }

        tFontSize.setText(String.format("اندازه فونت : %s", TinyData.getInstance().getInt("systemFontSize", BuildApp.DEFAULT_FONT_SIZE)));
        tSpace.setText(String.format("فاصله بین خطوط : %s", TinyData.getInstance().getInt("fontSpace", 1)));
    }

    private void initOnClickDialog() {

        font.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
                dialogFont();
            }
        });

        fontSize.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {
                int progress = seekParams.progress;
                TinyData.getInstance().putInt("systemFontSize", progress);
                tFontSize.setText(String.format("اندازه فونت : %s", progress));
                setTextSize(progress);
            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {
                settingsDialog.setAlpha(0f);
            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
                settingsDialog.setAlpha(1f);
            }

        });

        space.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {
                TinyData.getInstance().putInt("fontSpace", seekParams.progress);
                tSpace.setText(String.format("فاصله بین خطوط : %s", TinyData.getInstance().getInt("fontSpace", 1)));
                setLineSpacing();
            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {
                settingsDialog.setAlpha(0f);
            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
                settingsDialog.setAlpha(1f);
            }
        });

        filter.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startServiceMode();
                } else {
                    destroyServiceMode();
                }
                TinyData.getInstance().putBool("windowFilterScreen", isChecked);
            }
        });
        bottomSheetDialog.findViewById(R.id.l_light_filter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter.setChecked(!filter.isChecked());
            }
        });

        white.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                white.setBackgroundResource(R.drawable.white_sel_bg);
                sepia.setBackgroundResource(R.drawable.sepia_bg);
                dark.setBackgroundResource(R.drawable.dark_bg);
                TinyData.getInstance().putString("postBackground", "0");
                frameLayout.setBackgroundColor(0xffffffff);
                setTextColor(0xff000000);
                backMP = getResources().getDrawable(R.drawable.music_player_bg);
                divider = 0xffe2e2e2;
                setBackground();
                setColor();
            }
        });

        sepia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                white.setBackgroundResource(R.drawable.white_bg);
                sepia.setBackgroundResource(R.drawable.sepia_sel_bg);
                dark.setBackgroundResource(R.drawable.dark_bg);
                TinyData.getInstance().putString("postBackground", "1");
                frameLayout.setBackgroundColor(0xffFCF1D1);
                setTextColor(0xff000000);
                backMP = getResources().getDrawable(R.drawable.music_player_sepia_bg);
                divider = 0XFFFFCCBC;
                setBackground();
                setColor();
            }
        });

        dark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                white.setBackgroundResource(R.drawable.white_bg);
                sepia.setBackgroundResource(R.drawable.sepia_bg);
                dark.setBackgroundResource(R.drawable.dark_sel_bg);
                TinyData.getInstance().putString("postBackground", "2");
                frameLayout.setBackgroundColor(0xff333333);
                setTextColor(0xffffffff);
                backMP = getResources().getDrawable(R.drawable.music_player_dark_bg);
                divider = 0xFF797979;
                setBackground();
                setColor();
            }
        });

    }

    private void setColor() {
        for (View view : dividers) {
            Drawable drawable = view.getBackground();
            drawable.setColorFilter(divider, PorterDuff.Mode.SRC_IN);
            view.setBackground(drawable);
        }
        boolean isDay = !TinyData.getInstance().getString("postBackground").equals("2");
        for (TextView textView : textViews) {
            textView.colorDayNight(isDay);
        }
    }

    private void setTextColor(int i) {
        for (TextView textView : textViews) {
            if (textView.isNormalColor())
                textView.setTextColor(i);
        }
    }

    private void setLineSpacing() {
        for (TextView textView : textViews) {
            textView.setLineSpacing(0, SettingsActivity.getSpace());
        }
    }

    private void setTextSize(int size) {
        for (TextView textView : textViews) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size + textView.getDifferent());
        }
    }

    private void setTypeFont() {
        for (TextView textView : textViews) {
            switch (textView.getTypeTextView()) {
                case "normal":
                    textView.setTypeface(AndroidUtilities.getLightTypeFace());
                    break;
                case "bold":
                    textView.setTextBold();
                    break;
                case "customFont":
                    // without change
                    break;
                default:
                    throw new RuntimeException("post: not found !");
            }
        }
    }

    private void setBackground() {
        MusicPlayerUtilities.setBackground(backMP);
    }

    private void cleanSpanFromText() {
        for (TextView textView : textViews) {
            textView.clearSpan();
        }
    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activiy_bottom_in, R.anim.activiy_bottom_out);
    }

    @Override
    public void onRequestPlay(MusicPlayer musicPlayer) {
        MusicPlayerUtilities.rebuild();
        if (MusicPlayerUtilities.getMusicPlayerID() != -1 && MusicPlayerUtilities.get() != musicPlayer) {
            MusicPlayerUtilities.get().pause();
            MusicPlayerUtilities.get().hideProgressBar();
        }
        MusicPlayerUtilities.setMusicPlayer(musicPlayer);
    }

    @Override
    public void onStopMusic() {
        music.setVisibility(View.GONE);
        nestedScrollView.setPadding(0, 0, 0, 0);
    }

    @Override
    public void onPlayMusic(boolean play) {
        playPauseButton.setPlayed(play);
        if (search.getVisibility() == View.VISIBLE) {
            cleanSpanFromText();
            search.setVisibility(View.GONE);
        }
        if (music.getVisibility() == View.GONE) {
            music.setVisibility(View.VISIBLE);
            nestedScrollView.setPadding(0, AndroidUtilities.dp(32), 0, 0);
        }
    }

    @Override
    public void onTimeMusic(String time) {
        PostActivity.this.time.setText(time);
    }

}