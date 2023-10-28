package com.doubtnutapp.notificationmanager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.ActionHandlerActivity
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.fcm.notification.NotificationConstants
import com.doubtnutapp.utils.BitmapUtils
import com.google.gson.JsonObject
import org.json.JSONObject
import java.util.Timer
import kotlin.concurrent.schedule

object StickyCourseNotificationManager {

    private var notificationManager: NotificationManager? = null
    private var offset: String? = null
    private var isCancelable: Boolean = true
    private var dataObject: JSONObject = JSONObject()
    private var firebaseTag: String? = null
    private var sn_type: String? = null

    fun handleCourseStickyNotification(
        data: Map<String, String>,
        context: Context,
        _notificationManager: NotificationManager?
    ) {
        notificationManager = _notificationManager
        if (notificationManager == null || Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            return
        }

        val channelId = NotificationConstants.CHANNEL_ID_COURSE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(
                channelId, channelId, importance
            )
            notificationManager!!.createNotificationChannel(mChannel)
        }

        dataObject = JSONObject(data["data"])

        firebaseTag =
            if (!data["firebase_eventtag"].isNullOrEmpty() && !data["firebase_eventtag"].equals("null")) {
                data["firebase_eventtag"].orEmpty()
            } else {
                ""
            }
        isCancelable = dataObject.getBoolean("is_vanish")
        offset = dataObject.getString("offset")
        sn_type = data["sn_type"]

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.logo)
            .setColorized(true)
            .setAutoCancel(isCancelable)
            .setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_SOUND)
            .setVibrate(longArrayOf(0L))


        when (sn_type) {
            "image" -> {
                val notificationLayout =
                    RemoteViews(context.packageName, R.layout.notification_course_sticky)
                setupNotificationLayout(data, context, notificationLayout)

                notificationBuilder.setCustomContentView(notificationLayout)
                    .setCustomBigContentView(notificationLayout)
            }
            "banner" -> {

                val notificationLayout =
                    RemoteViews(context.packageName, R.layout.notification_course_sticky)
                setupBannerNotificationLayout(data, context, notificationLayout)

                notificationBuilder.setCustomContentView(notificationLayout)
                    .setCustomBigContentView(notificationLayout)
            }
            else -> {

                val title = data["title"]
                val body = data["message"]
                val imageUrl = dataObject.getString("image_url")

                notificationBuilder.apply {
                    setContentTitle(title)
                    setContentText(body)
                    setLargeIcon(BitmapUtils.getBitmapFromUrl(context, imageUrl))
                    setStyle(NotificationCompat.BigTextStyle().bigText(body))
                    setContentIntent(
                        getPendingIntent(
                            context,
                            NotificationConstants.COURSE_NOTIFICATION_REQUEST_CODE,
                            dataObject.getString("deeplink_banner")
                        )
                    )
                }
            }
        }

        val notification = notificationBuilder.build()
        notification.flags = notification.flags or Notification.FLAG_NO_CLEAR
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notification.color = context.resources.getColor(R.color.buttonColor)
        }

        notificationManager!!.notify(
            NotificationConstants.COURSE_NOTIFICATION_REQUEST_CODE,
            notification
        )
        startTimer()

        DoubtnutApp.INSTANCE.analyticsPublisher.get().publishEvent(
            AnalyticsEvent(
                EventConstants.COURSE_NOTIFICATION_DISPLAY, hashMapOf(
                    "source" to NotificationConstants.COURSE_NOTIFICATION,
                    "id" to dataObject.getString("id"),
                    "sn_type" to sn_type.toString()
                )
            )
        )
    }

    private fun setupNotificationLayout(
        map: Map<String, String>,
        context: Context,
        notificationLayout: RemoteViews
    ) {
        val data = dataObject
        notificationLayout.apply {
            setTextViewText(R.id.tvPrice, data.get("price").toString())
            setTextColor(R.id.tvPrice, Color.parseColor(data.get("price_color").toString()))
            setTextViewText(R.id.tvPriceCrossed, data.get("crossed_price").toString())
            setTextColor(
                R.id.tvPriceCrossed,
                Color.parseColor(data.get("crossed_price_color").toString())
            )
            setTextViewText(R.id.tvSyllabus, data.get("text").toString())
            setTextColor(R.id.tvSyllabus, Color.parseColor(data.get("text_color").toString()))
            setTextColor(R.id.crossView, Color.parseColor(data.get("cross_color").toString()))
            setTextViewText(R.id.btnCourseNotify, data.get("button_cta").toString())
            setTextColor(
                R.id.btnCourseNotify,
                Color.parseColor(data.get("button_text_color").toString())
            )
            setImageViewBitmap(
                R.id.ivBackground,
                BitmapUtils.getBitmapFromUrl(context, data.get("image_url").toString())
            )

            setOnClickPendingIntent(
                R.id.ivBackground,
                getPendingIntent(
                    context,
                    NotificationConstants.BANNER_REQUEST_CODE,
                    data.get("deeplink_banner").toString()
                )
            )
            setOnClickPendingIntent(
                R.id.tvPrice,
                getPendingIntent(
                    context,
                    NotificationConstants.BANNER_REQUEST_CODE,
                    data.get("deeplink_banner").toString()
                )
            )
            setOnClickPendingIntent(
                R.id.tvPriceCrossed,
                getPendingIntent(
                    context,
                    NotificationConstants.BANNER_REQUEST_CODE,
                    data.get("deeplink_banner").toString()
                )
            )
            setOnClickPendingIntent(
                R.id.tvSyllabus,
                getPendingIntent(
                    context,
                    NotificationConstants.BANNER_REQUEST_CODE,
                    data.get("deeplink_banner").toString()
                )
            )
            setOnClickPendingIntent(
                R.id.btnCourseNotify,
                getPendingIntent(
                    context,
                    NotificationConstants.BUTTON_REQUEST_CODE,
                    data.get("deeplink_button").toString(),
                    true
                )
            )
        }
    }

    private fun setupBannerNotificationLayout(
        map: Map<String, String>,
        context: Context,
        notificationLayout: RemoteViews
    ) {

        val data = dataObject

        notificationLayout.apply {
            setViewVisibility(R.id.tvPrice, View.INVISIBLE)
            setViewVisibility(R.id.tvPriceCrossed, View.INVISIBLE)
            setViewVisibility(R.id.tvSyllabus, View.INVISIBLE)
            setViewVisibility(R.id.btnCourseNotify, View.INVISIBLE)
            setImageViewBitmap(
                R.id.ivBackground,
                BitmapUtils.getBitmapFromUrl(context, data.get("image_url").toString())
            )

            setOnClickPendingIntent(
                R.id.ivBackground,
                getPendingIntent(
                    context,
                    NotificationConstants.BANNER_REQUEST_CODE,
                    data.get("deeplink_banner").toString()
                )
            )
        }

    }

    private fun startTimer() {
        val timeLag: Long = if (offset.isNullOrEmpty())
            6000
        else
            offset.toString().toLong()

        Timer().schedule(delay = timeLag) {
            dismissNotification(notificationManager)
        }
    }

    private fun getPendingIntent(
        context: Context,
        requestCode: Int,
        deeplink: String,
        isButton: Boolean = false
    ): PendingIntent {

        val intent = Intent(context, ActionHandlerActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra("data", getData(deeplink, isButton))
        intent.action = NotificationConstants.ACTION_COURSE_NOTIFICATION
        return PendingIntent.getActivity(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun getData(deeplink: String, isButton: Boolean): HashMap<String, String> {

        val dataMap: HashMap<String, String> = HashMap()
        dataMap["event"] = NotificationConstants.CHANNEL_ID_COURSE
        dataMap["is_vanish"] = isCancelable.toString()
        dataMap["source"] = NotificationConstants.COURSE_NOTIFICATION

        if (!firebaseTag.isNullOrBlank()) {
            dataMap["firebase_eventtag"] = firebaseTag.orEmpty()
        }

        dataMap["data"] = JsonObject().apply {
            addProperty("deeplink", deeplink)
            addProperty("id", dataObject.getString("id"))
            addProperty("sn_type", sn_type)
            if (isButton) {
                addProperty(Constants.SOURCE, Constants.COURSE_STICKY_NOTIFICATION_BUTTON)
            }
        }.toString()
        return dataMap
    }

    fun dismissNotification(notificationManager: NotificationManager?) {
        notificationManager?.cancel(NotificationConstants.COURSE_NOTIFICATION_REQUEST_CODE)
    }
}