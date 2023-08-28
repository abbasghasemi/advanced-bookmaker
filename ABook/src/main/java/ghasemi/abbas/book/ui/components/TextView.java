/*
 * Copyright (C) 2019  All rights reserved for FaraSource (ABBAS GHASEMI)
 * https://farasource.com
 */
package ghasemi.abbas.book.ui.components;

import android.content.Context;
import android.content.res.TypedArray;

import androidx.annotation.Nullable;

import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.TypedValue;

import ghasemi.abbas.book.R;
import ghasemi.abbas.book.general.AndroidUtilities;
import ghasemi.abbas.book.BuildApp;

public class TextView extends androidx.appcompat.widget.AppCompatTextView {

    private String typeTextView = "normal";
    private boolean normalColor = true;
    private int different = 0;
    private Spanned spanned;
    private int[] colors = null;

    public TextView(Context context) {
        this(context, null);
    }

    public TextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public TextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.TextView, defStyleAttr, 0);
        boolean b = a.getBoolean(R.styleable.TextView_tv_bold, false);
        int color = a.getColor(R.styleable.TextView_tv_color, getResources().getColor(R.color.textDefaultColor));
        int text_size = (int) a.getInteger(R.styleable.TextView_tv_size, -1);
        a.recycle();

        if (b) {
            setTypeface(BuildApp.FONT_TYPE.getTypefaceBold());
        } else {
            setTypeface(BuildApp.FONT_TYPE.getTypefaceLight());
        }

        setTextColor(color);
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, text_size == -1 ? BuildApp.GENERAL_FONT_SIZE : text_size);
    }

    public void setTextBold() {
        setTypeface(AndroidUtilities.getBoldTypeFace());
    }

    public String getTypeTextView() {
        return typeTextView;
    }

    public void setTypeTextView(String typeTextView) {
        this.typeTextView = typeTextView;
    }

    public void disableNormalColor() {
        this.normalColor = false;
    }

    public boolean isNormalColor() {
        return normalColor;
    }

    public void setDifferentTextSize(int size) {
        this.different = size;
        super.setTextSize(TypedValue.COMPLEX_UNIT_DIP, AndroidUtilities.getTextSize() + size);
    }

    public int getDifferent() {
        return different;
    }

    public void setTextHtml(String substring) {
        spanned = Html.fromHtml(substring);
        setText(spanned);
    }

    public void setTextColors(int[] colors) {
        this.colors = colors;
    }

    public void colorDayNight(boolean isDay) {
        if (colors != null) {
            setTextColor(colors[isDay ? 0 : 1]);
        }
    }

    public int setHighLighted(String textToHighlight) {
        int i = 0;
        String text = getText().toString();
        int ofe = text.indexOf(textToHighlight);
        Spannable wordToSpan = new SpannableString(getText());
        for (int ofs = 0; ofs < text.length() && ofe != -1; ofs = ofe + 1) {
            ofe = text.indexOf(textToHighlight, ofs);
            if (ofe == -1) {
                break;
            } else {
                i++;
                wordToSpan.setSpan(new BackgroundColorSpan(0xffA5D6A7), ofe, ofe + textToHighlight.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                wordToSpan.setSpan(new ForegroundColorSpan(0xff2E7D32), ofe, ofe + textToHighlight.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                setText(wordToSpan, TextView.BufferType.SPANNABLE);
            }
        }
        return i;
    }

    public void clearSpan() {
        setText(spanned == null ? getText().toString() : spanned);
    }
}
