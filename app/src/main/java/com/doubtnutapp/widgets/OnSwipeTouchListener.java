package com.doubtnutapp.widgets;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class OnSwipeTouchListener implements View.OnTouchListener {
    private final GestureDetector gestureDetector;

    public OnSwipeTouchListener(Context context) {
        this.gestureDetector = new GestureDetector(context, new OnSwipeTouchListener.GestureListener());
    }

    public void onSwipeLeft() {
    }

    public void onSwipeBottom() {
    }

    public void onSwipeTop() {
    }

    public void onSwipeRight() {
    }

    public void onClickView() {
    }

    public boolean onTouch(View v, MotionEvent event) {
        return this.gestureDetector.onTouchEvent(event);
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_DISTANCE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        private GestureListener() {
        }

        public boolean onDown(MotionEvent e) {
            return true;
        }

        public boolean onSingleTapUp(MotionEvent e) {
            OnSwipeTouchListener.this.onClickView();
            return true;
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (e1 == null || e2 == null) return false;
            float distanceX = e2.getX() - e1.getX();
            float distanceY = e2.getY() - e1.getY();
            if (Math.abs(distanceX) > Math.abs(distanceY) && Math.abs(distanceX) > 100.0F && Math.abs(velocityX) > 100.0F) {
                if (distanceX > 0.0F) {
                    OnSwipeTouchListener.this.onSwipeRight();
                } else {
                    OnSwipeTouchListener.this.onSwipeLeft();
                }

                return true;
            } else {
                if (Math.abs(distanceY) > 100.0F && Math.abs(velocityY) > 100.0F) {
                    if (distanceY > 0.0F) {
                        OnSwipeTouchListener.this.onSwipeBottom();
                    } else {
                        OnSwipeTouchListener.this.onSwipeTop();
                    }
                }

                return false;
            }
        }
    }
}