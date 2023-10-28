package com.doubtnutapp.fcm.notification

import com.doubtnutapp.fcm.notification.video.VideoNotificationBuilder

/**
 * Responsible for providing the correct builder on receiving the type received in the FCM RemoteMessage.
 * */
class NotificationBuilderFactory {

    fun provideNotificationBuilder(type: String): NotificationBuilder? {
        when (type) {
            NotificationConstants.VIDEO_NOTIFICATION -> return VideoNotificationBuilder()
        }
        return null
    }
}