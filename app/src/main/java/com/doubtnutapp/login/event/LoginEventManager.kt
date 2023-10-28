package com.doubtnutapp.login.event

import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.login.model.ApiVerifyUser
import com.facebook.appevents.AppEventsConstants
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-08-23.
 */
class LoginEventManager @Inject constructor(private val analyticsPublisher: AnalyticsPublisher) {

    fun eventWith(eventName: String, ignoreSnowplow: Boolean = false) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, hashMapOf(), ignoreSnowplow = ignoreSnowplow))
    }

    fun eventWith(eventName: String, params: HashMap<String, Any>, ignoreSnowplow: Boolean = false) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, params, ignoreSnowplow = ignoreSnowplow))
    }

    fun onOneTapLoginClick(source: String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.ONE_TAP_LOGIN, hashMapOf<String, Any>().apply {
            put(EventConstants.SOURCE, source)
        }, ignoreSnowplow = true))
    }

    fun onWhatsappOptinClick(source: String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.WHATSAPP_OPTIN_CLICK, hashMapOf<String, Any>().apply {
            put(EventConstants.SOURCE, source)
        }, ignoreSnowplow = true))
    }

    fun sendUserRegistrationEvent(apiVerifyUser: ApiVerifyUser) {
        analyticsPublisher.publishEvent(AnalyticsEvent(AppEventsConstants.EVENT_NAME_COMPLETED_REGISTRATION, hashMapOf<String, Any>().apply {
            put(EventConstants.EVENT_PRAMA_KEY_STUDENT_ID, apiVerifyUser.studentId)
            put(EventConstants.EVENT_PARAM_KEY_STUDENT_NAME, apiVerifyUser.studentUsername.orEmpty())
        }))
    }

    fun sendMoEngageLoginEvent(apiVerifyUser: ApiVerifyUser) {
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.EVENT_NAME_LOGIN, hashMapOf<String, Any>().apply {
            put(EventConstants.LOGIN_STATUS, if (apiVerifyUser.isNewUser)
                EventConstants.EVENT_FIRST_LOGIN
            else
                EventConstants.EVENT_REPEAT_LOGIN
            )
        }, ignoreSnowplow = true))
    }

}