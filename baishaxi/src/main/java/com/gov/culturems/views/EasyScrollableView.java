package com.gov.culturems.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

import com.gov.culturems.utils.LogUtil;

/**
 * Easily scroll to next page
 * Created by peter on 2015/11/6.
 */
public class EasyScrollableView extends ScrollView {

    private boolean hasReachedBottom;
    private GestureDetector mGestureDetector;

    public EasyScrollableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mGestureDetector = new GestureDetector(context, new MyGestureListener());
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mGestureDetector.onTouchEvent(ev) && hasReachedBottom) {
            return true;
        } else {
            return super.dispatchTouchEvent(ev);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (hasReachedBottom && mGestureDetector.onTouchEvent(ev)) {
            return false;
        } else {
            return super.onTouchEvent(ev);
        }
    }


    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        // Grab the last child placed in the ScrollView, we need it to determinate the bottom position.
        View view = getChildAt(getChildCount() - 1);

        // Calculate the scrolldiff
        int diff = (view.getBottom() - (getHeight() + getScrollY()));

        // if diff is zero, then the bottom has been reached
        if (diff <= 0) {
            // notify that we have reached the bottom
            LogUtil.d("MyScrollView: Bottom has been reached");
            hasReachedBottom = true;
        } else {
            hasReachedBottom = false;
        }

        super.onScrollChanged(l, t, oldl, oldt);
    }

    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (distanceY > 0) {
                return true;
            } else {
                return false;
            }
        }
    }
}
