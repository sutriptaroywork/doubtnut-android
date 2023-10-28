package com.doubtnutapp.fcm.notification

import android.content.Context
import com.doubtnutapp.fcm.notification.video.VideoNotificationItem

class NotificationItemResolver {

    fun resolve(context: Context, data: Map<String, String>): PushNotificationItem? {

        val type = data[NotificationConstants.NOTIFICATION_PARAM_EVENT] ?: ""

        return when (type) {
            NotificationConstants.VIDEO_NOTIFICATION -> prepareNewTrainerNotification(context, data)
            else -> null
        }
    }


    private fun prepareNewTrainerNotification(context: Context, data: Map<String, String>): PushNotificationItem? {
        return VideoNotificationItem(context, data).also {
            it.title = data[NotificationConstants.NOTIFICATION_PARAM_TITLE] ?: ""
            it.imageUrl = data[NotificationConstants.NOTIFICATION_PARAM_IMAGE_URL] ?: ""
            it.message = data[NotificationConstants.NOTIFICATION_PARAM_MESSAGE] ?: ""
            it.tag = data[NotificationConstants.NOTIFICATION_EVENT_TAG] ?: ""
        }
    }

}