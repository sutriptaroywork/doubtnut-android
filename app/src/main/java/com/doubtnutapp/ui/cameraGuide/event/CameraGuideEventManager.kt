package com.doubtnutapp.ui.cameraGuide.event

import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-07-23.
 */
class CameraGuideEventManager @Inject constructor(private val analyticsPublisher: AnalyticsPublisher) {

    fun eventWith(eventName: String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, hashMapOf()))
    }

    fun cameraButtonClicked(source: String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.CAMERA_BUTTON, hashMapOf<String, Any>().apply {
            put(EventConstants.VIEW_SOURCE, source)
        }, ignoreSnowplow = true))
    }

}