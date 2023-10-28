package com.doubtnut.core.ui.helpers

import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

class RvRequestDisallowInterceptTouchEventListener : RecyclerView.OnItemTouchListener {

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        val action = e.action
        @Suppress("LiftReturnOrAssignment")
        if (rv.canScrollHorizontally(RecyclerView.FOCUS_FORWARD)) {
            when (action) {
                MotionEvent.ACTION_MOVE -> rv.parent
                    .requestDisallowInterceptTouchEvent(true)
            }
            return false
        } else {
            when (action) {
                MotionEvent.ACTION_MOVE -> rv.parent
                    .requestDisallowInterceptTouchEvent(false)
            }
            rv.removeOnItemTouchListener(this)
            return true
        }
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
    }
}