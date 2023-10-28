package com.doubtnutapp.gamification.mybio.event

import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import javax.inject.Inject

class MyBioEventManager @Inject constructor(private val analyticsPublisher: AnalyticsPublisher) {

    fun eventWith(eventName: String, ignoreSnowplow: Boolean = false) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, hashMapOf(), ignoreSnowplow = ignoreSnowplow))
    }

    fun eventWith(eventName: String, params: HashMap<String, Any>, ignoreSnowplow: Boolean = false) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, params, ignoreSnowplow = ignoreSnowplow))
    }
}