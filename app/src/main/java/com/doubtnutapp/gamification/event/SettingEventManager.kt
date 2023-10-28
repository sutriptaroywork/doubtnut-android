package com.doubtnutapp.gamification.event

import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.EventConstants.CHANGE_LANGUAGE_CLICK
import com.doubtnut.analytics.EventConstants.CONTEST_ITEM_CLICK
import com.doubtnut.analytics.EventConstants.ITEM
import com.doubtnut.analytics.EventConstants.MY_PDF_CLICK
import com.doubtnut.analytics.EventConstants.MY_PLAYLIST_CLICK
import com.doubtnut.analytics.EventConstants.PROFILE_MOCK_TEST_SELECTED
import com.doubtnut.analytics.EventConstants.SETTING_ITEM_CLICK
import com.doubtnut.analytics.EventConstants.VIDEO_WATCHED_CLICK
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-07-18.
 */
class SettingEventManager @Inject constructor(private val analyticsPublisher: AnalyticsPublisher) {

    fun onItemClick(featureType: String) {
        var item = SETTING_ITEM_CLICK
        if (featureType == "my_pdf") {
            item = MY_PDF_CLICK
        } else if (featureType == "my_playlist") {
            item = MY_PLAYLIST_CLICK
        } else if (featureType == "watched_video") {
            item = VIDEO_WATCHED_CLICK
        } else if (featureType == "win_patym_cash") {
            item = CONTEST_ITEM_CLICK
        } else if (featureType == "change_language") {
            item = CHANGE_LANGUAGE_CLICK
        } else if (featureType == "mock_test") {
            item = PROFILE_MOCK_TEST_SELECTED
        }
        analyticsPublisher.publishEvent(AnalyticsEvent(item, hashMapOf(),
            ignoreSnowplow = item == VIDEO_WATCHED_CLICK || item == CHANGE_LANGUAGE_CLICK || item == MY_PLAYLIST_CLICK))
    }

    fun settingMenuItemClick(item: String, ignoreSnowplow: Boolean = false) {
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.SETTING_MENU_ITEM_CLICKED, hashMapOf<String, Any>().apply {
            put(ITEM, item)
        }, ignoreSnowplow = ignoreSnowplow))
    }

    fun settingLogoutClick() {
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.EVENT_NAME_LOGOUT, hashMapOf(), ignoreSnowplow = true))
    }

    fun eventWith(eventName: String, ignoreSnowplow: Boolean = false) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, hashMapOf(), ignoreSnowplow = ignoreSnowplow))
    }
}