package com.doubtnutapp.videoPage.event

import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.EventConstants.COMMENT_VIDEO_CLICK
import com.doubtnut.analytics.EventConstants.DISLIKE_VIDEO
import com.doubtnut.analytics.EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK
import com.doubtnut.analytics.EventConstants.LIKE_VIDEO
import com.doubtnut.analytics.EventConstants.QUESTION_ID
import com.doubtnut.analytics.EventConstants.SHARE_VIDEO_CLICK
import com.doubtnut.analytics.EventConstants.SOURCE
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.RemoteConfigUtils
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnutapp.utils.Utils
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-07-17.
 */
class VideoPageEventManager @Inject constructor(private val analyticsPublisher: AnalyticsPublisher) {

    fun onVideoLikeDislike(isLiked: Boolean, questionId: String, source: String) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(if (isLiked) {
                LIKE_VIDEO
            } else {
                DISLIKE_VIDEO
            }, hashMapOf<String, Any>().apply {
                put(QUESTION_ID, questionId)
                put(SOURCE, source)
            }, ignoreSnowplow = true)
        )
    }

    fun onVideoLike(questionId: String, source: String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(LIKE_VIDEO, hashMapOf<String, Any>().apply {
            put(QUESTION_ID, questionId)
            put(SOURCE, source)
        }, ignoreSnowplow = true))
    }

    fun onVideoComment(questionId: String, source: String) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                COMMENT_VIDEO_CLICK,
                hashMapOf<String, Any>().apply {
                    put(QUESTION_ID, questionId)
                    put(SOURCE, source)
                }, ignoreSnowplow = true)
        )
    }

    fun onVideoShare(questionId: String, source: String) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                SHARE_VIDEO_CLICK,
                hashMapOf<String, Any>().apply {
                    put(QUESTION_ID, questionId)
                    put(SOURCE, source)
                }, ignoreSnowplow = true)
        )
    }

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
        eventMap: HashMap<String, Any>,
        ignoreSnowplow: Boolean = false,
        ignoreFirebase: Boolean = false
    ) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                eventName,
                eventMap,
                ignoreSnowplow = ignoreSnowplow,
                ignoreFirebase = ignoreFirebase
            )
        )
    }

    fun evenWithSnowPlow(eventMap: StructuredEvent) {
        analyticsPublisher.publishEvent(eventMap)
    }

    fun playVideoClick(questionId: String, source: String) {
        val map = hashMapOf<String, Any>().apply {
            put(QUESTION_ID, questionId)
            put(SOURCE, source)
        }

        Utils.sendClassLangEvents(EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK)
        Utils.sendClassLanguageSpecificEvent(EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK)

        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EVENT_NAME_PLAY_VIDEO_CLICK,
                map
            )
        )

        val countToSend: Int =
            Utils.getCountToSend(
                RemoteConfigUtils.getEventInfo(),
                EVENT_NAME_PLAY_VIDEO_CLICK
            )
        repeat((0 until countToSend).count()) {
            analyticsPublisher.publishBranchIoEvent(
                AnalyticsEvent(
                    EVENT_NAME_PLAY_VIDEO_CLICK,
                    map
                )
            )
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EVENT_NAME_PLAY_VIDEO_CLICK,
                    map,
                    ignoreApxor = true,
                    ignoreBranch = true,
                    ignoreFacebook = true,
                    ignoreSnowplow = true,
                    ignoreMoengage = true,
                    ignoreFirebase = false
                )
            )
        }
    }

    fun cameraButtonClicked(source: String) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.CAMERA_BUTTON,
                hashMapOf<String, Any>().apply {
                    put(EventConstants.VIEW_SOURCE, source)
                },
                ignoreSnowplow = true
            )
        )
    }
}