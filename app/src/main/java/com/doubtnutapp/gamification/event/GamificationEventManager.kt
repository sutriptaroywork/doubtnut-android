package com.doubtnutapp.gamification.event

import android.content.Context
import com.doubtnut.analytics.EventConstants.EVENT_NAME_OTHERS_PROFILE_CLICK
import com.doubtnut.analytics.EventConstants.PROFILE_SHARE_CLICK
import com.doubtnut.analytics.EventConstants.SOURCE
import com.doubtnut.analytics.Tracker
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.data.base.di.qualifier.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Anand Gaurav on 2019-07-26.
 */
@Singleton
class GamificationEventManager @Inject constructor(
    private val analyticsPublisher: AnalyticsPublisher,
    @ApplicationContext val context: Context,
    private val eventTracker: Tracker
) {

    fun onProfileShareClick(source: String) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                PROFILE_SHARE_CLICK,
                hashMapOf<String, Any>().apply {
                    put(SOURCE, source)
                }, ignoreSnowplow = true
            )
        )
    }

    fun onOtherProfileClick(source: String) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EVENT_NAME_OTHERS_PROFILE_CLICK,
                hashMapOf<String, Any>().apply {
                    put(SOURCE, source)
                })
        )
    }

    fun eventWith(eventName: String, ignoreSnowplow: Boolean = false) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                eventName,
                hashMapOf(),
                ignoreSnowplow = ignoreSnowplow
            )
        )
    }

    fun eventWith(eventName: String, params: HashMap<String, Any>) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, params))
    }
}