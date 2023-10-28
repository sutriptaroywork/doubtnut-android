package com.doubtnutapp.camera.ui

import android.content.Context
import android.view.OrientationEventListener
import android.view.Surface

/**
 * Created by devansh on 22/07/20.
 */

abstract class CameraActivityOrientationEventListener(context: Context) :
    OrientationEventListener(context) {

    private var previousOrientation = ORIENTATION_UNKNOWN
    private var currentOrientation = ORIENTATION_UNKNOWN

    override fun onOrientationChanged(orientation: Int) {
        currentOrientation = when (orientation) {
            in 45..134 -> {
                Surface.ROTATION_270
            }
            in 135..224 -> {
                Surface.ROTATION_180
            }
            in 225..314 -> {
                Surface.ROTATION_90
            }
            else -> {
                Surface.ROTATION_0
            }
        }
        if (previousOrientation != currentOrientation && orientation != ORIENTATION_UNKNOWN) {
            previousOrientation = currentOrientation
            if (currentOrientation != ORIENTATION_UNKNOWN) {
                onSimpleOrientationChanged(currentOrientation)
            }
        }
    }

    abstract fun onSimpleOrientationChanged(orientation: Int)
}
