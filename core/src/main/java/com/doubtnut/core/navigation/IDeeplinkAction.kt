package com.doubtnut.core.navigation

import android.content.Context
import android.os.Bundle

interface IDeeplinkAction {

    fun performAction(context: Context, deeplink: String?): Boolean

    fun performAction(context: Context, deeplink: String?, source: String): Boolean

    fun performAction(context: Context, deeplink: String?, bundle: Bundle? = null): Boolean
}