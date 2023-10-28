package com.doubtnutapp.ui.quiz

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import com.apxor.androidsdk.core.Attributes
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.RemoteConfigUtils
import com.doubtnutapp.addEventNames
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnutapp.base.extension.finishOrKillProcess
import com.doubtnutapp.databinding.ActivityQuizTransparentScreenBinding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.test.QuizActivity
import com.doubtnutapp.utils.ApxorUtils
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.doubtnutapp.utils.Utils
import javax.inject.Inject

/**
 * Created by akshaynandwana on
 * 28, November, 2018
 **/
class QuizNotificationActivity :
    BaseBindingActivity<DummyViewModel, ActivityQuizTransparentScreenBinding>() {

    private lateinit var countDownTimer: CountDownTimer
    private var countDownTimerBool: Boolean = false
    private lateinit var tracker: Tracker

    @Inject
    lateinit var quizAlertEventManager: QuizAlertEventManager

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private var UNIQUE_LOG_TAG = ""

    var value: Int? = 2

    override fun onDestroy() {
        super.onDestroy()
        if (countDownTimerBool) {
            countDownTimer.cancel()
        }
        value = 3
    }

    override fun provideViewBinding(): ActivityQuizTransparentScreenBinding {
        return ActivityQuizTransparentScreenBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): DummyViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun getStatusBarColor(): Int {
        return R.color.grey_light
    }

    override fun setupView(savedInstanceState: Bundle?) {
        Utils.sendClassLangEvents(EventConstants.EVENT_NAME_APP_OPEN_DN)
        Utils.sendClassLanguageSpecificEvent(EventConstants.EVENT_NAME_APP_OPEN_DN)

        setUpTimer()
        setUpListener()
        tracker = (applicationContext as DoubtnutApp).getEventTracker()
        sendEvent(EventConstants.EVENT_NAME_QUIZ_ALERT_VIEW)
        quizAlertEventManager.QuizAlertShown(getStudentId())
        ApxorUtils.logAppEvent(EventConstants.EVENT_NAME_APP_OPEN_DN, Attributes().apply {
            putAttribute(EventConstants.SOURCE, EventConstants.EVENT_NAME_QUIZ)
        })

        DoubtnutApp.INSTANCE.getEventTracker()
            .addEventNames(EventConstants.EVENT_NAME_APP_OPEN_DN)
            .addEventParameter(EventConstants.SOURCE, EventConstants.EVENT_NAME_QUIZ)
            .addStudentId(getStudentId())
            .track()

//        DoubtnutApp.INSTANCE.analyticsPublisher.get().publishBranchIoEvent(
//            AnalyticsEvent(EventConstants.EVENT_NAME_APP_OPEN_DN, hashMapOf<String, Any>().apply {
//                put(EventConstants.SOURCE, EventConstants.EVENT_NAME_QUIZ)
//            })
//        )

        // Send to snowplow
        analyticsPublisher.publishEvent(
            StructuredEvent(category = EventConstants.EVENT_NAME_APP_OPEN_DN_CATEGORY,
                action = EventConstants.EVENT_NAME_APP_OPEN_DN,
                eventParams = hashMapOf<String, Any>().apply {
                    put(EventConstants.SOURCE, EventConstants.EVENT_NAME_QUIZ)
                })
        )

        val countToSendEvent: Int =
            Utils.getCountToSend(RemoteConfigUtils.getEventInfo(), EventConstants.SESSION_START)
        repeat((0 until countToSendEvent).count()) {
            sendEvent(EventConstants.SESSION_START)
        }

    }

    override fun onStart() {
        super.onStart()
        val udid = Utils.getUDID()
        UNIQUE_LOG_TAG = "id_" + udid + "_" + System.currentTimeMillis().toString()
        val timeStamp = System.currentTimeMillis()
        val params = hashMapOf<String, Any>().apply {
            put(EventConstants.STATUS, EventConstants.QUIZ_ACTIVITY_ENTERED_FOREGROUND)
            put(EventConstants.EVENT_NAME_ID, UNIQUE_LOG_TAG)
            put(EventConstants.QUIZ_NOTIFICATION_VERSION, "old_quiz")
            put(EventConstants.EVENT_UDID, udid)
            put(EventConstants.TIME_STAMP, timeStamp)
        }
        analyticsPublisher.publishEvent(
            StructuredEvent(
                EventConstants.QUIZ_ACTIVITY_STATE,
                EventConstants.QUIZ_ACTIVITY_ENTERED_FOREGROUND_ACTION,
                eventParams = params
            )
        )
    }

    override fun onStop() {
        super.onStop()
        val timeStamp = System.currentTimeMillis()
        val params = hashMapOf<String, Any>().apply {
            put(EventConstants.STATUS, EventConstants.QUIZ_ACTIVITY_ENTERED_BACKGROUND)
            put(EventConstants.EVENT_NAME_ID, UNIQUE_LOG_TAG)
            put(EventConstants.QUIZ_NOTIFICATION_VERSION, "old_quiz")
            put(EventConstants.EVENT_UDID, Utils.getUDID())
            put(EventConstants.TIME_STAMP, timeStamp)
        }

        UNIQUE_LOG_TAG = ""
    }

    private fun sendEvent(eventName: String) {
        tracker.addEventNames(eventName)
            .addStudentId(getStudentId())
            .track()
    }

    private fun setUpListener() {
        binding.rootView.setOnClickListener {
            finishOrKillProcess()
        }
        binding.parentOldQuiz.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.CLICK_QUIZ_EVENT_V2, hashMapOf(
                        EventConstants.QUIZ_CLICKED_TYPE to "old",
                        EventConstants.QUIZ_NOTIFICATION_TITLE to "Play quiz",
                        EventConstants.STUDENT_ID to getStudentId()
                    ), ignoreSnowplow = true
                )
            )
        }
        binding.tvSkipQuizNotification.setOnClickListener {
            sendEvent(EventConstants.EVENT_NAME_QUIZ_ALERT_SKIP)
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.CLICK_QUIZ_NOTIFICATION, hashMapOf(
                        EventConstants.QUIZ_NOTIFICATION_TITLE to "old_quiz",
                        EventConstants.CLICK_QUIZ_TYPE to "skip"
                    ), ignoreSnowplow = true
                )
            )
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.CLICK_QUIZ_EVENT_V2, hashMapOf(
                        EventConstants.QUIZ_CLICKED_TYPE to "old",
                        EventConstants.QUIZ_NOTIFICATION_TITLE to "Play quiz",
                        EventConstants.STUDENT_ID to getStudentId()
                    ), ignoreSnowplow = true
                )
            )
            finishOrKillProcess()
        }
        binding.startQuiz.setOnClickListener {
            sendEvent(EventConstants.EVENT_NAME_QUIZ_ALERT_GOTO)
            val quizIntent = Intent(this, QuizActivity::class.java)
            startActivity(quizIntent)
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.CLICK_QUIZ_NOTIFICATION, hashMapOf(
                        EventConstants.QUIZ_NOTIFICATION_TITLE to "old_quiz",
                        EventConstants.CLICK_QUIZ_TYPE to "cta"
                    ), ignoreSnowplow = true
                )
            )
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.CLICK_QUIZ_EVENT_V2, hashMapOf(
                        EventConstants.QUIZ_CLICKED_TYPE to "old",
                        EventConstants.QUIZ_NOTIFICATION_TITLE to "Play quiz",
                        EventConstants.STUDENT_ID to getStudentId()
                    ), ignoreSnowplow = true
                )
            )
            finishOrKillProcess()
        }
    }

    private fun setUpTimer() {
        countDownTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                countDownTimerBool = true
                val hours = ((millisUntilFinished / (1000 * 60 * 60)) % 24)
                val minutes = ((millisUntilFinished / (1000 * 60)) % 60)
                val seconds = (millisUntilFinished / 1000) % 60
                binding.tvQuizStartOver.text =
                    getString(R.string.string_quiz_question_timer, hours, minutes, seconds)
            }

            override fun onFinish() {
                binding.tvQuizStartOver.text = getString(R.string.string_quiz_time_over)
            }
        }
        countDownTimer.start()
    }

    companion object {
        const val TAG = "QuizNotificationActivity"
    }

}