package com.doubtnut.core.utils

import android.content.res.ColorStateList
import android.graphics.Color
import com.google.android.material.button.MaterialButton

fun MaterialButton.applyStrokeColor(color: String?) {
    if (!color.isNullOrEmpty()) {
        strokeColor = ColorStateList.valueOf(Color.parseColor(color))
    }
}

fun MaterialButton.applyIconColor(color: String?) {
    if (!color.isNullOrEmpty()) {
        iconTint = ColorStateList.valueOf(Color.parseColor(color))
    }
}

fun MaterialButton.applyCornerRadius(radius: Int?) {
    radius?.let { r ->
        cornerRadius = r.dpToPx()
    }
}

fun MaterialButton.applyStrokeWidth(width: Int?) {
    width?.let { w ->
        strokeWidth = w.dpToPx()
    }
}
