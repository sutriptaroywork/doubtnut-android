package com.doubtnutapp.fcm.notification

import com.doubtnutapp.R

/**
 * @param channelId used by the system and must be unique
 * @param titleResource name of the channel that will be visible to the user
 */
sealed class PushNotificationChannel(val channelId: String, val titleResource: Int)
class VideoNotificationChannel : PushNotificationChannel(NotificationConstants.CHANNEL_ID_VIDEO, R.string.notification_channel_video)
