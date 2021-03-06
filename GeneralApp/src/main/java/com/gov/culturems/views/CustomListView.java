package com.gov.culturems.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Created by peter on 2015/11/6.
 */
public class CustomListView extends LoadMoreListView {
    private GestureDetector mGestureDetector;

    public CustomListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mGestureDetector = new GestureDetector(context, new MyGestureListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        boolean flag;
        if (getFirstVisiblePosition() == 0 && mGestureDetector.onTouchEvent(ev)) {
            flag = false;
        } else {
            try{
                flag = super.onTouchEvent(ev);
            }catch (Exception e){
                e.printStackTrace();
                flag = false;
            }
        }
        return flag;
    }

    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (distanceY < 0) {
                return true;
            } else {
                return false;
            }
        }
    }
}
