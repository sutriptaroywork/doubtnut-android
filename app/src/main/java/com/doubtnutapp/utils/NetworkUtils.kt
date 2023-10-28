package com.doubtnutapp.utils

import android.content.Context
import com.doubtnut.core.utils.NetworkUtils as CoreNetworkUtils

object NetworkUtils {

    fun isConnected(context: Context) = CoreNetworkUtils.isConnected(context)

    fun getNetworkClass(context: Context) = CoreNetworkUtils.getNetworkClass(context)
}