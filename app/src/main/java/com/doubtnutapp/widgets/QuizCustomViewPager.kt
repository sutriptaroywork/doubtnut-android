package com.doubtnutapp.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager


/**
 * Created by akshaynandwana on
 * 03, October, 2018
 **/
class CustomViewPager(context: Context, attrs: AttributeSet) : ViewPager(context, attrs) {

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return false

    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return false

    }
}