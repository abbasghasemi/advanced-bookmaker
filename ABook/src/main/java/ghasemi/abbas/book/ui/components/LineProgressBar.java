/*
 * Copyright (C) 2019  All rights reserved for FaraSource (ABBAS GHASEMI)
 * https://farasource.com
 */
package ghasemi.abbas.book.ui.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;

import androidx.annotation.Nullable;

import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

import ghasemi.abbas.book.R;
import ghasemi.abbas.book.general.AndroidUtilities;
import ghasemi.abbas.book.general.FileLogs;

public class LineProgressBar extends View {

    private int progress;
    private int maxProgress;
    private Runnable runnable;
    private Handler handler;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int color = 0;

    public LineProgressBar(Context context) {
        super(context);
        init();
    }

    public LineProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LineProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        color = getResources().getColor(R.color.colorAccent);
        paint.setStrokeWidth(AndroidUtilities.dp(3));
        paint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setColor(Color.argb(50, Color.red(color), Color.green(color), Color.blue(color)));
        canvas.drawLine(0, 0, getWidth(), 0, paint);
        float x = (float) progress * getWidth() / 100;
        FileLogs.print(x);
        paint.setColor(color);
        canvas.drawLine(0, 0, x, 0, paint);
    }

    public void setProgress(int newProgress) {
        if (newProgress < 101 && newProgress > -1) {
            this.maxProgress = newProgress;
            setVisibility(VISIBLE);
            if (!isRunning()) {
                handler = new Handler(Looper.getMainLooper());
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        if (progress + 1 < maxProgress) {
                            progress++;
                            invalidate();
                            handler.postDelayed(this, 15);
                        } else {
                            runnable = null;
                            handler = null;
                            progress = maxProgress;
                            invalidate();
                            if (progress == 100) {
                                progress = 0;
                                maxProgress = 0;
                                setVisibility(GONE);
                            }
                        }
                    }
                };
                runnable.run();
            }
        }
    }

    public boolean isRunning() {
        return handler != null;
    }
}
