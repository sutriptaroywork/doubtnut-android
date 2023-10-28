package com.doubtnutapp.libraryhome.event

import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.EventConstants.MENU_ITEM
import com.doubtnut.analytics.EventConstants.TEST_NAME
import com.doubtnut.analytics.EventConstants.TOPIC_NAME
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-07-18.
 */
class LibraryEventManager @Inject constructor(private val analyticsPublisher: AnalyticsPublisher) {

    fun eventWith(eventName: String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, hashMapOf()))
    }

    fun onLibraryTabSelected(tab: String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.LIBRARY_TOP_MENU_CLICKED, hashMapOf<String, Any>().apply {
            put(MENU_ITEM, tab)
        }, ignoreSnowplow = true))
    }

    fun onLibraryItemClicked(topicName: String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.LIBRARY_ITEM_CLICKED, hashMapOf<String, Any>().apply {
            put(TOPIC_NAME, topicName)
        }, ignoreSnowplow = true))
    }

    fun onLibraryPlaylistTabSelectedFromSetting(tab: String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.SETTING_PLAYLIST_SELECTION, hashMapOf<String, Any>().apply {
            put(MENU_ITEM, tab)
        }, ignoreSnowplow = true))
    }

    fun onMockTestItemClicked(testName: String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.LIBRARY_MOCK_TEST_SELECTED, hashMapOf<String, Any>().apply {
            put(TEST_NAME, testName)
        }, ignoreSnowplow = true))
    }

    fun eventWith(eventName: String, params: HashMap<String, Any>) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, params))
    }
}