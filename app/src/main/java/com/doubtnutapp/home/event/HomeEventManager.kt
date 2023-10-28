package com.doubtnutapp.home.event

import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.EventConstants.CAMERA_BUTTON
import com.doubtnut.analytics.EventConstants.EVENT_BANNER_CLICK
import com.doubtnut.analytics.EventConstants.EVENT_HOME_FEED_API_ERROR
import com.doubtnut.analytics.EventConstants.EVENT_NAME_HOME_PAGE_CLICK
import com.doubtnut.analytics.EventConstants.EVENT_NAME_SEARCH_ICON_CLICK
import com.doubtnut.analytics.EventConstants.ITEM
import com.doubtnut.analytics.EventConstants.SOURCE
import com.doubtnut.analytics.EventConstants.TOP_ICON_CLICK
import com.doubtnut.analytics.EventConstants.VIEW_ALL_CLICK
import com.doubtnut.analytics.EventConstants.VIEW_SOURCE
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.analytics.model.StructuredEvent
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-07-17.
 */
class HomeEventManager @Inject constructor(private val analyticsPublisher: AnalyticsPublisher) {

    fun onTopItemClick(item: String) {
        var eventName = TOP_ICON_CLICK
        if (item == "show_more" || item == "show_less") {
            eventName += "_$item"
        } else {
            analyticsPublisher.publishMoEngageEvent(AnalyticsEvent(eventName, hashMapOf(
                    ITEM to item
            ), ignoreSnowplow = true))
        }
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, hashMapOf<String, Any>().apply {
            put(ITEM, item)
        }, ignoreSnowplow = true))
    }

    fun onSearchIconClick(source: String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(EVENT_NAME_SEARCH_ICON_CLICK, hashMapOf<String, Any>().apply {
            put(SOURCE, source)
        },ignoreSnowplow = true))
    }

    fun eventWith(eventName: String, ignoreSnowplow: Boolean = false) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, hashMapOf(), ignoreSnowplow = ignoreSnowplow))
    }

    fun eventWith(eventName: String, params: HashMap<String, Any>, ignoreSnowplow: Boolean = false) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, params, ignoreSnowplow = ignoreSnowplow))
    }

    fun viewAllClicked(source: String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(VIEW_ALL_CLICK, hashMapOf<String, Any>().apply {
            put(SOURCE, source)
        }))
    }

    fun cameraButtonClicked(source: String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(CAMERA_BUTTON, hashMapOf<String, Any>().apply {
            put(VIEW_SOURCE, source)
        }, ignoreSnowplow = true))
    }

    fun onHomeClick(source: String, title: String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(EVENT_NAME_HOME_PAGE_CLICK, hashMapOf<String, Any>().apply {
            put(SOURCE, source)
            put(ITEM, title)
        }))
    }

    fun homeFeedApiError(errorMessage: String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(EVENT_HOME_FEED_API_ERROR, hashMapOf<String, Any>().apply {
            put(SOURCE, errorMessage)
        }, ignoreSnowplow = true))
    }

    fun onHomeBannerClick(source: String, title: String, id: String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(EVENT_BANNER_CLICK, hashMapOf<String, Any>().apply {
            put(SOURCE, source)
            put(ITEM, title)
            put(VIEW_SOURCE, id)
        }))
    }

    fun onVipPlanViewClick(source: String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.EVENT_NAME_PLAN_VIEW, hashMapOf<String, Any>().apply {
            put(SOURCE, source)
        }, ignoreSnowplow = true))
    }

    fun publishEvent(event: AnalyticsEvent) {
        analyticsPublisher.publishEvent(event)
    }

    fun publishEvent(event: StructuredEvent) {
        analyticsPublisher.publishEvent(event)
    }
}