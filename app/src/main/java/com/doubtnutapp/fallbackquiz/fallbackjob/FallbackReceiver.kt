package com.doubtnutapp.fallbackquiz.fallbackjob

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.format.DateUtils.isToday
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.CoreApplication
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.fallbackquiz.db.FallbackQuizModel
import com.doubtnutapp.fallbackquiz.db.FallbackQuizRepository
import com.doubtnutapp.fallbackquiz.ui.FallbackActionHandlerActivity
import com.doubtnutapp.fallbackquiz.ui.QuizFallbackActivity
import com.doubtnutapp.fcm.notification.NotificationConstants
import com.doubtnutapp.fcm.notification.NotificationConstants.NOTIFICATION_CHANNEL_ID_FALLBACK
import com.doubtnutapp.fcm.notification.NotificationConstants.NOTIFICATION_ID_10PM
import com.doubtnutapp.fcm.notification.NotificationConstants.PENDING_INTENT_DEEPLINK_10PM
import com.doubtnutapp.fcm.notification.NotificationConstants.PENDING_INTENT_REPEAT_ALARM_10PM
import com.doubtnutapp.utils.BitmapUtils
import com.doubtnutapp.utils.UserUtil
import kotlinx.coroutines.runBlocking
import java.util.*
import javax.inject.Inject

class FallbackReceiver : BroadcastReceiver() {

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    @Inject
    lateinit var fallbackQuizRepository: FallbackQuizRepository

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private var fallbackModel: FallbackQuizModel? = null

    private val imageQuestionMap by lazy {
        hashMapOf<String, String>().apply {
            this["104097692"] =
                "https://doubtnut-static.s3.ap-south-1.amazonaws.com/images/2022/03/23/12-37-32-938-PM_fallback_one.webp"
            this["66073146"] =
                "https://doubtnut-static.s3.ap-south-1.amazonaws.com/images/2022/03/23/12-41-27-678-PM_fallback_two.webp"
            this["104097708"] =
                "https://doubtnut-static.s3.ap-south-1.amazonaws.com/images/2022/03/23/12-40-50-146-PM_fallback_three.webp"
            this["104097707"] =
                "https://doubtnut-static.s3.ap-south-1.amazonaws.com/images/2022/03/23/12-42-17-780-PM_fallback_four.webp"
            this["104097706"] =
                "https://doubtnut-static.s3.ap-south-1.amazonaws.com/images/2022/03/23/12-42-48-396-PM_fallback_five.webp"
            this["104097705"] =
                "https://doubtnut-static.s3.ap-south-1.amazonaws.com/images/2022/03/23/12-44-16-957-PM_fallback_six.webp"
            this["104097704"] =
                "https://doubtnut-static.s3.ap-south-1.amazonaws.com/images/2022/03/23/12-44-42-182-PM_fallback_seven.webp"
            this["104097691"] =
                "https://doubtnut-static.s3.ap-south-1.amazonaws.com/images/2022/03/23/12-45-12-080-PM_fallback_eight.webp"
            this["104097702"] =
                "https://doubtnut-static.s3.ap-south-1.amazonaws.com/images/2022/03/23/12-45-32-950-PM_fallback_nine.webp"
            this["104097701"] =
                "https://doubtnut-static.s3.ap-south-1.amazonaws.com/images/2022/03/23/12-45-57-249-PM_fallback_ten.webp"
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val pendingResult = goAsync()
        CoreApplication.INSTANCE.runOnDifferentThread {
            if (intent?.getStringExtra("data") != null) {
                runBlocking {
                    fallbackModel = fallbackQuizRepository.getCurrentFallbackQuiz()
                }
                if (context != null) {
                    fallbackModel?.let {
                        setFallbackPushNotification(context, it)
                    }
                }
            } else {
                setNextAlarm(context!!)
            }
            pendingResult.finish()
        }
    }

    private fun setFallbackPushNotification(
        context: Context,
        fallbackQuizModel: FallbackQuizModel
    ) {
        val currentDay = defaultPrefs().getInt("fallback_day", 0)
        defaultPrefs().edit {
            putInt("fallback_day", (currentDay + 1) % 10)
        }

        if (FeaturesManager.isFeatureEnabled(
                context,
                Features.FALLBACK_PUSH_NOTIFICATION
            ) && FeaturesManager.isFeatureEnabled(context, Features.QUIZ_POPUP)
        ) {
            if (isPopupShownOnDifferentDay()) {
                //Show push notification
                val notificationManager =
                    context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
                val mBuilder =
                    NotificationCompat.Builder(context.applicationContext, default_notification_channel_id)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channelName = NotificationConstants.NOTIFICATION_CHANNEL_NAME_FALLBACK
                    val importance = NotificationManager.IMPORTANCE_HIGH
                    val notificationChannel =
                        NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, importance)
                    mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID)
                    notificationManager.createNotificationChannel(notificationChannel)
                }

                val intent = Intent(context, FallbackActionHandlerActivity::class.java)
                intent.putExtra("deeplink", fallbackQuizModel.deeplink)
                intent.putExtra("title", fallbackQuizModel.heading)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

                val btPendingIntent =
                    PendingIntent.getActivity(context, PENDING_INTENT_DEEPLINK_10PM, intent, 0)

                val fullScreenIntent = Intent(context, QuizFallbackActivity::class.java)
                fullScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                fullScreenIntent.putExtra(QuizFallbackActivity.KEY_IS_FULL_SCREEN_INTENT, true)
                val fullScreenPendingIntent = PendingIntent.getActivity(context, NotificationConstants.PENDING_INTENT_FULL_SCREEN_INTENT, fullScreenIntent, 0)
                context.startActivity(fullScreenIntent)

                val thumbnailBitmap =
                    BitmapUtils.getBitmapFromUrl(
                        context,
                        imageQuestionMap[fallbackQuizModel.questionId]
                    )

                mBuilder
                    .setContentIntent(btPendingIntent)
                    .setFullScreenIntent(fullScreenPendingIntent, true)
                    .setSmallIcon(R.mipmap.logo)
                    .setColorized(true)
                    .setStyle(
                        NotificationCompat.BigPictureStyle()
                            .bigPicture(thumbnailBitmap)
                            .setBigContentTitle(fallbackQuizModel.heading)
                    )
                    .setAutoCancel(true)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mBuilder.color =
                        context.resources.getColor(
                            R.color.buttonColor,
                            context.applicationContext.theme
                        )
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mBuilder.color = context.resources.getColor(R.color.buttonColor)
                }

