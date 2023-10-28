package com.doubtnutapp.store.event

import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-07-19.
 */
class MyOrderEventManager @Inject constructor(private val analyticsPublisher: AnalyticsPublisher) {

    fun eventWith(eventName: String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, hashMapOf()))
    }
}