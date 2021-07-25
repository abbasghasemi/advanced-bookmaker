/*
 * Copyright (C) 2019  All rights reserved for FaraSource (ABBAS GHASEMI)
 * https://farasource.com
 */
package ghasemi.abbas.book;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;


import java.util.ArrayList;
import java.util.List;

import ghasemi.abbas.book.general.AndroidUtilities;
import ghasemi.abbas.book.general.BuildApp;
import ghasemi.abbas.book.general.TinyData;
import ghasemi.abbas.book.components.TextView;
import ghasemi.abbas.book.sqlite.BookDB;
import ghasemi.abbas.book.sqlite.ResultRequest;
import ghasemi.abbas.book.sqlite.SqlModel;
import ghasemi.abbas.book.components.CheckBox.SmoothCheckBox;
import ghasemi.abbas.book.components.ItemDecoration;
import ghasemi.abbas.book.components.EditText;

public class SearchActivity extends BaseActivity {
    private final List<SqlModel> sqlModels = new ArrayList<>();
    private final Bundle searchData = new Bundle();
    private SearchAdapter adapter;
    private ProgressBar progressBar;
    private EditText editText;
    private TextView textNotFound;
    private ImageView imageView, imgNotFound;
    private String backupSearch = "";
    private BottomSheetDialog bottomSheetDialog;
    private String query;
    private String id;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search);
        ImageView back = findViewById(R.id.back);
        progressBar = findViewById(R.id.progressBar);
        editText = findViewById(R.id.search);
        TextView title = findViewById(R.id.title);
        textNotFound = findViewById(R.id.txt_not_found);
        imgNotFound = findViewById(R.id.img_not_found);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        findViewById(R.id.img_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkQuerySearch();
            }
        });
        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        if (BuildApp.ENABLE_DIVIDER) {
            ItemDecoration itemDecoration = new ItemDecoration(SearchActivity.this) {
                @Override
                public int getMarginRight(int position) {
                    if (position == 0) {
                        return 0;
                    }
                    return sqlModels.get(position - 1).deleteIcon() ?
                            AndroidUtilities.dp(10) : AndroidUtilities.dp(75);
                }

                @Override
                public boolean deleteDivider(int position) {
                    String type = sqlModels.get(position).getPostType();
                    if (type.equals("title") || type.equals("content")) {
                        return true;
                    }
                    if (position != 0) {
                        type = sqlModels.get(position - 1).getPostType();
                        if (type.equals("title") || type.equals("content")) {
                            return true;
                        }
                    }
                    return super.deleteDivider(position);
                }
            };
            recyclerView.addItemDecoration(itemDecoration);
        }
        recyclerView.setLayoutManager(AndroidUtilities.getLayoutManager(this, "row_list"));

        adapter = new SearchAdapter();
        recyclerView.setAdapter(adapter);

        imageView = findViewById(R.id.close);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText("");
            }
        });

        editText.setOnEditorActionListener(new android.widget.TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(android.widget.TextView v, int actionId, KeyEvent event) {
                return checkQuerySearch();
            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().isEmpty() && imageView.getVisibility() == View.VISIBLE) {
                    Animation animation = AnimationUtils.loadAnimation(SearchActivity.this, R.anim.hide);
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            imageView.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    imageView.startAnimation(animation);
                } else if (!s.toString().trim().isEmpty() && imageView.getVisibility() == View.INVISIBLE) {
                    Animation animation = AnimationUtils.loadAnimation(SearchActivity.this, R.anim.show);
                    imageView.startAnimation(animation);
                    imageView.setVisibility(View.VISIBLE);
                }
            }


            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        findViewById(R.id.filter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogFilter();
            }
        });

        searchData.putBoolean("title", TinyData.getInstance().getBool("isTitle", true));
        searchData.putBoolean("content", TinyData.getInstance().getBool("isContent", true));
        searchData.putBoolean("fav", TinyData.getInstance().getBool("isFav"));
        searchData.putBoolean("similar", TinyData.getInstance().getBool("isSimilar", true));
        id = getIntent().getStringExtra("season_id");
        searchData.putBoolean("all", TextUtils.isEmpty(id));
        searchData.putString("season_id", id);
        if (!TextUtils.isEmpty(id)) {
            title.setText(getIntent().getStringExtra("title"));
        }
    }

    private boolean checkQuerySearch() {
        AndroidUtilities.hideKeyboard(editText);
        if (editText.getText().toString().trim().isEmpty()) {
            AndroidUtilities.toast("متن جستجو نمی تواند خالی باشد.");
            return false;
        } else if (editText.getText().toString().trim().length() < 3) {
            AndroidUtilities.toast("حداقل یک کلمه الزامی است.");
            return false;
        } else if (backupSearch.equals(editText.getText().toString().trim())) {
            return false;
        }
        backupSearch = editText.getText().toString().trim();
        editText.setEnabled(false);
        imageView.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        imgNotFound.setVisibility(View.GONE);
        textNotFound.setVisibility(View.GONE);
        startSearch();
        return true;
    }

    private void dialogFilter() {
        if (bottomSheetDialog != null) {
            return;
        }
        bottomSheetDialog = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        bottomSheetDialog.setContentView(getLayoutInflater().inflate(R.layout.dialog_search_filter, null));
        bottomSheetDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                bottomSheetDialog = null;
            }
        });
        bottomSheetDialog.getWindow().findViewById(R.id.design_bottom_sheet).setBackgroundColor(0);
        bottomSheetDialog.show();
        if (TextUtils.isEmpty(id)) {
            bottomSheetDialog.findViewById(R.id.radio_group).setVisibility(View.GONE);
        } else {
            RadioButton all = bottomSheetDialog.findViewById(R.id.all);
            all.setChecked(searchData.getBoolean("all"));
            all.setTypeface(BuildApp.FONT_TYPE.getTypefaceLight());
            RadioButton list = bottomSheetDialog.findViewById(R.id.list);
            list.setTypeface(BuildApp.FONT_TYPE.getTypefaceLight());

            RadioGroup radioGroup = bottomSheetDialog.findViewById(R.id.radio_group);
            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    searchData.putBoolean("all", checkedId == R.id.all);
                    backupSearch = "";
                }
            });
        }
        final SmoothCheckBox s_title = bottomSheetDialog.findViewById(R.id.s_title);
        final SmoothCheckBox s_context = bottomSheetDialog.findViewById(R.id.s_context);
        final SmoothCheckBox s_fav = bottomSheetDialog.findViewById(R.id.s_fav);
        final SmoothCheckBox s_similar = bottomSheetDialog.findViewById(R.id.s_similar);
        s_title.setChecked(searchData.getBoolean("title"), false);
        s_context.setChecked(searchData.getBoolean("content"), false);
        s_fav.setChecked(searchData.getBoolean("fav"), false);
        s_similar.setChecked(searchData.getBoolean("similar"), false);
        bottomSheetDialog.findViewById(R.id.sl_title).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                s_title.setChecked(!s_title.isChecked(), true);
            }
        });
        bottomSheetDialog.findViewById(R.id.sl_context).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                s_context.setChecked(!s_context.isChecked(), true);
            }
        });
        bottomSheetDialog.findViewById(R.id.sl_fav).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                s_fav.setChecked(!s_fav.isChecked(), true);
            }
        });
        bottomSheetDialog.findViewById(R.id.sl_similar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                s_similar.setChecked(!s_similar.isChecked(), true);
            }
        });

        s_title.setOnCheckedChangeListener(new SmoothCheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SmoothCheckBox checkBox, boolean isChecked) {
                searchData.putBoolean("title", isChecked);
                if (!isChecked && !s_context.isChecked()) {
                    s_context.setChecked(!s_context.isChecked(), true);
                }
                TinyData.getInstance().putBool("isTitle", isChecked);
                backupSearch = "";
            }
        });
        s_context.setOnCheckedChangeListener(new SmoothCheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SmoothCheckBox checkBox, boolean isChecked) {
                searchData.putBoolean("content", isChecked);
                if (!isChecked && !s_title.isChecked()) {
                    s_title.setChecked(!s_title.isChecked(), true);
                }
                TinyData.getInstance().putBool("isContent", isChecked);
                backupSearch = "";
            }
        });
        s_fav.setOnCheckedChangeListener(new SmoothCheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SmoothCheckBox checkBox, boolean isChecked) {
                searchData.putBoolean("fav", isChecked);
                TinyData.getInstance().putBool("isFav", isChecked);
                backupSearch = "";
            }
        });

        s_similar.setOnCheckedChangeListener(new SmoothCheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SmoothCheckBox checkBox, boolean isChecked) {
                searchData.putBoolean("similar", isChecked);
                TinyData.getInstance().putBool("isSimilar", isChecked);
                backupSearch = "";
            }
        });
    }

    private void startSearch() {
        searchData.putString("search", backupSearch);
        BookDB.getInstance().search(searchData, new ResultRequest() {
            @Override
            public void onSuccess(final List<SqlModel> sqlModels) {
                SearchActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        query = sqlModels.remove(sqlModels.size() - 1).getPostType();
                        SearchActivity.this.sqlModels.clear();
                        SearchActivity.this.sqlModels.addAll(sqlModels);
                        adapter.notifyDataSetChanged();
                        editText.setEnabled(true);
                        imageView.setEnabled(true);
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onFail() {
                SearchActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sqlModels.clear();
                        adapter.notifyDataSetChanged();
                        imgNotFound.setVisibility(View.VISIBLE);
                        textNotFound.setVisibility(View.VISIBLE);
                        editText.setEnabled(true);
                        imageView.setEnabled(true);
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activiy_bottom_in, R.anim.activiy_bottom_out);
    }

    public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.Holder> {
        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

            switch (i) {
                case 2:
                    return new Holder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_row, null));
                case 0:
                    TextView title = new TextView(SearchActivity.this);
                    title.setBackgroundColor(getResources().getColor(R.color.colorSearchBackground));
                    title.setTextColor(getResources().getColor(R.color.textTitleColor));
                    title.setTag("title");
                    title.setPadding(AndroidUtilities.dp(10), AndroidUtilities.dp(8), AndroidUtilities.dp(10), AndroidUtilities.dp(8));
                    title.setGravity(Gravity.CENTER | Gravity.RIGHT);
                    title.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    return new Holder(title);
                case 1:
                    TextView context = new TextView(SearchActivity.this);
                    context.setTag("content");
                    context.setPadding(AndroidUtilities.dp(10), AndroidUtilities.dp(8), AndroidUtilities.dp(10), AndroidUtilities.dp(8));
                    context.setGravity(Gravity.CENTER | Gravity.RIGHT);
                    context.setBackgroundColor(getResources().getColor(R.color.colorSearchBackground));
                    context.setTextColor(getResources().getColor(R.color.textTitleColor));
                    context.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    return new Holder(context);
                default:
                    return null;

            }
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int i) {
            if (holder.logo != null) {
                holder.setChange(i);
            } else if (holder.title.getTag().toString().equals("content")) {
                holder.title.setText(String.format("مطالب جستجو شده برای '%s' در متون", backupSearch));
            } else {
                holder.title.setText(String.format("مطالب جستجو شده برای '%s' در عنوان ها", backupSearch));
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (sqlModels.get(position).getPostType().equals("title")) {
                return 0;
            } else if (sqlModels.get(position).getPostType().equals("content")) {
                return 1;
            }
            return 2;
        }

        @Override
        public int getItemCount() {
            return sqlModels.size();
        }

        class Holder extends RecyclerView.ViewHolder {
            private final TextView title;
            private ImageView logo;
            private View arrow_left;

            Holder(@NonNull View itemView) {
                super(itemView);
                if (itemView instanceof RelativeLayout) {
                    title = itemView.findViewById(R.id.title);
                    logo = itemView.findViewById(R.id.logo);
                    arrow_left = itemView.findViewById(R.id.arrow_left);
                } else {
                    title = (TextView) itemView;
                }
            }

            void setChange(final int i) {
                final SqlModel sqlModel = sqlModels.get(i);
                title.setText(sqlModel.getTitle());
                if (sqlModel.deleteIcon()) {
                    logo.setVisibility(View.GONE);
                    title.setPaddingRelative(0, 0, AndroidUtilities.dp(15), 0);
                } else {
                    logo.setVisibility(View.VISIBLE);
                    title.setPaddingRelative(0, 0, 0, 0);
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
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (sqlModel.hasList()) {
                            Intent intent = new Intent(SearchActivity.this, SeasonActivity.class);
                            intent.putExtra("hide_search", true);
                            intent.putExtra("title", sqlModel.getTitle());
                            intent.putExtra("season", String.valueOf(sqlModel.getSeason() + 1));
                            intent.putExtra("season_id", String.valueOf(sqlModel.getId()));
                            intent.putExtra("list_type", sqlModel.getPostType());
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(SearchActivity.this, PostActivity.class);
                            intent.putExtra("title", sqlModel.getTitle());
                            intent.putExtra("id", String.valueOf(sqlModel.getId()));
                            if (sqlModel.getContent().equals("content")) {
                                intent.putExtra("query", query);
                            }
                            startActivity(intent);
                        }
                    }
                });
                if (sqlModel.hasList()) {
                    arrow_left.setVisibility(View.VISIBLE);
                } else {
                    arrow_left.setVisibility(View.GONE);
                }
            }
        }
    }

}