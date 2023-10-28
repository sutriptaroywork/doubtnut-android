package com.doubtnutapp.pcmunlockpopup.event

import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-08-01.
 */
class PCMUnlockPopUpEventManager @Inject constructor(private val analyticsPublisher: AnalyticsPublisher) {

    fun eventWith(eventName: String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, hashMapOf()))
    }

    fun popUpPcClick(source: String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.POP_UP_PC_CLICK, hashMapOf<String, Any>().apply {
            put(EventConstants.SOURCE, source)
        }))
    }

}