package com.doubtnutapp.workmanager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.apxor.androidsdk.core.Attributes
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.ActionHandlerActivity
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.fcm.notification.NotificationConstants
import com.doubtnutapp.ui.ask.receiver.OnMatchNotificationDismissReceiver
import com.doubtnutapp.utils.ApxorUtils
import com.doubtnutapp.utils.BitmapUtils
import com.google.gson.JsonObject

/**
 * Created by Sachin Saxena on 2020-04-27.
 */
object MatchQuesNotificationManager {

    fun handleStickyNotification(context: Context, questionId: String, title: String?, message: String?, imageUrl: String, notificationId: Int) {

        val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
                        ?: return

        val channelId = NotificationConstants.MATCH_PAGE_CHANNEL_ID

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = NotificationConstants.MATCH_PAGE_CHANNEL_NAME
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

        if (imageUrl.isNotEmpty()) {
            val bitmap = BitmapUtils.getBitmapFromUrl(context, imageUrl)
            if (bitmap != null)
                notificationBuilder.setStyle(NotificationCompat.BigPictureStyle().bigPicture(bitmap))
        }

        notificationBuilder.setContentIntent(
                getPendingIntent(
                        context,
                        questionId,
                        imageUrl,
                        notificationId
                ))

        notificationBuilder.setDeleteIntent(
                getDeleteIntent(
                        context,
                        questionId,
                        imageUrl,
                        notificationId
                )
        )

        val notification = notificationBuilder.build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notification.color = context.resources.getColor(R.color.buttonColor)
        }

        notificationManager.notify(notificationId, notification)

        ApxorUtils.logAppEvent(EventConstants.MATCH_NOTIFICATION_SHOWN, Attributes().apply {
            putAttribute(Constants.QUESTION_ID, questionId)
        })
    }

    private fun getPendingIntent(context: Context, questionId: String, imageUrl: String, notificationId: Int): PendingIntent {
        val intent = Intent(context, ActionHandlerActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra("data", getData(questionId, imageUrl))
        intent.action = NotificationConstants.ACTION_MATCH_PAGE
        return PendingIntent.getActivity(context, notificationId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun getDeleteIntent(context: Context, questionId: String, imageUrl: String, notificationId: Int): PendingIntent {
        val intent = Intent(context, OnMatchNotificationDismissReceiver::class.java)
        intent.putExtra("data", getData(questionId, imageUrl))
        intent.action = NotificationConstants.ACTION_MATCH_PAGE
        return PendingIntent.getBroadcast(context, notificationId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun getData(questionId: String, imageUrl: String): HashMap<String, String> {
        val dataMap: HashMap<String, String> = HashMap()
        dataMap["event"] = Constants.MATCH_NOTIFICATION
        dataMap["data"] = JsonObject().apply {
            addProperty(Constants.QUESTION_ID, questionId)
            addProperty(Constants.ASK_QUE_URI, imageUrl)
        }.toString()
        return dataMap
    }

    fun dismissNotification(notificationManager: NotificationManager?, notificationId: Int?) {

        if (notificationManager == null || notificationId == null) return

        notificationManager.cancel(notificationId)
    }
}