                mBuilder.setContentTitle(fallbackQuizModel.heading)
                notificationManager.notify(NOTIFICATION_ID, mBuilder.build())
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.QUIZ_NOTIFICATION_SHOWN, hashMapOf(
                            EventConstants.QUIZ_NOTIFICATION_TITLE to fallbackQuizModel.heading.orDefaultValue(
                                "Motivation for the day"
                            ),
                            EventConstants.QUIZ_STUDENT_ID to UserUtil.getStudentId()
                        ), ignoreSnowplow = true
                    )
                )
            } else {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.QUIZ_TO_BE_SHOWN, hashMapOf(
                            EventConstants.QUIZ_NOTIFICATION_TITLE to fallbackQuizModel.heading.orDefaultValue(
                                "Motivation for the day"
                            ),
                            EventConstants.QUIZ_NOTIFICATION_REASON to "popup_shown_same_day",
                            EventConstants.QUIZ_STUDENT_ID to UserUtil.getStudentId()
                        ), ignoreSnowplow = true
                    )
                )
            }
        } else {
            //Send event
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.QUIZ_TO_BE_SHOWN, hashMapOf(
                        EventConstants.QUIZ_NOTIFICATION_TITLE to fallbackQuizModel.heading.orDefaultValue(
                            "Motivation for the day"
                        ),
                        EventConstants.QUIZ_NOTIFICATION_REASON to "feature_disabled",
                        EventConstants.QUIZ_STUDENT_ID to UserUtil.getStudentId()
                    ), ignoreSnowplow = true
                )
            )
        }
        setNextAlarm(context)
    }

    private fun setNextAlarm(context: Context) {
        val intent = Intent(context, FallbackReceiver::class.java)
        intent.extras?.clear()
        intent.putExtra("data", "second")

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            PENDING_INTENT_REPEAT_ALARM_10PM /* Request code */,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager: AlarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val targetTime = Calendar.getInstance()
        targetTime[Calendar.HOUR_OF_DAY] = 21
        targetTime[Calendar.MINUTE] = 45
        targetTime[Calendar.SECOND] = 0
        targetTime[Calendar.MILLISECOND] = 0

        val currentTimeDiff = Calendar.getInstance().timeInMillis - targetTime.timeInMillis
        if (currentTimeDiff > 0) {
            /*
            alarm time already passed
             */
            targetTime.add(Calendar.DATE, 1)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                targetTime.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, targetTime.timeInMillis, pendingIntent)
        }
    }

    private fun isPopupShownOnDifferentDay(): Boolean {
        val lastQuizShownTime = defaultPrefs().getLong(Constants.QUIZ_LAST_SHOWN, 0)
        return !isToday(lastQuizShownTime)
    }

    companion object {
        @Suppress("unused")
        private const val TAG = "FallbackReceiver"

        const val NOTIFICATION_CHANNEL_ID = NOTIFICATION_CHANNEL_ID_FALLBACK
        const val NOTIFICATION_ID = NOTIFICATION_ID_10PM
        private const val default_notification_channel_id = "default"
    }
}
