package com.example.jt.android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;


public class ListDecoration extends RecyclerView.ItemDecoration {

    private Drawable mDivder;

    public ListDecoration(Context context)
    {
        mDivder = context.getResources().getDrawable(R.drawable.divider);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state)
    {
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        int count = parent.getChildCount();

        for(int i = 0; i<count; i++)
        {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + mDivder.getIntrinsicHeight();

            mDivder.setBounds(left, top, right, bottom);
            mDivder.draw(c);
        }
    }
}
