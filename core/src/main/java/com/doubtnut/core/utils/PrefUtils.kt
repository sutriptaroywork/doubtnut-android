package com.doubtnut.core.utils

import android.preference.PreferenceManager
import com.doubtnut.core.CoreApplication

object PrefUtils

fun defaultPreferences() =
    PreferenceManager.getDefaultSharedPreferences(CoreApplication.INSTANCE.applicationContext)!!