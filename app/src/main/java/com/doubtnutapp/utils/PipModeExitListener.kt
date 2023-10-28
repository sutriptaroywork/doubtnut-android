package com.doubtnutapp.utils

import android.app.Activity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

/**
 * Created by devansh on 18/12/20.
 */

/**
 * There's no simple way to distinguish between PiP window closed and expanded actions.
 * This class encapsulates identifying those cases and sends correct events.
 * There can be two reasons for PiP exit:
 *      1. User expands PiP window
 *      2. PiP window is closed by user action or programmatically
 */
abstract class PipModeExitListener : LifecycleObserver {

    companion object {
        private const val COUNTDOWN_COUNT = 2
    }

    private var mPipExpandedFlag: Int = COUNTDOWN_COUNT
    private var mPipClosedFlag: Int = COUNTDOWN_COUNT

    /**
     * Call when PiP window is exited for any of the reasons mention in [PipModeExitListener]
     * Call in [Activity.onPictureInPictureModeChanged]
     */
    fun pipExited() {
        pipExpanded()
        pipClosed()
    }

    /**
     * Call when activity enters PiP mode. Call in [Activity.onPictureInPictureModeChanged]
     */
    fun pipEntered() {
        resetFlags()
    }

    /**
     * Callback received when PiP window is expanded into full screen activity
     */
    abstract fun onPipExpanded()

    /**
     * Callback received when PiP window is closed for reasons mentioned in [PipModeExitListener]
     */
    abstract fun onPipClosed()

    /**
     * Call when PiP window is expanded. [Activity.onResume] is called in this case
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun pipExpanded() {
        if (mPipExpandedFlag == 0) return
        mPipExpandedFlag -= 1
        if (mPipExpandedFlag == 0) {
            resetFlags()
            onPipExpanded()
        }
    }

    /**
     * Call when PiP window is closed. [Activity.onStop] is called in this case
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun pipClosed() {
        if (mPipClosedFlag == 0) return
        mPipClosedFlag -= 1
        if (mPipClosedFlag == 0) {
            resetFlags()
            onPipClosed()
        }
    }

    private fun resetFlags() {
        mPipExpandedFlag = COUNTDOWN_COUNT
        mPipClosedFlag = COUNTDOWN_COUNT
    }
}