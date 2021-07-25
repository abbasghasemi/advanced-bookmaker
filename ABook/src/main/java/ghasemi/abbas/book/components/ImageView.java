/*
 * Copyright (C) 2019  All rights reserved for FaraSource (ABBAS GHASEMI)
 * https://farasource.com
 */
package ghasemi.abbas.book.components;

import android.animation.ObjectAnimator;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import ghasemi.abbas.book.R;
import ghasemi.abbas.book.general.AndroidUtilities;
import ghasemi.abbas.book.support.Glide;

public class ImageView extends AppCompatActivity {
    LinearLayout toolbar;
    boolean isViewStatus;
    TextView textView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        isViewStatus = AndroidUtilities.disableFitsSystemWindow(this);
        textView = findViewById(R.id.number);
        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        String url = getIntent().getStringExtra("urlOrId");
        if (url == null) {
            final Slider slider = new Slider(this, true);
            slider.addItem(getIntent().getStringArrayListExtra("items"));
            slider.create();
            FrameLayout view = findViewById(R.id.view);
            slider.setOnTouch(new Slider.onTouch() {
                @Override
                public void setPosition(int position, int count) {
                    textView.setText(String.format("تصویر %s از %s", position + 1, count));
                }

                @Override
                public void onClick() {
                    if (isViewStatus) {
                        isViewStatus = false;
                        animation(false);
                    } else {
                        isViewStatus = true;
                        animation(true);
                    }
                }
            });
            slider.setCurrentItem(getIntent().getIntExtra("item",0));
            view.addView(slider.toView());
        } else {
            final TouchImageView imageView = findViewById(R.id.image);
            if (url.startsWith("http://") || url.startsWith("https://")) {
                Glide.with(this).load(url).into(imageView);
            } else {
                imageView.setImageResource(AndroidUtilities.getImageId(url));
            }

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isViewStatus) {
                        isViewStatus = false;
                        animation(false);
                    } else {
                        isViewStatus = true;
                        animation(true);
                    }
                }
            });
        }
        toolbar = findViewById(R.id.toolbar);
        if (isViewStatus) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N || !isInMultiWindowMode()) {
                toolbar.setPadding(0, AndroidUtilities.getStatusBarHeight(), 0, 0);
            }
        } else {
            isViewStatus = true;
        }
    }

    private void animation(boolean show) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(toolbar, "alpha", show ? 0 : 1, show ? 1 : 0);
        animator.setDuration(300);
        animator.start();
    }
}
