package com.doubtnutapp.textsolution.event

import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.Constants
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnutapp.utils.BranchIOUtils
import org.json.JSONObject
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-08-28.
 */
class TextSolutionEventManager @Inject constructor(private val analyticsPublisher: AnalyticsPublisher) {

    fun onVideoLikeDislike(isLiked: Boolean, questionId: String, source: String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(if (isLiked) {
            EventConstants.LIKE_VIDEO
        } else {
            EventConstants.DISLIKE_VIDEO
        }, hashMapOf<String, Any>().apply {
            put(EventConstants.QUESTION_ID, questionId)
            put(EventConstants.SOURCE, source)
        }, ignoreSnowplow = true))
    }

    fun onVideoLike(questionId: String, source: String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.LIKE_VIDEO, hashMapOf<String, Any>().apply {
            put(EventConstants.QUESTION_ID, questionId)
            put(EventConstants.SOURCE, source)
        }, ignoreSnowplow = true))
    }

    fun onVideoComment(questionId: String, source: String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.COMMENT_VIDEO_CLICK, hashMapOf<String, Any>().apply {
            put(EventConstants.QUESTION_ID, questionId)
            put(EventConstants.SOURCE, source)
        }, ignoreSnowplow = true))
    }

    fun onVideoShare(questionId: String, source: String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.SHARE_VIDEO_CLICK, hashMapOf<String, Any>().apply {
            put(EventConstants.QUESTION_ID, questionId)
            put(EventConstants.SOURCE, source)
        }, ignoreSnowplow = true))
    }

    fun eventWith(eventName: String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, hashMapOf()))
    }

    fun eventWith(eventName: String, param: HashMap<String, Any>) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, param))
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

        // Send this event to Branch
//        BranchIOUtils.userCompletedAction(
//            EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK,
//            JSONObject().apply {
//                put(Constants.QUESTION_ID, questionId)
//                put(EventConstants.SOURCE, source)
//            })
    }

    fun cameraButtonClicked(source: String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.CAMERA_BUTTON, hashMapOf<String, Any>().apply {
            put(EventConstants.VIEW_SOURCE, source)
        }, ignoreSnowplow = true))
    }

    fun evenWithSnowPlow(eventMap: StructuredEvent){
        analyticsPublisher.publishEvent(eventMap)
    }
}