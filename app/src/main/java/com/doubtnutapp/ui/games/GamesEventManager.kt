package com.doubtnutapp.ui.games

import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnutapp.base.PublishSnowplowEvent
import javax.inject.Inject

class GamesEventManager @Inject constructor(private val publisher: AnalyticsPublisher) {

    fun sendGameClickEvent(gameName: String) {
        publisher.publishEvent(AnalyticsEvent(EventConstants.EVENT_GAME_CLICK, hashMapOf(
                EventConstants.EVENT_GAME to gameName,
                EventConstants.SOURCE to EventConstants.EVENT_SOURCE_ALL_GAME_SCREEN
        )))
    }

    fun eventWith(eventName: String, hashMap: HashMap<String, Any> = hashMapOf()) {
        publisher.publishEvent(AnalyticsEvent(eventName, hashMap))
    }

    fun eventWith(snowplowEvent: StructuredEvent){
        publisher.publishEvent(snowplowEvent)
    }
}