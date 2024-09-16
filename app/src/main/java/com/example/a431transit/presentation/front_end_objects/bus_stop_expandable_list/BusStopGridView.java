package com.example.a431transit.presentation.front_end_objects.bus_stop_expandable_list;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class BusStopGridView extends GridView {

    private final Context mContext;
    int mHeight;

    public BusStopGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int desiredWidth = 100;
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int width;

        // Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            // Must be this size
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            // Can't be bigger than...
            width = Math.min(desiredWidth, widthSize);
        } else { // Be whatever you want
            width = desiredWidth;
        }

        // MUST CALL THIS
        setMeasuredDimension(width, mHeight);
        setNumColumns(2);
    }

    public void setGridViewItemHeight(int height) {
        mHeight = height;
    }
}