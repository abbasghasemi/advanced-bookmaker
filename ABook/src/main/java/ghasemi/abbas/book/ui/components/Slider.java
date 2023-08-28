/*
 * Copyright (C) 2019  All rights reserved for FaraSource (ABBAS GHASEMI)
 * https://farasource.com
 */
package ghasemi.abbas.book.ui.components;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import ghasemi.abbas.book.R;
import ghasemi.abbas.book.general.AndroidUtilities;
import ghasemi.abbas.book.support.Glide;
import ru.tinkoff.scrollingpagerindicator.ScrollingPagerIndicator;

public class Slider {
    private ArrayList<String> urls;
    private final TextView textView;
    private final View view;
    private boolean isZoom;
    private final boolean sliderZoom;
    private final ViewPager pager;
    private final ScrollingPagerIndicator indicator;
    private final Context context;
    private ObjectAnimator objectAnimator;
    private Handler handler;
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            objectAnimator = ObjectAnimator.ofFloat(textView, "alpha", 1, 0);
            objectAnimator.setDuration(300);
            objectAnimator.start();
            handler = null;
            objectAnimator = null;
        }
    };
    private int c;
    private onTouch onTouch;

    public Slider(Context context, boolean sliderZoom) {
        this.context = context;
        this.sliderZoom = sliderZoom;
        view = LayoutInflater.from(context).inflate(R.layout.post_slider, null);
        pager = view.findViewById(R.id.pager);
        pager.setOffscreenPageLimit(1);
        indicator = view.findViewById(R.id.indicator);
        indicator.setSelectedDotColor(context.getResources().getColor(R.color.colorAccent));
        textView = view.findViewById(R.id.number);

        if (sliderZoom) {
            view.setPadding(0, 0, 0, 0);
        }
    }

    public void addItem(ArrayList<String> urls) {
        this.urls = urls;
    }

    void setCurrentItem(int item) {
        int cur = item;
        for (int i = 0; i < item; i++) {
            if (urls.get(i).startsWith("v:")) {
                cur--;
            }
        }
        pager.setCurrentItem(cur);
        if (onTouch != null) {
            onTouch.setPosition(cur, urls.size());
        }
    }

    public void create() {
        pager.setAdapter(new Adapter());
        indicator.attachToPager(pager);
    }

    public View toView() {
        return view;
    }

    public void setZoom() {
        isZoom = true;
    }

    void setOnTouch(Slider.onTouch onTouch) {
        this.onTouch = onTouch;
    }

    public interface onTouch {
        void setPosition(int position, int count);

        void onClick();
    }

    public class Adapter extends PagerAdapter {

        @Override
        public int getCount() {
            return urls.size();
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull final ViewGroup container, int position) {
            if (sliderZoom) {
                final TouchImageView imageView = new TouchImageView(context);
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setAdjustViewBounds(true);
                imageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                if (urls.get(position).startsWith("http://") || urls.get(position).startsWith("https://")) {
                    imageView.setImageResource(R.drawable.icplaceholder);
                    Glide.with(context).load(urls.get(position)).into(imageView);
                } else {
                    imageView.setImageResource(AndroidUtilities.getImageId(urls.get(position)));
                }
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onTouch != null) {
                            onTouch.onClick();
                        }
                    }
                });
                container.addView(imageView);
                return imageView;
            } else {
                final String url = urls.get(position);
                if (url.startsWith("v:")) {
                    VideoPlayer videoView = new VideoPlayer(context);
                    String[] p = AndroidUtilities.split(url.substring(2), "\\|");
                    videoView.setUri(p[0]);
                    if (p.length > 1) {
                        videoView.setName(p[1]);
                    }
                    container.addView(videoView);
                    return videoView;
                } else {
                    FrameLayout frameLayout = new FrameLayout(context);
                    frameLayout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    ImageView imageView = new ImageView(context);
                    imageView.setAdjustViewBounds(true);
                    imageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    if (url.startsWith("http://") || url.startsWith("https://")) {
                        imageView.setImageResource(R.drawable.icplaceholder);
                        Glide.with(context).load(url).into(imageView);
                    } else {
                        imageView.setImageResource(AndroidUtilities.getImageId(url));
                    }
                    if (isZoom) {
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(context, ghasemi.abbas.book.ui.components.ImageView.class);
                                intent.putExtra("item", position);
                                intent.putExtra("items", urls);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);
                            }
                        });
                    }
                    frameLayout.addView(imageView);
                    container.addView(frameLayout);
                    return frameLayout;
                }
            }
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            if (position == c) {
                return;
            }
            c = position;
            if (sliderZoom) {
                if (onTouch != null) {
                    onTouch.setPosition(position, urls.size());
                }
            } else {
                float x = 0;
                if (objectAnimator != null) {
                    x = objectAnimator.getAnimatedFraction();
                    objectAnimator.cancel();
                }
                objectAnimator = ObjectAnimator.ofFloat(textView, "alpha", x, 1);
                textView.setText(String.format("%s/%s", position + 1, urls.size()));
                objectAnimator.setDuration(300);
                objectAnimator.start();
                if (handler != null) {
                    handler.removeCallbacks(runnable);
                }
                handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(runnable, 1300);
            }
            super.setPrimaryItem(container, position, object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }

    }
}
