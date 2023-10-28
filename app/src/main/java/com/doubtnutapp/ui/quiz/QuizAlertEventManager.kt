package com.doubtnutapp.ui.quiz

import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import javax.inject.Inject

/**
 *  Created by Pradip Awasthi on 2019-09-23.
 */

class QuizAlertEventManager @Inject constructor(private val analyticsPublisher: AnalyticsPublisher) {

    fun QuizAlertShown(item: String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.EVENT_NAME_QUIZ_ALERT_VIEW, hashMapOf<String, Any>().apply {
            put(EventConstants.EVENT_PRAMA_KEY_STUDENT_ID, item)
        }, ignoreSnowplow = true))
    }

    fun NewQuizAlertShown(item: String, popupTitle: String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.EVENT_NAME_QUIZ_ALERT_VIEW_NEW, hashMapOf<String, Any>().apply {
            put(EventConstants.EVENT_PRAMA_KEY_STUDENT_ID, item)
            put(EventConstants.QUIZ_NOTIFICATION_TITLE, popupTitle)
        }, ignoreSnowplow = true))
    }

}