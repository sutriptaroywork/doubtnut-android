package com.doubtnutapp.notificationmanager

/**
 * Created by Akshat Jindal on 01/06/22.
 */

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.TypedValue
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.doubtnutapp.R
import com.doubtnutapp.fcm.notification.NotificationConstants
import com.doubtnutapp.utils.BitmapUtils
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

interface Timer {
    fun play(context: Context, timeMillis: Long)
    fun terminate(context: Context)
}

typealias onFinishListener = () -> Unit

object NotificationTimer : Timer {

    private var notificationPriority = NotificationCompat.PRIORITY_LOW
    private var contentPendingIntent: PendingIntent? = null
    private var finishListener: onFinishListener? = null

    private val channelId: String by lazy { NotificationConstants.CHANNEL_ID_GENERIC_STICKY_TIMER }

    @SuppressLint("StaticFieldLeak")
    private lateinit var notificationManager: NotificationManager

    private const val TAG = "generic_sticky_timer"

    private var setStartTime by Delegates.notNull<Long>()

    private var data: HashMap<String, String> = hashMapOf()

    const val notificationId: Int = NotificationConstants.GENERIC_STICKY_TIMER_BANNER_REQUEST_CODE

    override fun play(context: Context, timeMillis: Long) {
        if (TimerService.state == TimerState.RUNNING) return

        val playIntent = Intent(context, TimerService::class.java).apply {
            action = "PLAY"
            putExtra("setTime", timeMillis)
            putExtra("forReplay", TimerService.state == TimerState.PAUSED)
        }
        ContextCompat.startForegroundService(context, playIntent)
    }

    override fun terminate(context: Context) {
        if (!::notificationManager.isInitialized && TimerService.state == TimerState.TERMINATED) return

        val terminateIntent = Intent(context, TimerService::class.java).apply {
            action = "TERMINATE"
        }
        ContextCompat.startForegroundService(context, terminateIntent)
    }

    fun createNotification(context: Context, setTime: Long): Notification {
        this.setStartTime = setTime

        return playStateNotification(context, getTimeInHHMMSS(context, setTime))
    }

    fun updateTimeLeft(context: Context, timeLeft: String) =
        notificationManager.notify(notificationId, playStateNotification(context, timeLeft))

    fun removeNotification() = notificationManager.cancelAll()

    private fun getTimeInHHMMSS(context: Context, millisUntilFinished: Long) = context.getString(
        R.string.string_quiz_question_timer,
        TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
        (TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) -
                TimeUnit.HOURS.toMinutes(
                    TimeUnit.MILLISECONDS.toHours(
                        millisUntilFinished
                    )
                )),
        (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                TimeUnit.MINUTES.toSeconds(
                    TimeUnit.MILLISECONDS.toMinutes(
                        millisUntilFinished
                    )
                ))
    )

    private fun baseNotificationBuilder(
        context: Context,
        timeLeft: String
    ): NotificationCompat.Builder {
        val notificationLayout =
            RemoteViews(context.packageName, R.layout.notification_generic_timer_sticky).also {

                //banner iv
                it.setImageViewBitmap(
                    R.id.ivBackground,
                    BitmapUtils.getBitmapFromUrl(context, data["image_url"])
                )

                //background
                it.setInt(
                    R.id.timerNotifyLayout,
                    "setBackgroundColor",
                    Color.parseColor(data["background_color"] ?: "#eb532c")
                )

                //title tv
                it.setTextViewText(R.id.tvNotificationTitle, data["text"])
                it.setTextColor(
                    R.id.tvNotificationTitle,
                    Color.parseColor(data["text_color"] ?: "#FFFFFF")
                )
                it.setTextViewTextSize(
                    R.id.tvNotificationTitle,
                    TypedValue.COMPLEX_UNIT_SP,
                    (data["text_size"] ?: "13").toFloat()
                )

                //timer tv
                it.setTextViewText(R.id.tvNotificationTimer, timeLeft)
                it.setTextColor(
                    R.id.tvNotificationTimer,
                    Color.parseColor(data["timer_text_color"] ?: "#FFFFFF")
                )
                it.setTextViewTextSize(
                    R.id.tvNotificationTimer,
                    TypedValue.COMPLEX_UNIT_SP,
                    (data["timer_text_size"] ?: "20").toFloat()
                )

                //button tv
                it.setInt(
                    R.id.tvRegisterNow,
                    "setBackgroundColor",
                    Color.parseColor(data["cta_background_color"] ?: "#eb532c")
                )
                it.setTextViewText(R.id.tvRegisterNow, data["cta"])
                it.setTextColor(
                    R.id.tvRegisterNow,
                    Color.parseColor(data["cta_text_color"] ?: "#FFFFFF")
                )
                it.setTextViewTextSize(
                    R.id.tvRegisterNow,
                    TypedValue.COMPLEX_UNIT_SP,
                    (data["cta_text_size"] ?: "15").toFloat()
                )

            }

        return NotificationCompat.Builder(context, channelId).apply {
            setSmallIcon(R.mipmap.logo)
            color = context.resources.getColor(R.color.buttonColor)
            priority = notificationPriority
            setAutoCancel(true)
            setOnlyAlertOnce(true)
            setCustomContentView(notificationLayout)
            setCustomBigContentView(notificationLayout)
            contentPendingIntent?.let { setContentIntent(it) }
        }
    }

    private fun playStateNotification(context: Context, timeLeft: String): Notification =
        baseNotificationBuilder(context, timeLeft).build().apply {
            flags = this.flags or Notification.FLAG_NO_CLEAR
        }

    class Builder(private val context: Context) {

        fun setData(hashMap: HashMap<String, String>): Builder {
            data = hashMap
            return this
        }

        fun setContentIntent(intent: PendingIntent): Builder {
            contentPendingIntent = intent
            return this
        }

        fun setOnFinishListener(listener: onFinishListener): Builder {
            finishListener = listener
            return this
        }

        fun setNotificationManager(manager: NotificationManager): Builder {
            notificationManager = manager
            return this
        }

        fun play(timeMillis: Long) = play(context, timeMillis)

        fun terminate() = terminate(context)
    }
}