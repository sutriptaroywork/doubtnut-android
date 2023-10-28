package com.doubtnut.core.utils

import android.content.Context
import android.os.Looper
import android.widget.Toast
import androidx.annotation.StringRes
import com.doubtnut.core.BuildConfig
import com.doubtnut.core.CoreApplication
import me.drakeet.support.toast.ToastCompat

/**
 * https://github.com/PureWriter/ToastCompat
 * https://stackoverflow.com/questions/51532449/fatal-exception-android-view-windowmanagerbadtokenexception-unable-to-add-wind
 */
object ToastUtils {

    @JvmStatic
    fun makeText(
        context: Context,
        message: String,
        duration: Int,
    ): Toast {
        return if (android.os.Build.VERSION.SDK_INT == 25) {
            ToastCompat.makeText(context, message, duration)
        } else {
            Toast.makeText(context, message, duration)
        }
    }

    @JvmStatic
    fun makeTextInDev(
        context: Context = CoreApplication.INSTANCE,
        message: String,
        duration: Int = Toast.LENGTH_SHORT,
        isAdminEnabled: Boolean = false
    ): Toast? {
        if (Looper.myLooper() != Looper.getMainLooper()) return null
        if (BuildConfig.DEBUG || isAdminEnabled) {
            return makeText(context, "Dev Only: $message", duration)
        }
        return null
    }

    @JvmStatic
    fun makeText(
        context: Context,
        message: CharSequence,
        duration: Int,
    ): Toast {
        return makeText(context, message.toString(), duration)
    }

    @JvmStatic
    fun makeText(
        context: Context,
        @StringRes resId: Int,
        duration: Int,
    ): Toast {
        return makeText(context, context.getString(resId), duration)
    }
}
