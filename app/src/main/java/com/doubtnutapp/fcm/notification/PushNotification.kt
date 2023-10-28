package com.doubtnutapp.fcm.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi


/**
 * Main Entry Point of the notification flow. FirebaseMessagingService would have the
 * instance of this class.
 * @param notificationManager provides an API to notify the user with the notification, it is Android
 *                            platform class.
 * @param resolver it will give you the concrete implementation of NotificationItem according to the id
 *                 received in FCM RemoteMessage.
 *
 *@param notificationBuilderFactory it will give the concrete implementation of NotificationBuilder class
 *
 *
 * */
class PushNotification constructor(private val notificationManager: NotificationManager,
                                   private val resolver: NotificationItemResolver,
                                   private val notificationBuilderFactory: NotificationBuilderFactory) {

    fun show(context: Context, data: Map<String, String>) {

        val id = data[NotificationConstants.NOTIFICATION_PARAM_EVENT] ?: "default"

        val notificationItem = resolver.resolve(context, data)

        notificationItem?.also { it ->

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!notificationChannelExists(it.channel().channelId)) {
                    createChannel(context, it.channel())
                }
            }

            val notificationBuilder = notificationBuilderFactory.provideNotificationBuilder(id)

            notificationBuilder?.also { builder ->
                notificationManager.notify(
                        id.hashCode()/*Integer*/,
                        builder.build(context, it)
                )
            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel(context: Context, channel: PushNotificationChannel) {
        val channelTitle = context.getString(channel.titleResource)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val notificationChannel = NotificationChannel(channel.channelId, channelTitle, importance)
        notificationChannel.setShowBadge(true)
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        notificationManager.createNotificationChannel(notificationChannel)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun notificationChannelExists(channelId: String): Boolean =
            notificationManager.getNotificationChannel(channelId) != null
}