package com.doubtnutapp.libraryhome.event

import android.content.Context
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnutapp.Constants
import com.doubtnutapp.addEventNames
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.data.base.di.qualifier.ApplicationContext
import com.doubtnutapp.defaultPrefs
import com.doubtnutapp.orDefaultValue
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil.getStudentClass
import com.doubtnutapp.utils.UserUtil.getStudentId
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-07-18.
 */
class LiveClassesEventManager @Inject constructor(private val analyticsPublisher: AnalyticsPublisher,
                                                  @ApplicationContext val context: Context,
                                                  private val eventTracker: Tracker) {

    fun eventWith(eventName: String, params: HashMap<String, Any>) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, params))
    }

    fun onLibraryTabSelected(tab: String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.LIBRARY_TOP_MENU_CLICKED, hashMapOf<String, Any>().apply {
            put(EventConstants.MENU_ITEM, tab)
        }, ignoreSnowplow = true))
    }

    fun sendFirebaseEvent(eventName: String, screenName: String) {
        eventTracker.addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(context).toString())
                .addStudentId(getStudentId())
                .addEventParameter(EventConstants.PARAM_CLASS,getStudentClass())
                .addScreenName(screenName)
                .track()
    }
}