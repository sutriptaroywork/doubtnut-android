package com.doubtnutapp.ui.test.event

import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-07-20.
 */
class TestEventManager @Inject constructor(private val analyticsPublisher: AnalyticsPublisher) {

    fun onDailyQuizTopicSelection(testId: String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.DAILY_QUIZ_TOPIC_SELECTION, hashMapOf<String, Any>().apply {
            put(EventConstants.TEST_ID, testId)
        }, ignoreSnowplow = true))
    }

    fun eventWith(eventName: String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, hashMapOf()))
    }

    fun onQuizSubmit(testId: String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.SUBMIT_QUIZ, hashMapOf<String, Any>().apply {
            put(EventConstants.TEST_ID, testId)
        }, ignoreSnowplow = true))
    }

    fun onViewAnswer(testId: String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.VIEW_ANSWERS, hashMapOf<String, Any>().apply {
            put(EventConstants.TEST_ID, testId)
        }, ignoreSnowplow = true))
    }
}