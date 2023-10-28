package com.doubtnutapp.fcm.notification.video

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.doubtnut.analytics.Tracker
import com.doubtnutapp.ActionHandlerActivity
import com.doubtnutapp.Constants
import com.doubtnutapp.fcm.notification.NotificationConstants
import com.doubtnutapp.fcm.notification.PushNotificationChannel
import com.doubtnutapp.fcm.notification.PushNotificationItem
import com.doubtnutapp.fcm.notification.VideoNotificationChannel
import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**
 * This class hold all the item required for NotificationBuilder to build the notification
 *
 * @param context application context
 * @param title small title of the notification
 * @param message long description of the notificaiton
 * @param smallIcon resource id of the drawable to be shown in the notification, preferably use for
 *                  small for the default notification that Android provides by default, its not a
 *                  compulsion.
 *@param imageUrl url send in the data field of the FCM notification of type data, preferably used in
 *                    custom layout of the notification.
 * */
data class VideoNotificationItem constructor(
        val context: Context,
        val data: Map<String, String>,
        var title: String = "",
        var message: String = "",
        var imageUrl: String = "",
        var tag: String = ""
) : PushNotificationItem {
    lateinit var eventTracker: Tracker

    override fun data() = data

    override fun channel(): PushNotificationChannel {
        return VideoNotificationChannel()
    }


    override fun title(): String {
        return title
    }

    override fun message(): String {
        return message
    }

    override fun imageUrl(): String {
        return imageUrl
    }

    override fun pendingIntent(): PendingIntent {

        val dataMap: HashMap<String, String> = HashMap(data)
        var intent = Intent(context, ActionHandlerActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra("data", dataMap)

        return PendingIntent.getActivity(context, NotificationConstants.VIDEO_NOTIFICATION_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT)

    }

    private fun getPageAndQId(): Triple<String, String, String> {
        var page = ""
        var questionId = ""
        var resourceType = ""

        try {
            val data = JSONObject(data[Constants.NOTIFICATION_DATA])
            page = if (data.has(Constants.NOTIFICATION_PAGE)) data.getString(Constants.NOTIFICATION_PAGE) else ""
            questionId = if (data.has(Constants.NOTIFICATION_QID)) data.getString(Constants.NOTIFICATION_QID) else ""
            resourceType = if (data.has(Constants.NOTIFICATION_RESOURCE_TYPE)) data.getString(Constants.NOTIFICATION_RESOURCE_TYPE) else ""

        } catch (ex: JSONException) {
            ex.printStackTrace()
        }

        return Triple(page, questionId, resourceType)
    }

}