package com.doubtnut.core.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

object EmailUtils {

    fun sendEmail(
        context: Context,
        email: Array<String>,
        subject: String?,
        message: String?,
    ) {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:")
        intent.putExtra(Intent.EXTRA_EMAIL, email)
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.putExtra(Intent.EXTRA_TEXT, message)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
        }
    }
}
