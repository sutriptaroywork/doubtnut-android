package com.doubtnutapp.eventmanager

import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-08-01.
 */
class CommonEventManager @Inject constructor(private val analyticsPublisher: AnalyticsPublisher) {

    fun eventWith(eventName: String, ignoreSnowplow: Boolean = false) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, hashMapOf(), ignoreSnowplow = ignoreSnowplow))
    }

    fun onPcBannerClick(source: String, parent: String?) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.PC_BANNER_CLICK,
                hashMapOf<String, Any>().apply {
                    put(EventConstants.SOURCE, source)
                    put(EventConstants.PARENT, parent.orEmpty())
                }
            )
        )
    }

    fun publishEvent(event: AnalyticsEvent) {
        analyticsPublisher.publishEvent(event)
    }
}
