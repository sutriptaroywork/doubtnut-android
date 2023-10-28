package com.doubtnutapp.utils

import android.content.Context
import androidx.core.content.edit
import com.doubtnutapp.defaultPrefs

/*
* Manages all count logic for various app actions for example app open, video seen and etc.
* */
object CountingManager {

    private const val PREF_KEY_APP_OPEN_COUNT = "pref_key_app_open_count"
    private const val PREF_KEY_PREVIOUS_APP_OPEN_COUNT = "pref_key_previous_app_open_count"
    private const val PREF_KEY_VIDEO_SEEN_COUNT = "pref_key_video_seen_count"
    private const val PREF_KEY_PREVIOUS_VIDEO_SEEN_COUNT = "pref_key_previous_video_seen_count"

    fun updateAppOpenCount(context: Context) {
        var count = getAppOpenCount(context)
        defaultPrefs(context).edit {
            putInt(PREF_KEY_APP_OPEN_COUNT, ++count)
        }
    }

    fun resetAppOpenCount(context: Context) {
        defaultPrefs(context).edit {
            putInt(PREF_KEY_APP_OPEN_COUNT, 0)
        }
    }

    fun updatePreviousAppOpenCount(context: Context) {
        val count = getAppOpenCount(context)
        defaultPrefs(context).edit {
            putInt(PREF_KEY_PREVIOUS_APP_OPEN_COUNT, count)
        }
    }

    fun getPreviousAppOpenCount(context: Context) = defaultPrefs(context).getInt(PREF_KEY_PREVIOUS_APP_OPEN_COUNT, 0)

    fun getAppOpenCount(context: Context) = defaultPrefs(context).getInt(PREF_KEY_APP_OPEN_COUNT, 0)


    fun updateVideoSeenCount(context: Context) {

        var count = getVideoSeenCount(context)
        defaultPrefs(context).edit {
            putInt(PREF_KEY_VIDEO_SEEN_COUNT, ++count)
        }
    }

    fun updatePreviousVideoSeenCount(context: Context) {
        val count = getVideoSeenCount(context)
        defaultPrefs(context).edit {
            putInt(PREF_KEY_PREVIOUS_VIDEO_SEEN_COUNT, count)
        }
    }

    fun getVideoSeenCount(context: Context) = defaultPrefs(context).getInt(PREF_KEY_VIDEO_SEEN_COUNT, 0)

    fun getPreviousVideoSeenCount(context: Context) = defaultPrefs(context).getInt(PREF_KEY_PREVIOUS_VIDEO_SEEN_COUNT, 0)

}