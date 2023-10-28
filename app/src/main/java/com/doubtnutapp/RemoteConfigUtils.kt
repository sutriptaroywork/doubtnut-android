package com.doubtnutapp

import com.google.firebase.remoteconfig.FirebaseRemoteConfig

/**
 * Created by Anand Gaurav on 2020-01-08.
 */
@Deprecated("Use CoreRemoteConfigUtils")
object RemoteConfigUtils {
    private const val CAMERA_COUNT = "CAMERA_COUNT"
    private const val SHOW_EXO_DATE_CHANGE_DIALOG = "SHOW_EXO_DATE_CHANGE_DIALOG"
    private const val EVENT_INFO = "EVENT_INFO"
    private const val COURSE_CLASS_ENABLED = "COURSE_CLASS_ENABLED"
    private const val COMMUNITY_GUIDELINES_JSON = "COMMUNITY_GUIDELINES_JSON"
    private const val LIVE_CLASS_CLASS_ENABLED = "LIVE_CLASS_CLASS_ENABLED"
    private const val OFFLINE_VIDEOS_COUNT = "offline_video_count"

    fun getCameraCountToShow(): Long {
        return FirebaseRemoteConfig.getInstance().getLong(CAMERA_COUNT)
    }

    fun getShowExoDateDialogStatus(): String {
        return FirebaseRemoteConfig.getInstance().getString(SHOW_EXO_DATE_CHANGE_DIALOG)
    }

    fun getEventInfo(): String {
        return FirebaseRemoteConfig.getInstance().getString(EVENT_INFO)
    }

    fun getCourseClassEnabled(): String {
        return FirebaseRemoteConfig.getInstance().getString(COURSE_CLASS_ENABLED)
    }

    fun getLiveClassClassEnabled(): String {
        return FirebaseRemoteConfig.getInstance().getString(LIVE_CLASS_CLASS_ENABLED)
    }

    fun getCommunityGuidelines(): String {
        return FirebaseRemoteConfig.getInstance().getString(COMMUNITY_GUIDELINES_JSON)
    }

    fun getMaxDownloadVideoCount(): Long =
        FirebaseRemoteConfig.getInstance().getLong(OFFLINE_VIDEOS_COUNT)

}