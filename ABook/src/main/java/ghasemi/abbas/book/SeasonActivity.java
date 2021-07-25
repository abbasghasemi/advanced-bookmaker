/*
 * Copyright (C) 2019  All rights reserved for FaraSource (ABBAS GHASEMI)
 * https://farasource.com
 */
package ghasemi.abbas.book;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import ghasemi.abbas.book.components.MusicPlayer;
import ghasemi.abbas.book.components.PlayPauseButton;
import ghasemi.abbas.book.general.AndroidUtilities;
import ghasemi.abbas.book.general.BuildApp;
import ghasemi.abbas.book.general.FileLogs;
import ghasemi.abbas.book.components.ItemDecoration;
import ghasemi.abbas.book.components.TextView;
import ghasemi.abbas.book.service.MusicPlayerUtilities;
import ghasemi.abbas.book.sqlite.BookDB;
import ghasemi.abbas.book.sqlite.ResultRequest;
import ghasemi.abbas.book.sqlite.SqlModel;
import ghasemi.abbas.book.support.customtabs.CustomTabsIntent;

import static ghasemi.abbas.book.CustomMainActivity.getAnimate;

public class SeasonActivity extends BaseActivity implements MusicPlayer.OnChangeListener {

    private RecyclerView recyclerView;
    private final List<SqlModel> sqlModels = new ArrayList<>();
    private static final List<SeasonActivity> seasons = new ArrayList<>();
    private String listType;
    //
    private RelativeLayout music;
    private TextView time;
    private PlayPauseButton playPauseButton;
    private int seasonLogoId = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_season);
        TextView title = findViewById(R.id.title);
        title.setText(getDataFromIntent("title", getString(R.string.title_of_first_season)));
        ImageView back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        listType = getIntent().getStringExtra("list_type");
        seasonLogoId = getIntent().getIntExtra("season_logo_id", 0);
        if (getIntent().getBooleanExtra("hide_search", false)) {
            findViewById(R.id.search).setVisibility(View.GONE);
        } else {
            ImageView search = findViewById(R.id.search);
            search.setColorFilter(Color.WHITE);
            search.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(SeasonActivity.this, SearchActivity.class);
                    intent.putExtra("season_id", getDataFromIntent("season_id", "0"));
                    intent.putExtra("title", "جستجو در '" + getDataFromIntent("title", getString(R.string.title_of_first_season)) + "'");
                    startActivity(intent, getAnimate());
                }
            });
        }
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(AndroidUtilities.getLayoutManager(SeasonActivity.this, listType));
        if (BuildApp.ENABLE_DIVIDER && listType.equals("row_list")) {
            ItemDecoration itemDecoration = new ItemDecoration(SeasonActivity.this) {
                @Override
                public int getMarginRight(int position) {
                    if (position == 0) {
                        return 0;
                    }
                    return sqlModels.get(position - 1).deleteIcon() ?
                            AndroidUtilities.dp(10) : AndroidUtilities.dp(75);
                }
            };
            recyclerView.addItemDecoration(itemDecoration);
        }
        BookDB.getInstance().seasons(getDataFromIntent("season", "1"), getDataFromIntent("season_id", "0"), new ResultRequest() {
            @Override
            public void onSuccess(final List<SqlModel> sqlModels) {
                SeasonActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SeasonActivity.this.sqlModels.addAll(sqlModels);
                        recyclerView.setAdapter(new SeasonAdapter());
                    }
                });
            }

            @Override
            public void onFail() {
                SeasonActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        AndroidUtilities.Toast("خطا در دریافت لیست مطالب");
                    }
                });
            }
        });

        if (BuildApp.FINISH_SEASON_LISTED_WITH_ON_LONG_CLICK_BACK) {
            seasons.add(this);
        }

        music = findViewById(R.id.music);
        findViewById(R.id.close_music).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicPlayerUtilities.stopService(SeasonActivity.this);
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

    @Override
    protected void onDestroy() {
        if (BuildApp.FINISH_SEASON_LISTED_WITH_ON_LONG_CLICK_BACK) {
            seasons.remove(this);
        }
        super.onDestroy();
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (BuildApp.FINISH_SEASON_LISTED_WITH_ON_LONG_CLICK_BACK && keyCode == KeyEvent.KEYCODE_BACK) {
            for (SeasonActivity seasonListed : seasons) {
                seasonListed.finish();
            }
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    private String getDataFromIntent(String key, String defaultValue) {
        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(key)) {
            return getIntent().getExtras().getString(key);
        }
        return defaultValue;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        if (seasons.size() == 1) {
            overridePendingTransition(R.anim.activiy_bottom_in, R.anim.activiy_bottom_out);
        } else {
            overridePendingTransition(0, 0);
        }
    }

    public class SeasonAdapter extends RecyclerView.Adapter<SeasonAdapter.Holder> {

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            int id;
            if (listType.equals("row_list")) {
                id = R.layout.item_row;
            } else if (listType.equals("card_list")) {
                id = R.layout.item_card;
            } else {
                id = R.layout.item_classic;
            }
            return new Holder(LayoutInflater.from(viewGroup.getContext()).inflate(id, null));
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int i) {
            holder.view(i);
        }

        @Override
        public int getItemCount() {
            return sqlModels.size();
        }

        public class Holder extends RecyclerView.ViewHolder {

            private final TextView title;
            private TextView content;
            private TextView more;
            private ImageView logo;
            private ImageView arrow_left;
            private ImageView share;
            private ImageView copy;

            Holder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.title);

                if (listType.equals("classic_list")) {
                    content = itemView.findViewById(R.id.content);
                    more = itemView.findViewById(R.id.more);
                    share = itemView.findViewById(R.id.share);
                    copy = itemView.findViewById(R.id.copy);
                } else {
                    logo = itemView.findViewById(R.id.logo);
                    arrow_left = itemView.findViewById(R.id.arrow_left);
                }
            }


            public void view(final int i) {
                final SqlModel sqlModel = sqlModels.get(i);
                title.setText(sqlModel.getTitle());
                if (listType.equals("classic_list")) {
                    if (sqlModel.isPost()) {
                        content.setText(sqlModel.getContentWithoutTags310());
                        content.setVisibility(View.VISIBLE);
                        more.setText("بیشتر");
                        if (BuildApp.ACCESS_SHARE) {
                            share.setImageResource(R.drawable.ic_share);
                            share.setVisibility(View.VISIBLE);
                            share.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(Intent.ACTION_SEND);
                                    intent.putExtra(Intent.EXTRA_TEXT, sqlModel.getContentWithoutTags());
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
                            copy.setVisibility(View.VISIBLE);
                            copy.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    AndroidUtilities.addToClipboard(sqlModel.getContentWithoutTags());
                                    AndroidUtilities.toast("کپی شد.");
                                }
                            });
                        } else {
                            share.setOnClickListener(null);
                            copy.setOnClickListener(null);
                            share.setVisibility(View.GONE);
                            copy.setVisibility(View.GONE);
                        }
                    } else {
                        content.setVisibility(View.GONE);
                        share.setOnClickListener(null);
                        copy.setOnClickListener(null);
                        copy.setVisibility(View.GONE);
                        if (sqlModel.hasList()) {
                            more.setText("مشاهده مطالب");
                            share.setImageResource(R.drawable.ic_arrow_left);
                            share.setVisibility(View.VISIBLE);
                        } else {
                            more.setText("مشاهده مطلب");
                            share.setVisibility(View.GONE);
                        }
                    }
                } else {
                    if (listType.equals("row_list") && sqlModel.deleteIcon()) {
                        logo.setVisibility(View.GONE);
                        title.setPaddingRelative(0, 0, AndroidUtilities.dp(15), 0);
                    } else {
                        logo.setVisibility(View.VISIBLE);
                        title.setPaddingRelative(0, 0, 0, 0);
                        int id;
                        if (sqlModel.getIconName().isEmpty()) {
                            id = AndroidUtilities.getLogoId(sqlModel.getId(), sqlModel.getSeasonId(), seasonLogoId);
                        } else {
                            id = AndroidUtilities.getImageId(sqlModel.getIconName());
                            if (id == R.drawable.icplaceholder) {
                                id = AndroidUtilities.getLogoId(sqlModel.getId(), sqlModel.getSeasonId(), seasonLogoId);
                            }
                        }
                        logo.setImageResource(id);
                    }
                    if (sqlModel.hasList()) {
                        arrow_left.setVisibility(View.VISIBLE);
                    } else {
                        arrow_left.setVisibility(View.GONE);
                    }
                }
                itemView.findViewById(R.id.view).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openActionPost(SeasonActivity.this, sqlModel, seasonLogoId, getIntent().getBooleanExtra("hide_search", false));
                    }
                });
            }
        }
    }

    public static void openActionPost(Activity activity, SqlModel sqlModel, int seasonLogoId, boolean hideSearch) {
        if (sqlModel.hasList()) {
            if (!sqlModel.deleteIcon() && !sqlModel.getIconName().isEmpty()) {
                int ID = AndroidUtilities.getImageId(sqlModel.getIconName());
                if (ID != R.drawable.icplaceholder) {
                    seasonLogoId = ID;
                }
            }
            ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(activity, 0, 0);
            Intent intent = new Intent(activity, SeasonActivity.class);
            intent.putExtra("hide_search", hideSearch);
            intent.putExtra("title", sqlModel.getTitle());
            intent.putExtra("season", String.valueOf(sqlModel.getSeason() + 1));
            intent.putExtra("season_id", String.valueOf(sqlModel.getId()));
            intent.putExtra("list_type", sqlModel.getPostType());
            intent.putExtra("season_logo_id", AndroidUtilities.getLogoId(sqlModel.getId(), sqlModel.getSeasonId(), seasonLogoId));
            activity.startActivity(intent, activityOptions.toBundle());
        } else if (sqlModel.isPost()) {
            Intent intent = new Intent(activity, PostActivity.class);
            intent.putExtra("title", sqlModel.getTitle());
            intent.putExtra("id", String.valueOf(sqlModel.getId()));
            activity.startActivity(intent, getAnimate());
        } else {
            switch (sqlModel.getPostType()) {
                case "web":
                    Intent intent = new Intent(activity, WebViewActivity.class);
                    intent.putExtra("web_title", sqlModel.getTitle());
                    intent.putExtra("web_url", sqlModel.getContent().trim());
                    activity.startActivity(intent, getAnimate());
                    break;
                case "dialog":
                    String[] part = AndroidUtilities.split(sqlModel.getContent(), "\\|");
                    AndroidUtilities.setCustomFontDialog(new AlertDialog.Builder(activity)
                            .setTitle(part[0])
                            .setMessage(part[1])
                            .setPositiveButton("باشه", null)
                            .show());
                    break;
                case "toast":
                    AndroidUtilities.toast(sqlModel.getContent());
                    break;
                case "ref":
                    Intent ref = new Intent();
                    String[] part2 = AndroidUtilities.split(sqlModel.getContent(), "\\|");
                    switch (part2[1]) {
                        case "send":
                            ref.setAction(Intent.ACTION_SEND);
                            ref.putExtra(Intent.EXTRA_TEXT, part2[0]);
                            ref.setType("text/*");
                            ref.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            if (part2.length > 3) {
                                ref = Intent.createChooser(ref, part2[3]);
                            }
                            break;
                        case "web":
                        case "view":
                            if (BuildApp.OPEN_LINK_IN_APP && part2[1].equals("web")) {
                                Uri uri = Uri.parse(part2[0]);
                                CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();
                                intentBuilder.setShowTitle(true);
                                intentBuilder.setToolbarColor(activity.getResources().getColor(R.color.colorPrimary));
                                intentBuilder.addDefaultShareMenuItem();
                                intentBuilder.enableUrlBarHiding();
                                CustomTabsIntent customTabsIntent = intentBuilder.build();
                                customTabsIntent.setUseNewTask();
                                customTabsIntent.launchUrl(activity, uri);
                                ref = null;
                            } else {
                                ref.setAction(Intent.ACTION_VIEW);
                                if (part2[0].startsWith("sms")) {
                                    ref.putExtra("address", new String[]{part2[0].substring(6)});
                                }
                                ref.setData(Uri.parse(part2[0]));
                                if (part2.length > 3) {
                                    String[] sub = AndroidUtilities.split(part2[3], "\\^");
                                    ref.putExtra(sub[0], sub[1]);
                                }
                            }
                            break;
                        case "open":
                            ref = activity.getPackageManager().getLaunchIntentForPackage(part2[2]);
                            if (ref == null) {
                                AndroidUtilities.toast("خطا: برنامه هدف پیدا نشد.");
                            }
                            break;
                        case "edit":
                            ref.setAction(Intent.ACTION_EDIT);
                            ref.setData(Uri.parse(part2[0]));
                            break;
                        default:
                            ref.setAction(part2[1]);
                            if (!part2[0].isEmpty()) {
                                ref.setData(Uri.parse(part2[0]));
                            }
                    }

                    if (ref != null) {
                        if (part2.length > 3 && !part2[2].equals("")) {
                            ref.setPackage(part2[2]);
                        }
                        try {
                            activity.startActivity(ref);
                        } catch (Exception e) {
                            FileLogs.e(e);
                            AndroidUtilities.toast("خطا: برنامه هدف پیدا نشد.");
                        }
                    }
                    break;
                case "jump":
                    int seasonLogoId2 = seasonLogoId;
                    BookDB.getInstance().requestTypeFromId(sqlModel.getContent().trim(), new ResultRequest() {
                        @Override
                        public void onSuccess(List<SqlModel> sqlModels) {
                            if (sqlModels.get(0).getPostType().equals("jump")) {
                                AndroidUtilities.toast("خطا: از حالت جامپ نمی توان به جامپ دیگری رفت.");
                                return;
                            }
                            openActionPost(activity, sqlModels.get(0), seasonLogoId2, hideSearch);
                        }

                        @Override
                        public void onFail() {
                            AndroidUtilities.toast(String.format("خطا: ای دی %s پیدا نشد.", sqlModel.getContent().trim()));
                        }
                    });
                    break;
                case "copy":
                    AndroidUtilities.addToClipboard(sqlModel.getContent());
                    AndroidUtilities.toast("کپی شد.");
                    break;
                default:
                    AndroidUtilities.toast("type not supported.");
                    break;
            }
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
