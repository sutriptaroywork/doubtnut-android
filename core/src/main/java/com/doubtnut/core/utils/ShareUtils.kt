package com.doubtnut.core.utils

import android.content.Context
import android.content.Intent

object ShareUtils {

    @JvmStatic
    fun shareText(
        context: Context,
        title: String = "",
        subject: String = "",
        text: String
    ) {
        val i = Intent(Intent.ACTION_SEND)
        i.type = "text/plain"
        i.putExtra(Intent.EXTRA_SUBJECT, subject)
        i.putExtra(Intent.EXTRA_TEXT, text)
        context.startActivity(Intent.createChooser(i, title))
    }
}
