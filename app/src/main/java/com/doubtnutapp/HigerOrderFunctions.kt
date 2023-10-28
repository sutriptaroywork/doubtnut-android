package com.doubtnutapp

import android.content.Context
import android.widget.Toast
import com.doubtnut.core.utils.ToastUtils
import com.doubtnutapp.utils.NetworkUtils

/**
 * Need to move this class to core. Class name doesn't look right.
 */
inline fun checkInternetConnection(context: Context, block: () -> Unit) {
    val isInternetConnect = NetworkUtils.isConnected(context)
    if (isInternetConnect) {
        block()
    } else {
        ToastUtils.makeText(context, R.string.string_noInternetConnection, Toast.LENGTH_SHORT).show()
    }
}