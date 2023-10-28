package com.doubtnutapp.newlibrary.event

import android.content.Context
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.addEventNames
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.data.base.di.qualifier.ApplicationContext
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.libraryhome.library.LibraryFragmentHome
import com.doubtnutapp.utils.NetworkUtils
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-10-25.
 */
class LibraryEventManager @Inject constructor(
    private val analyticsPublisher: AnalyticsPublisher,
    @ApplicationContext private val context: Context,
    private val userPreference: UserPreference
) {

    fun sendEvent(eventName: String) {
        (context as DoubtnutApp).getEventTracker().addEventNames(eventName)
            .addNetworkState(NetworkUtils.isConnected(context).toString())
            .addStudentId(userPreference.getUserStudentId())
            .addScreenName(LibraryFragmentHome.TAG)
            .track()
    }

    fun onLibraryPlaylistTabSelectedFromSetting(tab: String, parentTitle: String) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.SETTING_PLAYLIST_SELECTION,
                hashMapOf<String, Any>().apply {
                    put(EventConstants.MENU_ITEM, tab)
                    put(EventConstants.PARENT, parentTitle)
                },
                ignoreSnowplow = true
            )
        )
        sendEvent(EventConstants.SETTING_PLAYLIST_SELECTION + "_" + parentTitle + "_" + tab)
    }

    fun onLibraryFilterClick(title: String, parentTitle: String) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.LIBRARY_FILTER_CLICK,
                hashMapOf<String, Any>().apply {
                    put(EventConstants.SOURCE, title)
                    put(EventConstants.PARENT, parentTitle)
                },
                ignoreSnowplow = true
            )
        )
        sendEvent(EventConstants.LIBRARY_FILTER_CLICK + "_" + parentTitle + "_" + title)
    }

    fun onLibraryPlaylistTabSelected(tab: String, parentTitle: String) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.LIBRARY_PLAYLIST_TOP_MENU_CLICKED,
                hashMapOf<String, Any>().apply {
                    put(EventConstants.MENU_ITEM, tab)
                    put(EventConstants.PARENT, parentTitle)
                },
                ignoreSnowplow = true
            )
        )
        sendEvent(EventConstants.LIBRARY_PLAYLIST_TOP_MENU_CLICKED + "_" + parentTitle + "_" + tab)
    }

    fun sendEvent(event: String, param: HashMap<String, Any>, ignoreSnowplow: Boolean = false) =
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                event,
                param,
                ignoreSnowplow = ignoreSnowplow
            )
        )
}