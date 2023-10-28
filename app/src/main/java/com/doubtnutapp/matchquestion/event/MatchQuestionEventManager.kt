package com.doubtnutapp.matchquestion.event

import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.matchquestion.model.MatchedQuestionsList
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-07-23.
 */
class MatchQuestionEventManager @Inject constructor(private val analyticsPublisher: AnalyticsPublisher) {

    fun eventWith(eventName: String, ignoreSnowplow: Boolean = false) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                eventName,
                hashMapOf(),
                ignoreSnowplow = ignoreSnowplow
            )
        )
    }

    fun eventWith(
        eventName: String,
        param: HashMap<String, Any>,
        ignoreSnowplow: Boolean = false,
        ignoreFirebase: Boolean = false
    ) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                eventName,
                param,
                ignoreSnowplow = ignoreSnowplow,
                ignoreFirebase = ignoreFirebase
            )
        )
    }

    fun playVideoClick(questionId: String, source: String) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK,
                hashMapOf<String, Any>().apply {
                    put(EventConstants.QUESTION_ID, questionId)
                    put(EventConstants.SOURCE, source)
                })
        )
    }

    fun playVideoClick(matchedQueInfo: MatchedQuestionsList, source: String) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK,
                hashMapOf<String, Any>().apply {
                    put(EventConstants.QUESTION_ID, matchedQueInfo.id)
                    put(EventConstants.QUESTION_CLASS, matchedQueInfo.clazz ?: "")
                    put(EventConstants.QUESTION_CHAPTER, matchedQueInfo.chapter ?: "")
                    put(EventConstants.QUESTION_RESOURCE_TYPE, matchedQueInfo.resourceType)
                    put(EventConstants.SOURCE, source)
                })
        )
    }

    fun imageUploadFail(
        size: String,
        questionId: String,
        url: String,
        errorMessage: String,
        isRetry: Boolean = false
    ) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.ASK_IMAGE_UPLOAD_FAIL,
                hashMapOf<String, Any>().apply {
                    put(EventConstants.SIZE, size)
                    put(EventConstants.QUESTION_ID, questionId)
                    put(EventConstants.UPLOAD_URL, url)
                    put(EventConstants.IS_RETRIED, isRetry)
                    put(EventConstants.ERROR_MESSAGE, errorMessage)
                },
                ignoreSnowplow = true
            )
        )
    }

    fun askTabClick(source: String) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.ASK_TAB_CLICK,
                hashMapOf<String, Any>().apply {
                    put(EventConstants.SOURCE, source)
                },
                ignoreSnowplow = false
            )
        )
    }

}