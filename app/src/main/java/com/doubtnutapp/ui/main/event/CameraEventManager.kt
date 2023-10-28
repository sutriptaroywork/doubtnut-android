package com.doubtnutapp.ui.main.event

import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-07-18.
 */
class CameraEventManager @Inject constructor(private val analyticsPublisher: AnalyticsPublisher) {

    fun onDemoVideoClick(cameraVersion: String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.EVENT_NAME_DEMO_VIDEO_CLICKED, hashMapOf<String, Any>().apply {
            put(EventConstants.EVENT_NAME_CAMERA_VERSION, cameraVersion)
        }, ignoreSnowplow = true))
    }

    fun onCameraPermission(allow: Boolean, cameraVersion: String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.EVENT_NAME_CAMERA_PERMISSION, hashMapOf<String, Any>().apply {
            put(EventConstants.PARAM_ALLOW, allow)
            put(EventConstants.EVENT_NAME_CAMERA_VERSION, cameraVersion)
        }, ignoreSnowplow = true))
    }

    fun onDemoQuestionClick(title: String, cameraVersion: String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.EVENT_NAME_DEMO_QUESTION_CLICKED, hashMapOf<String, Any>().apply {
            put(EventConstants.EVENT_NAME_TITLE, title)
            put(EventConstants.EVENT_NAME_CAMERA_VERSION, cameraVersion)
        }, ignoreSnowplow = true))
    }

    fun eventWith(eventName: String, cameraVersion: String, ignoreSnowplow: Boolean = false) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, hashMapOf<String, Any>().apply {
            put(EventConstants.EVENT_NAME_CAMERA_VERSION, cameraVersion)
        }, ignoreSnowplow = ignoreSnowplow))
    }

    fun onVipPlanViewClick(source: String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.EVENT_NAME_PLAN_VIEW, hashMapOf<String, Any>().apply {
            put(EventConstants.SOURCE, source)
        }, ignoreSnowplow = true))
    }

    fun sendEvent(eventName: String, params: HashMap<String, Any>, ignoreSnowplow: Boolean = false) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, params, ignoreSnowplow = ignoreSnowplow))
    }

}