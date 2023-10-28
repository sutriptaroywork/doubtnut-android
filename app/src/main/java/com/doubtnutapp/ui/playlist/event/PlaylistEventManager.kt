package com.doubtnutapp.ui.playlist.event

import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.EventConstants.ADD_TO_PLAYLIST
import com.doubtnut.analytics.EventConstants.ADD_TO_PLAYLIST_CLICK
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-07-19.
 */
class PlaylistEventManager @Inject constructor(private val analyticsPublisher: AnalyticsPublisher) {

    fun onAddToPlaylistClick(questionId: String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(ADD_TO_PLAYLIST, hashMapOf<String, Any>().apply {
            put(EventConstants.QUESTION_ID, questionId)
        }))
    }

    fun onAddToPlaylistClicked(questionId: String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(ADD_TO_PLAYLIST_CLICK, hashMapOf<String, Any>().apply {
            put(EventConstants.QUESTION_ID, questionId)
        }))
    }

    fun eventWith(eventName: String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, hashMapOf()))
    }
}