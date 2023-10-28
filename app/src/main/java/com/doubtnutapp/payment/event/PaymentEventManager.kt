package com.doubtnutapp.payment.event

import android.content.Context
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.addEventNames
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.data.base.di.qualifier.ApplicationContext
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.vipplan.ui.VipPlanActivity
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-12-24.
 */
class PaymentEventManager @Inject constructor(
    private val analyticsPublisher: AnalyticsPublisher,
    @ApplicationContext private val context: Context,
    private val userPreference: UserPreference
) {

    fun eventWith(eventName: String, variantId: String, ignoreSnowplow: Boolean = false) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                eventName,
                hashMapOf(EventConstants.VARIANT_ID to variantId),
                ignoreSnowplow = ignoreSnowplow
            )
        )
        sendEvent(eventName)
    }

    fun eventWith(eventName: String, variantId: String, paymentSource: String, ignoreSnowplow: Boolean = false) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, hashMapOf<String, Any>().apply {
            put(EventConstants.SOURCE, paymentSource)
            put(EventConstants.VARIANT_ID, variantId)
        }, ignoreSnowplow = ignoreSnowplow))
        sendEvent(eventName)
    }

    fun sendEvent(eventName: String) {
        (context as DoubtnutApp).getEventTracker().addEventNames(eventName)
            .addNetworkState(NetworkUtils.isConnected(context).toString())
            .addStudentId(userPreference.getUserStudentId())
            .addScreenName(VipPlanActivity.TAG)
            .track()
    }

    fun publishPaymentSelection(id: String, variantId: String) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.EVENT_NAME_VIP_PLAN_SELECTION,
                hashMapOf<String, Any>().apply {
                    put(EventConstants.SOURCE, id)
                    put(EventConstants.VARIANT_ID, variantId)
                }, ignoreSnowplow = true)
        )
    }
}