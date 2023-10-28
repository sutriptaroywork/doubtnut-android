package com.doubtnutapp.ui.mockTest.event

import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-07-20.
 */
class MockTestEventManager @Inject constructor(private val analyticsPublisher: AnalyticsPublisher) {

    fun mockTestTopicSelected(subject: String, testName: String) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.MOCK_TEST_TOPIC_SELECTED,
                hashMapOf<String, Any>().apply {
                    put(EventConstants.SUBJECT, subject)
                    put(EventConstants.TEST_NAME, testName)
                }, ignoreSnowplow = true)
        )
    }

    fun eventWith(eventName: String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, hashMapOf()))
    }

    fun mockTestViewWinners() {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.VIEW_WINNER,
                hashMapOf<String, Any>().apply {
                    put(EventConstants.TEST_TYPE, "MockTest")
                })
        )
    }
}