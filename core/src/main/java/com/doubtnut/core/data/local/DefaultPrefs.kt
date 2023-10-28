package com.doubtnut.core.data.local

import android.preference.PreferenceManager
import com.doubtnut.core.CoreApplication

fun defaultPrefs2() = PreferenceManager.getDefaultSharedPreferences(CoreApplication.INSTANCE)!!