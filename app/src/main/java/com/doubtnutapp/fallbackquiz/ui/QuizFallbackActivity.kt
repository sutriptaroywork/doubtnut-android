package com.doubtnutapp.fallbackquiz.ui

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.isVisible
import com.apxor.androidsdk.core.Attributes
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnut.core.constant.CoreEventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnutapp.base.extension.finishOrKillProcess
import com.doubtnutapp.databinding.ActivityQuizFallbackBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.fallbackquiz.db.FallbackQuizModel
import com.doubtnutapp.fallbackquiz.viewmodel.FallbackQuizViewModel
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.quiz.QuizAlertEventManager
import com.doubtnutapp.utils.ApxorUtils
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.utils.Utils
import kotlinx.android.synthetic.main.activity_quiz_fallback.*
import javax.inject.Inject

class QuizFallbackActivity :
    BaseBindingActivity<FallbackQuizViewModel, ActivityQuizFallbackBinding>() {

    private lateinit var tracker: Tracker

    @Inject
    lateinit var quizAlertEventManager: QuizAlertEventManager

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var model: FallbackQuizModel? = null
    var imageQuestionMap = hashMapOf<String, String>()
    var eventDayMap = hashMapOf<String, String>()

    private var uniqueLogTag = ""

    private val isFullScreenIntent by lazy {
        intent?.getBooleanExtra(KEY_IS_FULL_SCREEN_INTENT, false) ?: false
    }

    override fun provideViewBinding(): ActivityQuizFallbackBinding {
        return ActivityQuizFallbackBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): FallbackQuizViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun getStatusBarColor(): Int {
        return R.color.grey_light
    }

    override fun setupView(savedInstanceState: Bundle?) {
        init()
        setUpView()
        setUpListener()
        tracker = (applicationContext as DoubtnutApp).getEventTracker()
        sendEvent(EventConstants.EVENT_NAME_QUIZ_ALERT_VIEW_NEW)
        if (model != null) {
            quizAlertEventManager.NewQuizAlertShown(
                UserUtil.getStudentId(),
                eventDayMap[model?.questionId].orEmpty()
            )
        }

        Utils.sendClassLangEvents(EventConstants.EVENT_NAME_APP_OPEN_DN)
        Utils.sendClassLanguageSpecificEvent(EventConstants.EVENT_NAME_APP_OPEN_DN)

        DoubtnutApp.INSTANCE.getEventTracker()
            .addEventNames(EventConstants.EVENT_NAME_APP_OPEN_DN)
            .addEventParameter(EventConstants.SOURCE, EventConstants.EVENT_NAME_QUIZ)
            .addStudentId(UserUtil.getStudentId())
            .track()

//        DoubtnutApp.INSTANCE.analyticsPublisher.get().publishBranchIoEvent(
//            AnalyticsEvent(EventConstants.EVENT_NAME_APP_OPEN_DN, hashMapOf<String, Any>().apply {
//                put(EventConstants.SOURCE, EventConstants.EVENT_NAME_QUIZ)
//            })
//        )

        ApxorUtils.logAppEvent(EventConstants.EVENT_NAME_APP_OPEN_DN, Attributes().apply {
            putAttribute(EventConstants.SOURCE, EventConstants.EVENT_NAME_QUIZ)
            putAttribute(CoreEventConstants.IS_FULL_SCREEN_INTENT, isFullScreenIntent)
        })

        analyticsPublisher.publishEvent(
            StructuredEvent(category = EventConstants.EVENT_NAME_APP_OPEN_DN_CATEGORY,
                action = EventConstants.EVENT_NAME_APP_OPEN_DN,
                eventParams = hashMapOf<String, Any>().apply {
                    put(EventConstants.SOURCE, EventConstants.EVENT_NAME_QUIZ)
                    put(CoreEventConstants.IS_FULL_SCREEN_INTENT, isFullScreenIntent)
                })
        )
    }

    private fun setUpView() {
        try {
            model = viewModel.getCurrentFallbackQuiz()
            headingIconQuizFallback.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.placeholder_quiz_header
                )
            )
            if (model != null && model?.btnText != null && model?.btnText!!.isNotEmpty()) {
                btnQuizFallback.text = model?.btnText
                headingQuizFallback.text = model?.heading.orEmpty()
                skipQuizFallback.isVisible = (model?.isSkippable == true)
                headingIconQuizFallback.setImageDrawable(getDrawable(R.drawable.placeholder_quiz_header))
                try {
                    thumbnailQuizFallback.loadImage(imageQuestionMap[model?.questionId])
                } catch (e: Exception) {
                }
                defaultPrefs().edit {
                    putLong(Constants.QUIZ_LAST_SHOWN, System.currentTimeMillis())
                }

                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.CLICK_QUIZ_EVENT_V2, hashMapOf(
                            EventConstants.QUIZ_CLICKED_TYPE to "fallback_quiz",
                            EventConstants.QUIZ_NOTIFICATION_TITLE to eventDayMap[model?.questionId].orEmpty(),
                            EventConstants.STUDENT_ID to UserUtil.getStudentId()
                        ), ignoreSnowplow = true
                    )
                )
            } else {
                cvPopup.isVisible = false
                finishOrKillProcess()
            }

        } catch (ignored: Exception) {
            // https://console.firebase.google.com/project/doubtnut-e000a/crashlytics/app/android:com.doubtnutapp/issues/90e09845d8b9caf22db99a391e0df801?time=last-twenty-four-hours&sessionEventKey=60B05BBD028600012F0794A8417FF0F4_1545615115635210627
            // https://console.firebase.google.com/project/doubtnut-e000a/crashlytics/app/android:com.doubtnutapp/issues/88901d8b750b1dfd89dd02c4af727b0f?time=last-twenty-four-hours&types=crash&sessionEventKey=61DE6965030D000119F2083A366AFEB8_1630635644186050521
        }
    }

    fun init() {
        imageQuestionMap = viewModel.imageQuestionMap
        eventDayMap = viewModel.eventDayMap
    }

    override fun onStart() {
        super.onStart()
        val udid = Utils.getUDID()
        uniqueLogTag = "id_" + udid + "_" + System.currentTimeMillis().toString()
        val timeStamp = System.currentTimeMillis()
        val params = hashMapOf<String, Any>().apply {
            put(EventConstants.STATUS, EventConstants.QUIZ_ACTIVITY_ENTERED_FOREGROUND)
            put(EventConstants.EVENT_NAME_ID, uniqueLogTag)
            put(EventConstants.QUIZ_NOTIFICATION_VERSION, "fallback_quiz")
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
            put(EventConstants.EVENT_NAME_ID, uniqueLogTag)
            put(EventConstants.QUIZ_NOTIFICATION_VERSION, "fallback_quiz")
            put(EventConstants.EVENT_UDID, Utils.getUDID())
            put(EventConstants.TIME_STAMP, timeStamp)
        }

        uniqueLogTag = ""
    }

    private fun sendEvent(eventName: String) {
        tracker.addEventNames(eventName)
            .addStudentId(UserUtil.getStudentId())
            .track()
    }

    private fun setUpListener() {
        rootViewFallback.setOnClickListener { finishOrKillProcess() }
        skipQuizFallback.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.CLICK_QUIZ_NOTIFICATION, hashMapOf(
                        EventConstants.QUIZ_NOTIFICATION_TITLE to eventDayMap[model?.questionId].orEmpty(),
                        EventConstants.CLICK_QUIZ_TYPE to "skip"
                    ), ignoreSnowplow = true
                )
            )
            finishOrKillProcess()
        }
        btnQuizFallback.setOnClickListener {
            deeplinkAction.performAction(this, model?.deeplink.orEmpty(), Bundle().apply {
                putBoolean(Constants.CLEAR_TASK, false)
                putString(Constants.SOURCE, EventConstants.PAGE_DEEPLINK_CLICK)
            })
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.CLICK_QUIZ_NOTIFICATION, hashMapOf(
                        EventConstants.QUIZ_NOTIFICATION_TITLE to eventDayMap[model?.questionId].orEmpty(),
                        EventConstants.CLICK_QUIZ_TYPE to "cta"
                    ), ignoreSnowplow = true
                )
            )
            finishOrKillProcess()
        }
        thumbnailQuizFallback.setOnClickListener {
            deeplinkAction.performAction(this, model?.deeplink.orEmpty(), Bundle().apply {
                putBoolean(Constants.CLEAR_TASK, false)
                putString(Constants.SOURCE, EventConstants.PAGE_DEEPLINK_CLICK)
            })
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.CLICK_QUIZ_NOTIFICATION, hashMapOf(
                        EventConstants.QUIZ_NOTIFICATION_TITLE to eventDayMap[model?.questionId].orEmpty(),
                        EventConstants.CLICK_QUIZ_TYPE to "cta"
                    ), ignoreSnowplow = true
                )
            )
            finishOrKillProcess()
        }
    }

    companion object {
        const val TAG = "QuizFallbackActivity"

        const val KEY_IS_FULL_SCREEN_INTENT = "is_full_screen_intent"
    }

}

