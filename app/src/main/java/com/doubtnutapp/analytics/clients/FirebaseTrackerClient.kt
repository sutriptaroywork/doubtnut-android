package com.doubtnutapp.analytics.clients

import android.content.Context
import android.os.Bundle
import com.doubtnut.analytics.AppEvent
import com.doubtnut.analytics.TrackerClient
import com.doubtnut.core.analytics.TrackerConstants
import com.doubtnutapp.utils.UserUtil
import com.google.firebase.analytics.FirebaseAnalytics

class FirebaseTrackerClient(context: Context) : TrackerClient {

    private val mFirebaseAnalytics by lazy { FirebaseAnalytics.getInstance(context) }

    override val id: String = TrackerConstants.TRACKER_CLIENT_FIREBASE

    override fun track(appEvent: AppEvent, appEventParams: Map<String, *>) {
        val bundle = Bundle()

        for ((key, value) in appEventParams) {
            when (value) {
                is Int -> bundle.putInt(key, value)
                is String -> bundle.putString(key, value)
                is Long -> bundle.putLong(key, value)
                is Float -> bundle.putFloat(key, value)
                is Boolean -> bundle.putBoolean(key, value)
            }
        }

        mFirebaseAnalytics.logEvent(appEvent.eventName, bundle)
    }

    override fun defaultParams(): MutableMap<String, Any> {
        return hashMapOf<String, Any>().apply {
            put("selected_board", UserUtil.getUserBoard())
            put("selected_exam", UserUtil.getUserExams())
            put("student_id", UserUtil.getStudentId())
            put("student_class", UserUtil.getStudentClass())
            put("student_language", UserUtil.getUserLanguage())
        }
    }
}
