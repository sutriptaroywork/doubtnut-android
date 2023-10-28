package com.doubtnutapp.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class SwipeLockableViewPager @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null
): ViewPager(context, attrs) {

    private var mSwipeEnabled = false

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return mSwipeEnabled && super.onTouchEvent(event)
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return mSwipeEnabled && super.onInterceptTouchEvent(event)
    }

    fun setSwipePagingEnabled(swipeEnabled: Boolean) {
        mSwipeEnabled = swipeEnabled
    }
}