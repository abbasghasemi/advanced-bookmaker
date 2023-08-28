/*
 * Copyright (C) 2019  All rights reserved for FaraSource (ABBAS GHASEMI)
 * https://farasource.com
 */
package ghasemi.abbas.book.general;

import android.graphics.Typeface;

import ghasemi.abbas.book.ApplicationLoader;
import ghasemi.abbas.book.R;

public enum CustomFonts {

    ONE(ApplicationLoader.context.getResources().getString(R.string.font_normal_1), ApplicationLoader.context.getResources().getString(R.string.font_bold_1),ApplicationLoader.context.getResources().getString(R.string.font_name_1)),
    TWO(ApplicationLoader.context.getResources().getString(R.string.font_normal_2), ApplicationLoader.context.getResources().getString(R.string.font_bold_2),ApplicationLoader.context.getResources().getString(R.string.font_name_2)),
    THREE(ApplicationLoader.context.getResources().getString(R.string.font_normal_3), ApplicationLoader.context.getResources().getString(R.string.font_bold_3),ApplicationLoader.context.getResources().getString(R.string.font_name_3)),
    FOUR(ApplicationLoader.context.getResources().getString(R.string.font_normal_4), ApplicationLoader.context.getResources().getString(R.string.font_bold_4),ApplicationLoader.context.getResources().getString(R.string.font_name_4));

    private final Typeface light;
    private final Typeface bold;
    private final String name;

    CustomFonts(String light, String bold, String name) {
        this.light = Typeface.createFromAsset(ApplicationLoader.context.getAssets(), "fonts/" + light);
        this.bold = Typeface.createFromAsset(ApplicationLoader.context.getAssets(), "fonts/" + bold);
        this.name = name.replace(" "," : ");
    }

    public Typeface getTypefaceLight() {
        return light;
    }

    public Typeface getTypefaceBold() {
        return bold;
    }

    public String getName() {
        return name;
    }
}
