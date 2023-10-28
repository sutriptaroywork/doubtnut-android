package com.doubtnutapp.fallbackquiz.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.forceUnWrap
import com.doubtnutapp.orDefaultValue
import com.doubtnutapp.ui.splash.SplashActivity
import com.doubtnutapp.utils.UserUtil
import javax.inject.Inject

class FallbackActionHandlerActivity : AppCompatActivity() {
    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val deeplink = intent.getStringExtra("deeplink")
        if (deeplink != null) {
            try {
                deeplinkAction.performAction(this, deeplink, Bundle().apply {
                    putBoolean(Constants.CLEAR_TASK, false)
                    putString(Constants.SOURCE, EventConstants.PAGE_DEEPLINK_CLICK)
                })

                val quizTitle: String? = intent.getStringExtra("title")
                analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.QUIZ_NOTIFICATION_CLICKED, hashMapOf(
                        EventConstants.QUIZ_NOTIFICATION_TITLE to quizTitle.orDefaultValue("Motivation for the day"),
                        EventConstants.QUIZ_STUDENT_ID to UserUtil.getStudentId()
                ), ignoreSnowplow = true))
                finish()
            } catch (e: Exception) {
                startActivity(Intent(this, SplashActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            }
        } else {
            startActivity(Intent(this, SplashActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
        }
    }
}