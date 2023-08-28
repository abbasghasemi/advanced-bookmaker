/*
 * Copyright (C) 2019  All rights reserved for FaraSource (ABBAS GHASEMI)
 * https://farasource.com
 */
package ghasemi.abbas.book.ui;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.Timer;
import java.util.TimerTask;

import ghasemi.abbas.book.R;
import ghasemi.abbas.book.BuildApp;
import ghasemi.abbas.book.general.TinyData;

public class LauncherActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getResources().getStringArray(R.array.intro_images).length != 0 && TinyData.getInstance().getBool("showedIntro", true)) {
            startActivity(new Intent(LauncherActivity.this, IntroActivity.class));
            finish();
        } else if (BuildApp.ENABLE_SPLASH) {
            setContentView(R.layout.activity_launcher);
            (new Timer()).schedule(new TimerTask() {
                @Override
                public void run() {
                    startMain();
                }
            }, BuildApp.SPLASH_TIME * 1000);
        } else {
            startMain();
        }

    }

    private void startMain() {
        ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(this, R.anim.fade_in, R.anim.fade_out);
        startActivity(new Intent(LauncherActivity.this, CustomMainActivity.class), activityOptions.toBundle());
        finish();
    }
}