package com.doubtnutapp.fcm.notification.video

import android.app.Notification
import android.content.Context
import android.text.TextUtils
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.doubtnutapp.R
import com.doubtnutapp.fcm.notification.NotificationBuilder
import com.doubtnutapp.fcm.notification.NotificationConstants
import com.doubtnutapp.utils.BitmapUtils
import com.doubtnutapp.fcm.notification.PushNotificationItem

/**
 * This class is responsible for building the UI Structure of notification that will be
 * Visible to the user
 * */
class VideoNotificationBuilder : NotificationBuilder {

    override fun build(context: Context, item: PushNotificationItem): Notification {

        val notificationBuilder = NotificationCompat.Builder(context, item.channel().channelId)
                .setSmallIcon(R.mipmap.logo)
                .setColorized(true)
                .setVibrate(null)
                .setContentTitle(getTitle(item))
                .setContentIntent(item.pendingIntent())
                .setAutoCancel(true)

        if (TextUtils.isEmpty(item.message()).not()) {
            notificationBuilder.setContentText(item.message())
        }

        val bitmap = BitmapUtils.getBitmapFromUrl(context, item.imageUrl())

        if (bitmap != null) {
            notificationBuilder.setStyle(NotificationCompat.BigPictureStyle().bigPicture(bitmap))
        } else {
            notificationBuilder.setStyle(NotificationCompat.BigTextStyle().bigText(item.message()))
        }

        return notificationBuilder.let {
            it.color = ContextCompat.getColor(context, R.color.buttonColor)
            it.build()
        }
    }

    private fun getTitle(notificationItem: PushNotificationItem) =
            if (TextUtils.isEmpty(notificationItem.title()).not()) {
                notificationItem.title()
            } else {
                NotificationConstants.DEFAULT_TITLE
            }

}