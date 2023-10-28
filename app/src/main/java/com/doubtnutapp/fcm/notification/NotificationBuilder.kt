package com.doubtnutapp.fcm.notification

import android.app.Notification
import android.content.Context


interface NotificationBuilder {

    fun build(context: Context, item: PushNotificationItem): Notification
}