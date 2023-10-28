package com.doubtnutapp.fcm.notification

import android.app.PendingIntent

interface PushNotificationItem {

    fun channel(): PushNotificationChannel

    fun title(): String

    fun message(): String

    fun imageUrl(): String

    fun pendingIntent(): PendingIntent

    fun data(): Map<String, String>
}