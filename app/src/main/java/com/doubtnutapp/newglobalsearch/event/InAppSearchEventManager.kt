package com.doubtnutapp.globalsearch.event

import com.doubtnut.analytics.EventConstants.ASSORTMENT_ID
import com.doubtnut.analytics.EventConstants.CLICKED_ITEM
import com.doubtnut.analytics.EventConstants.GLOBAL_SEARCH
import com.doubtnut.analytics.EventConstants.IN_APP_SEARCH_LANDING_VERSION
import com.doubtnut.analytics.EventConstants.ITEM_ID
import com.doubtnut.analytics.EventConstants.ITEM_POSITION
import com.doubtnut.analytics.EventConstants.MENU
import com.doubtnut.analytics.EventConstants.SEARCHED_ITEM
import com.doubtnut.analytics.EventConstants.SEARCH_QUERY
import com.doubtnut.analytics.EventConstants.SECTION
import com.doubtnut.analytics.EventConstants.SOURCE
import com.doubtnut.analytics.EventConstants.TYPE
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-07-19.
 */
class InAppSearchEventManager @Inject constructor(private val analyticsPublisher: AnalyticsPublisher) {

    fun onSearchInitiated(source: String, searchQuery: String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(GLOBAL_SEARCH, hashMapOf<String, Any>().apply {
            put(SOURCE, source)
            put(SEARCH_QUERY, searchQuery)
        }, ignoreSnowplow = true))
    }

    fun eventWith(eventName: String, params: HashMap<String, Any>, ignoreSnowplow: Boolean = false) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, params, ignoreSnowplow = ignoreSnowplow))
    }

    fun eventWith(analyticsEvent: AnalyticsEvent) {
        analyticsPublisher.publishEvent(analyticsEvent)
    }

    fun sendSearchEvent(eventName: String, itemId: String, source: String, searchedItem: String,
                        clickedItem: String, section: String, type: String, position: Int,
                        assortmentId : String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, hashMapOf<String, Any>().apply {
            put(SOURCE, source)
            put(SEARCHED_ITEM, searchedItem)
            put(CLICKED_ITEM, clickedItem)
            put(ITEM_ID, itemId)
            put(ITEM_POSITION, position)
            put(TYPE, type)
            put(SECTION, section)
            put(ASSORTMENT_ID, assortmentId)
        }))
    }

    fun sendKeyboardSearchEvent(eventName: String, source: String, searchedItem: String, ignoreSnowplow: Boolean = false) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, hashMapOf<String, Any>().apply {
            put(SOURCE, source)
            put(SEARCHED_ITEM, searchedItem)
        }, ignoreSnowplow = ignoreSnowplow))
    }

    fun sendTrendingClickEvent(eventName: String, source: String, menu: String, clickedItem: String, type: String, searchLandingVersion: Int, position: String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, hashMapOf<String, Any>().apply {
            put(SOURCE, source)
            put(MENU, menu)
            put(CLICKED_ITEM, clickedItem)
            put(ITEM_POSITION, position)
            put(TYPE, type)
            put(IN_APP_SEARCH_LANDING_VERSION, searchLandingVersion)
        }))
    }

    fun sendNoDataFoundEvent(eventName: String, source: String, searchedItem: String, ignoreSnowplow: Boolean = false) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, hashMapOf<String, Any>().apply {
            put(SOURCE, source)
            put(SEARCHED_ITEM, searchedItem)
        }, ignoreSnowplow = ignoreSnowplow))
    }

    fun sendNewLandingPageClickEvent(eventName: String, source: String, clickedItemName: String, position: String, searchLandingVersion: Int) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, hashMapOf<String, Any>().apply {
            put(SOURCE, source)
            put(CLICKED_ITEM, clickedItemName)
            put(ITEM_POSITION, position)
            put(IN_APP_SEARCH_LANDING_VERSION, searchLandingVersion)
        }))
    }
}