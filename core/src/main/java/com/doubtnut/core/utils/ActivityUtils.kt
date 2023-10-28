package com.doubtnut.core.utils

import android.app.Activity
import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.doubtnut.core.BuildConfig
import com.doubtnut.core.R

@Suppress("unused")
object ActivityUtils

fun Context?.isRunning() = (this as? Activity).isRunning()
fun Activity?.isRunning() = this != null && !this.isFinishing && !this.isDestroyed
fun Activity?.isNotRunning() = this == null || this.isFinishing || this.isDestroyed

fun Activity?.setStatusBarColor(@ColorRes color: Int) {
    if (this != null) {
        val window = this.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = this.resources.getColor(color)
    }
}

inline fun <reified VM : ViewModel> FragmentActivity.viewModelProvider(
    provider: ViewModelProvider.Factory
) = ViewModelProvider(this, provider).get(VM::class.java)

fun Activity.toast(message: String, duration: Int = Toast.LENGTH_LONG) =
    ToastUtils.makeText(this, message, duration).show()

fun Activity.toast(@StringRes message: Int, duration: Int = Toast.LENGTH_LONG) =
    ToastUtils.makeText(this, message, duration).show()

fun Activity.toastApiError(e: Throwable, duration: Int = Toast.LENGTH_LONG) = this.apiErrorToast2(e, duration)

fun Activity.apiErrorToast2(e: Throwable, duration: Int = Toast.LENGTH_LONG) {
    if (BuildConfig.DEBUG || BuildConfig.ENABLE_ADMIN_OPTIONS) {
        ToastUtils.makeText(this, e.message ?: "Api error.. please try later", duration).show()
    } else {
        ToastUtils.makeText(this, R.string.api_error, duration).show()
    }
}

fun Activity.getScreenWidth2(): Int {
    val displayMetrics = DisplayMetrics()
    this.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
    return displayMetrics.widthPixels
}

fun Activity.getScreenHeight2(): Int {
    val displayMetrics = DisplayMetrics()
    this.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
    return displayMetrics.heightPixels
}