package com.doubtnutapp.workmanager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.ActionHandlerActivity
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.fcm.notification.NotificationConstants
import com.doubtnutapp.ui.ask.receiver.OnMatchNotificationDismissReceiver
import com.doubtnutapp.utils.ApxorUtils
import com.google.gson.JsonObject

/**
 * Created by Sachin Saxena on 2020-04-27.
 */
object OcrFromImageNotificationManager {

    fun handleNotification(context: Context, ocr: String, title: String? = null, message: String? = null, notificationId: Long) {

        val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
                        ?: return

        val channelId = NotificationConstants.OCR_FROM_IMAGE_CHANNEL_ID

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = NotificationConstants.OCR_FROM_IMAGE_CHANNEL_NAME
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(
                    channelId, channelName, importance)
            notificationManager.createNotificationChannel(mChannel)
        }

        val contentTitle = if (title.isNullOrEmpty()) context.getString(R.string.match_notification_title) else title
        val contentText = if (message.isNullOrEmpty()) context.getString(R.string.match_notification_description) else message

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.logo)
                .setColorized(true)
                .setAutoCancel(true)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_SOUND)
                .setVibrate(longArrayOf(0L))

        notificationBuilder.setContentIntent(
                getPendingIntent(
                        context = context,
                        ocr = ocr,
                        notificationId = notificationId
                )
        )

        notificationBuilder.setDeleteIntent(
                getDeleteIntent(
                        context,
                        ocr,
                        notificationId
                )
        )

        val notification = notificationBuilder.build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notification.color = context.resources.getColor(R.color.buttonColor)
        }

        notificationManager.notify(notificationId.toInt(), notification)

        ApxorUtils.logAppEvent(EventConstants.OCR_FROM_IMAGE_NOTIFICATION_SHOWN)
    }

    private fun getPendingIntent(context: Context, ocr: String, notificationId: Long): PendingIntent {
        val intent = Intent(context, ActionHandlerActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra("data", getData(ocr, notificationId))
        intent.action = NotificationConstants.ACTION_MATCH_PAGE
        return PendingIntent.getActivity(context, notificationId.toInt(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun getData(ocr: String, notificationId: Long): HashMap<String, String> {
        val dataMap: HashMap<String, String> = HashMap()
        dataMap["event"] = Constants.MATCH_OCR_NOTIFICATION
        dataMap["data"] = JsonObject().apply {
            addProperty(Constants.ASK_IMAGE_OCR, ocr)
            addProperty(Constants.NOTIFICATION_ID, notificationId)
        }.toString()
        return dataMap
    }

    private fun getDeleteIntent(context: Context, ocr: String, notificationId: Long): PendingIntent {
        val intent = Intent(context, OnMatchNotificationDismissReceiver::class.java)
        intent.putExtra("data", getData(ocr, notificationId))
        intent.action = NotificationConstants.ACTION_MATCH_PAGE
        return PendingIntent.getBroadcast(context, notificationId.toInt(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT)
    }

    fun dismissNotification(notificationManager: NotificationManager?, notificationId: Long?) {

        if (notificationManager == null || notificationId == null) return

        notificationManager.cancel(notificationId.toInt())
    }

}