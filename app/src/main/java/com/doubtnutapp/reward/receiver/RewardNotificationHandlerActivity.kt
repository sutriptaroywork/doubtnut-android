package com.doubtnutapp.reward.receiver

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.fcm.notification.NotificationConstants
import com.doubtnutapp.forceUnWrap
import com.doubtnutapp.ui.splash.SplashActivity
import com.doubtnutapp.utils.UserUtil
import javax.inject.Inject

class RewardNotificationHandlerActivity : AppCompatActivity() {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var userPreference: UserPreference

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val deeplink = intent.getStringExtra("deeplink")
        val eventName = intent.getStringExtra("event_name").orEmpty()
        val eventTitle = intent.getStringExtra("event_title").orEmpty()
        val notificationID = intent.getIntExtra("notification_id", NotificationConstants.NOTIFICATION_ID_REWARD)
        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.cancel(notificationID)
        if (deeplink != null) {
            try {
                deeplinkAction.performAction(this, deeplink, Bundle().apply {
                    putBoolean(Constants.CLEAR_TASK, false)
                    putString(Constants.SOURCE, EventConstants.PAGE_DEEPLINK_CLICK)
                })
                analyticsPublisher.publishEvent(AnalyticsEvent(eventName, hashMapOf(
                        EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                        EventConstants.EVENT_NAME_TITLE to eventTitle,
                        Constants.CURRENT_LEVEL to userPreference.getRewardSystemCurrentLevel(),
                        Constants.CURRENT_DAY to userPreference.getRewardSystemCurrentDay(),
                )))
                finish()
            } catch (e: Exception) {
                startActivity(Intent(this, SplashActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            }

        } else {
            startActivity(Intent(this, SplashActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
        }
    }
}