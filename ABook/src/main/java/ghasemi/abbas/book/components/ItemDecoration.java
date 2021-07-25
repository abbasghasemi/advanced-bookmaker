/*
 * Copyright (C) 2019  All rights reserved for FaraSource (ABBAS GHASEMI)
 * https://farasource.com
 */
package ghasemi.abbas.book.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;

import ghasemi.abbas.book.R;
import ghasemi.abbas.book.general.AndroidUtilities;

public abstract class ItemDecoration extends RecyclerView.ItemDecoration {

    private final Drawable drawable;
    private final int left;


    public ItemDecoration(Context context) {
        drawable = new ColorDrawable(context.getResources().getColor(R.color.dividerColor));
        this.left = AndroidUtilities.dp(10);
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        if (parent.getChildAdapterPosition(view) < 1) {
            return;
        }

        outRect.top = drawable.getIntrinsicHeight();
    }

    public abstract int getMarginRight(int position);

    public boolean deleteDivider(int position) {
        return position == 0;
    }

    @Override
    public void onDrawOver(@NonNull Canvas c, RecyclerView parent, @NonNull RecyclerView.State state) {
        int left, right, top, bottom;
        int childCount = parent.getChildCount();
        left = parent.getPaddingLeft();
        right = parent.getWidth() - parent.getPaddingRight();
        for (int i = 1; i < childCount; i++) {
            View child = parent.getChildAt(i);
            int p = parent.getChildAdapterPosition(child);
            if (deleteDivider(p)) {
                continue;
            }
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            top = child.getTop() - params.topMargin;
            bottom = top + AndroidUtilities.dp(.5f);
            drawable.setBounds(left - (left - this.left), top, right == -1 ? right : right - getMarginRight(p), bottom);
            drawable.draw(c);
        }
    }
}