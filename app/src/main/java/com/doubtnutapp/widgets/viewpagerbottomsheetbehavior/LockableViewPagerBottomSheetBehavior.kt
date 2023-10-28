package com.doubtnutapp.widgets.viewpagerbottomsheetbehavior

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout

/**
 * Created by devansh on 17/11/20.
 */

class LockableViewPagerBottomSheetBehavior<V : View> : ViewPagerBottomSheetBehavior<V> {

    private var mLocked = false

    constructor()

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs)

    companion object {
        fun from(view: View) = ViewPagerBottomSheetBehavior.from(view) as LockableViewPagerBottomSheetBehavior
    }

    fun setLocked(locked: Boolean) {
        mLocked = locked
    }

    override fun onInterceptTouchEvent(parent: CoordinatorLayout, child: V, event: MotionEvent): Boolean {
        var handled = false
        if (!mLocked) {
            handled = super.onInterceptTouchEvent(parent, child, event)
        }
        return handled
    }

    override fun onTouchEvent(parent: CoordinatorLayout, child: V, event: MotionEvent): Boolean {
        var handled = false
        if (!mLocked) {
            handled = super.onTouchEvent(parent, child, event)
        }
        return handled
    }

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: V,
                                     directTargetChild: View, target: View, nestedScrollAxes: Int,
                                     type: Int): Boolean {
        var handled = false
        if (!mLocked) {
            handled = super.onStartNestedScroll(coordinatorLayout, child,
                    directTargetChild, target, nestedScrollAxes, type)
        }
        return handled
    }

    override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout, child: V, target: View,
                                   dx: Int, dy: Int, consumed: IntArray, type: Int) {
        if (!mLocked) {
            super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
        }
    }

    override fun onStopNestedScroll(coordinatorLayout: CoordinatorLayout, child: V, target: View, type: Int) {
        if (!mLocked) {
            super.onStopNestedScroll(coordinatorLayout, child, target, type)
        }
    }

    override fun onNestedPreFling(coordinatorLayout: CoordinatorLayout, child: V, target: View,
                                  velocityX: Float, velocityY: Float): Boolean {
        var handled = false
        if (!mLocked) {
            handled = super.onNestedPreFling(coordinatorLayout, child, target,
                    velocityX, velocityY)
        }
        return handled
    }
}