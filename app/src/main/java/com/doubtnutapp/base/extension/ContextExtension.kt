package com.doubtnutapp.base.extension

import android.content.Context
import android.content.ContextWrapper
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

/**
 * Extension class based on Context in Android
 */
@ColorInt
fun Context.getColorRes(@ColorRes id: Int) = ContextCompat.getColor(this, id)

/**
 * https://stackoverflow.com/questions/51640154/android-view-contextthemewrapper-cannot-be-cast-to-android-app-activity
 */
fun Context.getActivity(): AppCompatActivity? {
    return when (this) {
        is AppCompatActivity -> this
        is ContextWrapper -> this.baseContext.getActivity()
        else -> null
    }
}
