package com.doubtnutapp.course.event

import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-07-17.
 */
class CourseEventManager @Inject constructor(private val analyticsPublisher: AnalyticsPublisher) {

    fun publishEvent(eventName: String, params: HashMap<String, Any>, sendToBranch: Boolean) {
        val paramsCopy: HashMap<String, Any> = HashMap(params)
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, params))
        if (sendToBranch)
            analyticsPublisher.publishBranchIoEvent(AnalyticsEvent(eventName, paramsCopy))
    }
}
