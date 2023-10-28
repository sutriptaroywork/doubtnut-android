package com.doubtnutapp.utils

import android.content.Context
import android.widget.Toast
import com.doubtnut.core.CoreApplication
import com.doubtnut.core.utils.ToastUtils
import com.doubtnutapp.R

fun showToast(context: Context?, text: String) {
    if (context == null) return
    ToastUtils.makeText(context, text, Toast.LENGTH_SHORT).show()
}

private var currentToast: Toast? = null

fun showReusableToast(text: String) {
    if (currentToast == null) {
        currentToast = ToastUtils.makeText(
            context = CoreApplication.INSTANCE.applicationContext,
            message = text,
            duration = Toast.LENGTH_SHORT
        )
    } else {
        currentToast?.setText(text)
    }
    currentToast?.show()
}

fun showToast(context: Context?, text: String, gravity: Int, xOffset: Int = 0, yOffset: Int = 0) {
    if (context == null) return
    val toast = ToastUtils.makeText(context, text, Toast.LENGTH_SHORT)
    toast.setGravity(gravity, xOffset, yOffset)
    toast.show()
}

fun showApiErrorToast(context: Context?) {
    if (context == null) return
    ToastUtils.makeText(context, context.resources.getText(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
}
