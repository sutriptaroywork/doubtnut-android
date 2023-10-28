package com.doubtnutapp

import android.content.Context
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.EventConstants.CAMERA_BUTTON
import com.doubtnut.analytics.EventConstants.SOURCE
import com.doubtnut.analytics.EventConstants.VIEW_SOURCE
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.data.base.di.qualifier.ApplicationContext
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.utils.NetworkUtils
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-07-18.
 */
class MainViewEventManager @Inject constructor(
    private val analyticsPublisher: AnalyticsPublisher,
    @ApplicationContext private val context: Context,
    private val userPreference: UserPreference
) {

    fun onBottomItemClick(item: String, ignoreSnowplow: Boolean = false) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                item,
                hashMapOf(),
                ignoreSnowplow = ignoreSnowplow
            )
        )
    }

    fun onCameraClick(viewSource: String) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                CAMERA_BUTTON,
                hashMapOf<String, Any>().apply {
                    put(VIEW_SOURCE, viewSource)
                },
                ignoreSnowplow = true
            )
        )
    }

    fun eventWith(eventName: String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, hashMapOf()))
        sendEvent(eventName)
    }

    fun eventWith(
        eventName: String,
        params: HashMap<String, Any>,
        ignoreSnowplow: Boolean = false
    ) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                eventName,
                params,
                ignoreSnowplow = ignoreSnowplow
            )
        )
    }

    fun publishRewardSystemEvent(
        eventName: String,
        params: HashMap<String, Any>,
        ignoreSnowplow: Boolean = false
    ) {
        params[Constants.CURRENT_LEVEL] = userPreference.getRewardSystemCurrentLevel()
        params[Constants.CURRENT_DAY] = userPreference.getRewardSystemCurrentDay()
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                eventName,
                params,
                ignoreSnowplow = ignoreSnowplow
            )
        )
    }

    fun sendEvent(eventName: String) {
        (context as DoubtnutApp).getEventTracker().addEventNames(eventName)
            .addNetworkState(NetworkUtils.isConnected(context).toString())
            .addStudentId(userPreference.getUserStudentId())
            .addScreenName(MainActivity.TAG)
            .track()
    }

    fun onTransactionViewClick(source: String) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.EVENT_NAME_PAYMENT_HISTORY_BUTTON_CLICK,
                hashMapOf<String, Any>().apply {
                    put(SOURCE, source)
                },
                ignoreSnowplow = true
            )
        )
    }

}