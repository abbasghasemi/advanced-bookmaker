/*
 * Copyright (C) 2019  All rights reserved for FaraSource (ABBAS GHASEMI)
 * https://farasource.com
 */
package ghasemi.abbas.book;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import ghasemi.abbas.book.general.AndroidUtilities;
import ghasemi.abbas.book.general.BuildApp;
import ghasemi.abbas.book.general.FileLogs;
import ghasemi.abbas.book.sqlite.BookDB;
import ghasemi.abbas.book.sqlite.ResultRequest;
import ghasemi.abbas.book.sqlite.SqlModel;
import ghasemi.abbas.book.components.ItemDecoration;
import ghasemi.abbas.book.components.TextView;
import ghasemi.abbas.book.support.CallbackFavoriteListener;


public class FavoriteActivity extends BaseActivity implements CallbackFavoriteListener {

    public static CallbackFavoriteListener refFavorite;
    public List<SqlModel> sqlModels = new ArrayList<>();
    public SqlModel sqlModelBackup;
    private RecyclerView recyclerView;
    private FavoriteAdapter adapter;
    private TextView txtNotFound;
    private ImageView imgNotFound;

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activiy_bottom_in, R.anim.activiy_bottom_out);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_season);
        TextView title = findViewById(R.id.title);
        title.setText(getResources().getString(R.string.favorite));
        findViewById(R.id.search).setVisibility(View.GONE);
        ImageView back = findViewById(R.id.back);
        txtNotFound = findViewById(R.id.txt_not_found);
        imgNotFound = findViewById(R.id.img_not_found);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        recyclerView = findViewById(R.id.recyclerView);
        if (BuildApp.ENABLE_DIVIDER && BuildApp.FAVORITE_LIST_TYPE.equals("row_list")) {
            ItemDecoration itemDecoration = new ItemDecoration(FavoriteActivity.this) {
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
        recyclerView.setLayoutManager(AndroidUtilities.getLayoutManager(this, BuildApp.FAVORITE_LIST_TYPE));
        BookDB.getInstance().favorite(new ResultRequest() {
            @Override
            public void onSuccess(final List<SqlModel> sqlModels) {
                FavoriteActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        (new Handler(Looper.getMainLooper())).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                FavoriteActivity.this.sqlModels.addAll(sqlModels);
                                adapter = new FavoriteAdapter();
                                recyclerView.setAdapter(adapter);
                            }
                        }, 100);
                    }
                });
            }

            @Override
            public void onFail() {
                FavoriteActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        txtNotFound.setVisibility(View.VISIBLE);
                        imgNotFound.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

        refFavorite = this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
            if (sqlModels.isEmpty()) {
                txtNotFound.setVisibility(View.VISIBLE);
                imgNotFound.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        refFavorite = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        refFavorite = null;
    }

    @Override
    public void onChange(boolean isFav, int sqlModels) {
        if (isFav) {
            FavoriteActivity.this.sqlModels.add(sqlModels, sqlModelBackup);
        } else {
            sqlModelBackup = FavoriteActivity.this.sqlModels.get(sqlModels);
            FavoriteActivity.this.sqlModels.remove(sqlModels);
        }
    }

    public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.Holder> {

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            int id;
            if (BuildApp.FAVORITE_LIST_TYPE.equals("row_list")) {
                id = R.layout.item_row;
            } else if (BuildApp.FAVORITE_LIST_TYPE.equals("card_list")) {
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
            private ImageView logo;
            private ImageView share;
            private ImageView copy;

            Holder(@NonNull View itemView) {
                super(itemView);

                title = itemView.findViewById(R.id.title);

                if (BuildApp.FAVORITE_LIST_TYPE.equals("classic_list")) {
                    content = itemView.findViewById(R.id.content);
                    share = itemView.findViewById(R.id.share);
                    copy = itemView.findViewById(R.id.copy);
                } else {
                    logo = itemView.findViewById(R.id.logo);
                    itemView.findViewById(R.id.arrow_left).setVisibility(View.GONE);
                }

            }


            public void view(final int i) {
                final SqlModel sqlModel = sqlModels.get(i);
                title.setText(sqlModel.getTitle());
                if (BuildApp.FAVORITE_LIST_TYPE.equals("classic_list")) {
                    content.setText(sqlModel.getContentWithoutTags310());
                    if (BuildApp.ACCESS_SHARE) {
                        share.setVisibility(View.VISIBLE);
                        share.setImageResource(R.drawable.ic_share);
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
                        copy.setImageResource(R.drawable.ic_copy);
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
                    if (BuildApp.FAVORITE_LIST_TYPE.equals("row_list") && sqlModel.deleteIcon()) {
                        logo.setVisibility(View.GONE);
                        title.setPaddingRelative(0, 0, AndroidUtilities.dp(15), 0);
                    } else {
                        title.setPaddingRelative(0, 0, 0, 0);
                        logo.setVisibility(View.VISIBLE);
                        int id;
                        if (sqlModel.getIconName().isEmpty()) {
                            id = AndroidUtilities.getLogoId(sqlModel.getId(), sqlModel.getSeasonId(), 0);
                        } else {
                            id = AndroidUtilities.getImageId(sqlModel.getIconName());
                            if (id == R.drawable.icplaceholder) {
                                id = AndroidUtilities.getLogoId(sqlModel.getId(), sqlModel.getSeasonId(), 0);
                            }
                        }
                        logo.setImageResource(id);
                    }
                }
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(FavoriteActivity.this, PostActivity.class);
                        intent.putExtra("title", sqlModel.getTitle());
                        intent.putExtra("id", String.valueOf(sqlModel.getId()));
                        intent.putExtra("fav_pos_id", i);
                        startActivity(intent);
                    }
                });
            }
        }
    }
}
