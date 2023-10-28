package com.doubtnutapp.reward.receiver

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.remote.models.reward.RewardNotificationData
import com.doubtnutapp.fcm.notification.NotificationConstants
import com.doubtnutapp.utils.UserUtil
import javax.inject.Inject

class LoginRewardReceiver : BroadcastReceiver() {

    companion object {
        private const val NOTIFICATION_CHANNEL_ID =
            NotificationConstants.NOTIFICATION_CHANNEL_ID_REWARD
        private const val NOTIFICATION_ID = NotificationConstants.NOTIFICATION_ID_REWARD
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var userPreference: UserPreference

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    private var notificationDataListEnglish = listOf(
        RewardNotificationData(
            "Doubtnut par Login karne ka dhanywaad!",
            "Roz lagayein attendance or jeetein Prize!"
        ),
        RewardNotificationData(
            "Doubtnut par aapka swagat hai!",
            "Har roz lagayein haziri or banein winner!"
        )
    )
    private var notificationDataListHindi = listOf(
        RewardNotificationData(
            "डाउटनट पर लॉगइन करने का धन्यवाद!",
            "रोज़ लगाएं हाजिरी और जीतें इनाम!"
        ),
        RewardNotificationData("डाउटनट पर आपका स्वागत है!", "हर रोज़ हाजिरी लगाएं और बनें विजेता!")
    )

    override fun onReceive(context: Context?, intent: Intent?) {
        val userLanguage = defaultPrefs().getString(Constants.STUDENT_LANGUAGE_CODE, "en") ?: "en"
        if (userPreference.getUserLoggedIn()) {
            if (userLanguage == "hi") {
                createNotification(context, notificationDataListHindi.random())
            } else {
                createNotification(context, notificationDataListEnglish.random())
            }
        }

    }

    private fun createNotification(context: Context?, notificationData: RewardNotificationData) {
        val title = notificationData.notificationHeading
        val description = notificationData.notificationDescription

        val intent = Intent(context, RewardNotificationHandlerActivity::class.java)
        intent.putExtra("deeplink", "doubtnutapp://rewards")
        intent.putExtra("event_name", EventConstants.REWARD_NOTIFICATION_CLICKED)
        intent.putExtra("event_title", title)
        intent.putExtra("notification_id", NOTIFICATION_ID)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val btPendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        val notificationLayout =
            RemoteViews(context?.packageName, R.layout.layout_reward_notification)
        val notificationLayoutSmall =
            RemoteViews(context?.packageName, R.layout.layout_reward_notification_small)

        notificationLayout.setTextViewText(R.id.rewardNotificationTitle, title)
        notificationLayout.setTextViewText(R.id.rewardNotificationDescription, description)
        notificationLayout.setOnClickPendingIntent(
            R.id.llHeadingRewardNotification,
            btPendingIntent
        )

        notificationLayoutSmall.setTextViewText(R.id.rewardNotificationSmallTitle, title)
        notificationLayout.setOnClickPendingIntent(
            R.id.llHeadingRewardNotificationSmall,
            btPendingIntent
        )

        val notificationManager =
            context?.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
        val notificationBuilder = NotificationCompat.Builder(
            context.applicationContext,
            NotificationConstants.DEFAULT_NOTIFICATION_CHANNEL_ID
        )
            .setSmallIcon(R.mipmap.logo)
            .setColorized(true)
            .setAutoCancel(true)
            .setStyle(
                NotificationCompat.BigPictureStyle()
                    .setBigContentTitle(title)
            )
            .setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_SOUND)
            .setVibrate(null)
            .setContentIntent(btPendingIntent)
            .setCustomContentView(notificationLayoutSmall)
            .setCustomBigContentView(notificationLayout)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = NotificationConstants.NOTIFICATION_CHANNEL_NAME_REWARD
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel =
                NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, importance)
            notificationBuilder.setChannelId(NOTIFICATION_CHANNEL_ID)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationBuilder.color =
                context.resources.getColor(R.color.buttonColor, context.applicationContext.theme)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.color = context.resources.getColor(R.color.buttonColor)
        }
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.ATTENDANCE_REMINDER_SENT, hashMapOf(
                    EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                    EventConstants.EVENT_NAME_TITLE to notificationData.notificationHeading.toString(),
                    Constants.CURRENT_LEVEL to userPreference.getRewardSystemCurrentLevel(),
                    Constants.CURRENT_DAY to userPreference.getRewardSystemCurrentDay(),
                ), ignoreSnowplow = true
            )
        )
    }

}