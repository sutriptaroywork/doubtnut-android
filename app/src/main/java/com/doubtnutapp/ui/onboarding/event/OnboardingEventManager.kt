package com.doubtnutapp.ui.onboarding.event

import com.doubtnut.analytics.EventConstants.CHANGE_CLASS_FROM_HOME
import com.doubtnut.analytics.EventConstants.CHANGE_CLASS_FROM_LIBRARY
import com.doubtnut.analytics.EventConstants.CHANGE_CLASS_FROM_PROFILE
import com.doubtnut.analytics.EventConstants.CLASS_SELECTED
import com.doubtnut.analytics.EventConstants.EVENT_NAME_BTN_VERIFY_OTP_CLICK
import com.doubtnut.analytics.EventConstants.EVENT_NAME_SPLASH_CLASS_ITEM_CLICK
import com.doubtnut.analytics.EventConstants.EVENT_PRAMA_KEY_SCREEN_NAME
import com.doubtnut.analytics.EventConstants.OTP_SUCCESS
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-07-18.
 */
class OnboardingEventManager @Inject constructor(private val analyticsPublisher: AnalyticsPublisher) {

    fun eventWith(eventName: String, ignoreSnowplow: Boolean = false) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                eventName,
                hashMapOf(),
                ignoreSnowplow = ignoreSnowplow
            )
        )
    }

    fun onClassSelectFromHome(classSelected: String) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                CHANGE_CLASS_FROM_HOME,
                hashMapOf<String, Any>().apply {
                    put(CLASS_SELECTED, classSelected)
                }, ignoreSnowplow = true)
        )
    }

    fun onClassSelectFromLibrary(classSelected: String) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                CHANGE_CLASS_FROM_LIBRARY,
                hashMapOf<String, Any>().apply {
                    put(CLASS_SELECTED, classSelected)
                })
        )
    }

    fun onSplashClassSelect(classSelected: String) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EVENT_NAME_SPLASH_CLASS_ITEM_CLICK,
                hashMapOf<String, Any>().apply {
                    put(CLASS_SELECTED, classSelected)
                }, ignoreSnowplow = true)
        )
    }

    fun onClassSelectFromProfile(classSelected: String) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                CHANGE_CLASS_FROM_PROFILE,
                hashMapOf<String, Any>().apply {
                    put(CLASS_SELECTED, classSelected)
                })
        )
    }

    fun onVerifyClick(screenName: String) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EVENT_NAME_BTN_VERIFY_OTP_CLICK,
                hashMapOf<String, Any>().apply {
                    put(EVENT_PRAMA_KEY_SCREEN_NAME, screenName)
                },
                ignoreSnowplow = true
            )
        )
    }

    fun onOtpSubmitSuccess(screenName: String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(OTP_SUCCESS, hashMapOf<String, Any>().apply {
            put(EVENT_PRAMA_KEY_SCREEN_NAME, screenName)
        }))
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

    fun publishMoengageEvent(eventName: String, params: HashMap<String, Any>) {
        analyticsPublisher.publishMoEngageEvent(AnalyticsEvent(eventName, params))
    }
}