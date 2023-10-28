package com.doubtnutapp.videoscreen

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.Toast
import com.doubtnut.core.utils.dpToPx
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.EventBus.TapBackwardEvent
import com.doubtnutapp.EventBus.TapForwardEvent

/** This custom frame layout enables double tap to move forward/back functionality
 *  by capturing events from YT view
 */

class CustomFrameLayout(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    var firstTouch = false
    var time = 0L;

    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {

        val width = this.width
        val half = width / 2
        val validLeftArea = 0.4 * width;
        val validRightArea = half + 0.1 * width

        when (event?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (firstTouch && (System.currentTimeMillis() - time) <= 300) {
                    //do stuff here for double tap
                    firstTouch = false;
                    if (event.x < validLeftArea) {
                        DoubtnutApp.INSTANCE.bus()?.send(TapBackwardEvent())
                    } else if (event.x > validRightArea) {
                        DoubtnutApp.INSTANCE.bus()?.send(TapForwardEvent())
                    }

                } else {
                    firstTouch = true;
                    time = System.currentTimeMillis();

                    return false;
                }

                return true

            }

        }

        return super.onInterceptTouchEvent(event)
    }
}