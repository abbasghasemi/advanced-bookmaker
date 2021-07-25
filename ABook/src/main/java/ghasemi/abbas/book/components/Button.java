/*
 * Copyright (C) 2019  All rights reserved for FaraSource (ABBAS GHASEMI)
 * https://farasource.com
 */
package ghasemi.abbas.book.components;

import android.content.Context;

import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;

import ghasemi.abbas.book.R;
import ghasemi.abbas.book.general.BuildApp;


public class Button extends androidx.appcompat.widget.AppCompatButton {

    public Button(Context context) {
        super(context);
        init();
    }

    public Button(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Button(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setTypeface(BuildApp.FONT_TYPE.getTypefaceBold());
        setGravity(Gravity.CENTER);
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        setTextColor(0xffffffff);
        setBackground(getResources().getDrawable(R.drawable.button_bg));
    }

}
