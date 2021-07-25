/*
 * Copyright (C) 2019  All rights reserved for FaraSource (ABBAS GHASEMI)
 * https://farasource.com
 */
package ghasemi.abbas.book.components;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import ghasemi.abbas.book.VideoActivity;
import ghasemi.abbas.book.R;
import ghasemi.abbas.book.support.Glide;

public class VideoPlayer extends FrameLayout {

    private String path;
    private String name;
    private final View root;

    public VideoPlayer(@NonNull Context context) {
        this(context, null);
    }

    public VideoPlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoPlayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        root = LayoutInflater.from(context).inflate(R.layout.post_video_player, null);
        addView(root, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getResources().getDisplayMetrics().widthPixels / 2));

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                VideoActivity.path = path;
                VideoActivity.name = name;
                ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(getContext(), R.anim.fade_in, R.anim.fade_out);
                Intent intent = new Intent(getContext(), VideoActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(intent, activityOptions.toBundle());
            }
        });
    }

    public void setUri(String name) {
        path = name;
        ImageView image = findViewById(R.id.image);
        Glide.with(getContext())
                .load(path)
                .into(image, new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.view).setVisibility(VISIBLE);
                    }
                });
    }


    public void setName(String s) {
        name = s;
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (root.getLayoutParams() != null) {
            root.getLayoutParams().height = getContext().getResources().getDisplayMetrics().widthPixels / 2;
        }
    }
}
