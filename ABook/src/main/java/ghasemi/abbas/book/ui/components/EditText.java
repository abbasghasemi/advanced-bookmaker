/*
 * Copyright (C) 2019  All rights reserved for FaraSource (ABBAS GHASEMI)
 * https://farasource.com
 */
package ghasemi.abbas.book.ui.components;

import android.content.Context;

import androidx.appcompat.widget.AppCompatEditText;
import android.util.AttributeSet;

import ghasemi.abbas.book.BuildApp;

public class EditText extends AppCompatEditText {

    {
        setTypeface(BuildApp.FONT_TYPE.getTypefaceLight());
    }

    public EditText(Context context) {
        super(context);

    }

    public EditText(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public EditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }
}
