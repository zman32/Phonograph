package com.kabouzeid.gramophone.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.kabouzeid.gramophone.adapter.song.AbsOffsetSongAdapter;
import com.kabouzeid.gramophone.util.Util;
import com.kabouzeid.gramophone.util.ViewUtil;

public class HorizontalItemDivider extends RecyclerView.ItemDecoration {

    private static final int START_OFFSET = 72;

    private Paint mPaint;
    private float mStartOffset;

    public HorizontalItemDivider(Context context) {
        this.mPaint = new Paint();
        this.mPaint.setColor(Util.getDividerColor(context));
        this.mPaint.setStrokeWidth(ViewUtil.convertDpToPixel(1, context.getResources()));

        this.mStartOffset = ViewUtil.convertDpToPixel(START_OFFSET, context.getResources());
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);

        // Draw lines only for visible items and skip the last item in the list from drawing the divider
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount - 1; i++) {
            View child = parent.getChildAt(i);
            if (shouldDrawLine(child, parent)) {
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                int y = child.getBottom() + params.bottomMargin;
                float startX = child.getX() + mStartOffset;    // Where to start drawing the line with offset
                float endX = child.getX() + child.getWidth();  // Where to stop drawing the line
                c.drawLine(startX, y, endX, y, mPaint);
            }
        }
    }

    private boolean shouldDrawLine(View view, RecyclerView parent) {
        int position = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewAdapterPosition();
        int viewType = parent.getAdapter().getItemViewType(position);

        return !(parent.getAdapter() instanceof AbsOffsetSongAdapter && viewType == AbsOffsetSongAdapter.OFFSET_ITEM);
    }
}
