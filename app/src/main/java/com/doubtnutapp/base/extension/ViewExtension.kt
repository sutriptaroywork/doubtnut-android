package com.doubtnutapp.base.extension

import android.content.res.ColorStateList
import android.util.TypedValue
import android.view.View
import androidx.annotation.ColorRes

/**
 * Created by devansh on 18/06/21.
 */

fun View.setBackgroundTint(@ColorRes colorRes: Int) {
    backgroundTintList = ColorStateList.valueOf(
        context.getColorRes(colorRes)
    )
}

fun View.clearBackgroundTint() {
    backgroundTintList = null
}

fun View.addRipple() = with(TypedValue()) {
    context.theme.resolveAttribute(android.R.attr.selectableItemBackground, this, true)
    setBackgroundResource(resourceId)
}

fun View.addCircleRipple() = with(TypedValue()) {
    context.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, this, true)
    setBackgroundResource(resourceId)
}
