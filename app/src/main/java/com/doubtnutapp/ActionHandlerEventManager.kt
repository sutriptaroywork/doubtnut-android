package com.doubtnutapp

import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Anand Gaurav on 2019-07-23.
 */
@Singleton
class ActionHandlerEventManager @Inject constructor(private val analyticsPublisher: AnalyticsPublisher) {

    fun onNotificationOpen(item: String, source: String) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.NOTIFICATION_OPENED,
                hashMapOf<String, Any>().apply {
                    put(EventConstants.ITEM, item)
                    put(EventConstants.SOURCE, source)
                },
                ignoreSnowplow = true
            )
        )
    }

    fun eventWith(eventName: String, param: HashMap<String, Any> = hashMapOf(), ignoreSnowplow: Boolean = false) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, param, ignoreSnowplow = ignoreSnowplow))
    }
}
