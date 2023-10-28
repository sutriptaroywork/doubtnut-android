package com.doubtnut.core.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast

fun Context.copy(
    text: String?,
    label: String = "doubtnut_coupon_code",
    toastMessage: String? = null,
    toastDuration: Int = Toast.LENGTH_SHORT
) {
    if (text.isNullOrEmpty()) return
    val clipboard = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    if (toastMessage.isNullOrEmpty().not()) {
        ToastUtils.makeText(this, toastMessage.orEmpty(), toastDuration).show()
    }
}

fun Context.getTextFromClipboard(): String? {
    try {
        val clipboard = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        return clipboard.primaryClip?.getItemAt(0)?.text?.toString()
    } catch (e: Exception) {
    }
    return null
}