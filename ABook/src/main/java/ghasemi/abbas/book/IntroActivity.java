/*
 * Copyright (C) 2019  All rights reserved for FaraSource (ABBAS GHASEMI)
 * https://farasource.com
 */
package ghasemi.abbas.book;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import ghasemi.abbas.book.general.AndroidUtilities;
import ghasemi.abbas.book.general.TinyData;
import ghasemi.abbas.book.components.TextView;
import ru.tinkoff.scrollingpagerindicator.ScrollingPagerIndicator;

public class IntroActivity extends BaseActivity {

    ViewPager viewPager;
    ScrollingPagerIndicator dots;

    private String[] titles;

    private String[] messages;

    private String[] images;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_intro);
        titles = getResources().getStringArray(R.array.intro_titles);
        messages = getResources().getStringArray(R.array.intro_messages);
        images =  getResources().getStringArray(R.array.intro_images);
        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(new Adapter());
        dots = findViewById(R.id.dots);
        dots.attachToPager(viewPager);

        findViewById(R.id.btn_go).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TinyData.getInstance().putBool("showedIntro",false);
                ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(IntroActivity.this, R.anim.fade_in, R.anim.fade_out);
                startActivity(new Intent(IntroActivity.this, CustomMainActivity.class), activityOptions.toBundle());
                finish();
            }
        });
    }

    private class Adapter extends PagerAdapter {

        @Override
        public int getCount() {
            return titles.length;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view.equals(object);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View view = LayoutInflater.from(IntroActivity.this).inflate(R.layout.pager_intro, null);
            ImageView imageView = view.findViewById(R.id.image_id);
            imageView.setImageResource(AndroidUtilities.getImageId(images[position]));
            TextView title = view.findViewById(R.id.title_id);
            title.setText(titles[position]);
            TextView message = view.findViewById(R.id.message_id);
            message.setText(messages[position]);
            container.addView(view);
            return view;
        }
    }
}
