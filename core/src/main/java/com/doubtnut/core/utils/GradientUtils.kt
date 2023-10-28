package com.doubtnut.core.utils

import android.graphics.drawable.GradientDrawable

object GradientUtils {

    fun getGradientBackground(
        startGradient: String?,
        endGradient: String?,
        orientation: GradientDrawable.Orientation = GradientDrawable.Orientation.TOP_BOTTOM,
        cornerRadius: Float = 0f,
        cornerRadii: FloatArray? = null
    ): GradientDrawable {
        // picking one of the color
        val colors = intArrayOf(
            parseColor(startGradient ?: endGradient),
            parseColor(endGradient ?: startGradient)
        )
        val gradientDrawable = GradientDrawable(orientation, colors)
        if (cornerRadius != 0f) {
            gradientDrawable.cornerRadius = cornerRadius
        }
        if (cornerRadii != null) {
            (gradientDrawable.mutate() as GradientDrawable).cornerRadii = cornerRadii
        }
        return gradientDrawable
    }
}
