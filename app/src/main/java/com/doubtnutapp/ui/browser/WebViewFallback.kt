package com.doubtnutapp.ui.browser

import android.content.Context
import android.net.Uri

class WebViewFallback : CustomTabActivityHelper.CustomTabFallback {

    override fun openUri(context: Context, uri: Uri) {
        val intent = WebViewActivity.getIntent(context, uri.toString(), null)
        intent.putExtra(WebViewActivity.EXTRA_URL, uri.toString())
        context.startActivity(intent)
    }

}