package com.doubtnutapp.scheduledquiz.ui

import android.content.Intent
import android.os.Bundle
import androidx.core.content.edit
import com.apxor.androidsdk.core.Attributes
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnutapp.base.ScheduledQuizNotificationClicked
import com.doubtnutapp.base.extension.finishOrKillProcess
import com.doubtnutapp.databinding.ActivityScheduledQuizNotiificationBinding
import com.doubtnutapp.scheduledquiz.di.model.ScheduledQuizNotificationModel
import com.doubtnutapp.scheduledquiz.viewmodel.ScheduledQuizNotificationViewModel
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.quiz.QuizAlertEventManager
import com.doubtnutapp.ui.splash.SplashActivity
import com.doubtnutapp.utils.ApxorUtils
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.utils.Utils
import javax.inject.Inject

class ScheduledQuizNotificationActivity :
    BaseBindingActivity<ScheduledQuizNotificationViewModel, ActivityScheduledQuizNotiificationBinding>(),
    ActionPerformer {

    private lateinit var adapter: ScheduledQuizNotificationAdapter
    private lateinit var scheduledQuizNotificationModelList: ArrayList<ScheduledQuizNotificationModel>
    private lateinit var tracker: Tracker

    @Inject
    lateinit var quizAlertEventManager: QuizAlertEventManager

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    companion object {
        private val TAG = "ScheduledQuizNotificati"

        const val SCHEDULED_QUIZ_TAG = "scheduled_quiz_job"
    }

    private var UNIQUE_LOG_TAG = ""

    override fun provideViewBinding(): ActivityScheduledQuizNotiificationBinding {
        return ActivityScheduledQuizNotiificationBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): ScheduledQuizNotificationViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun getStatusBarColor(): Int {
        return R.color.grey_light
    }

    override fun setupView(savedInstanceState: Bundle?) {
        tracker = (applicationContext as DoubtnutApp).getEventTracker()
        sendEvent(EventConstants.EVENT_NAME_QUIZ_ALERT_VIEW_NEW)
        Utils.sendClassLangEvents(EventConstants.EVENT_NAME_APP_OPEN_DN)
        Utils.sendClassLanguageSpecificEvent(EventConstants.EVENT_NAME_APP_OPEN_DN)

        ApxorUtils.logAppEvent(EventConstants.EVENT_NAME_APP_OPEN_DN, Attributes().apply {
            putAttribute(EventConstants.SOURCE, EventConstants.EVENT_NAME_QUIZ_NEW)
        })

        DoubtnutApp.INSTANCE.getEventTracker()
            .addEventNames(EventConstants.EVENT_NAME_APP_OPEN_DN)
            .addEventParameter(EventConstants.SOURCE, EventConstants.EVENT_NAME_QUIZ_NEW)
            .addStudentId(UserUtil.getStudentId())
            .track()

        analyticsPublisher.publishEvent(
            StructuredEvent(category = EventConstants.EVENT_NAME_APP_OPEN_DN_CATEGORY,
                action = EventConstants.EVENT_NAME_APP_OPEN_DN,
                eventParams = hashMapOf<String, Any>().apply {
                    put(EventConstants.SOURCE, EventConstants.EVENT_NAME_QUIZ_NEW)
                })
        )

//        DoubtnutApp.INSTANCE.analyticsPublisher.get().publishBranchIoEvent(
//            AnalyticsEvent(EventConstants.EVENT_NAME_APP_OPEN_DN, hashMapOf<String, Any>().apply {
//                put(EventConstants.SOURCE, EventConstants.EVENT_NAME_QUIZ_NEW)
//            })
//        )

        binding.outsideView.setOnClickListener {
            finishOrKillProcess()
        }
        init()
        setupRecyclerView()
    }

    private fun init() {
        scheduledQuizNotificationModelList = ArrayList()
    }

    private fun setupRecyclerView() {
        adapter = ScheduledQuizNotificationAdapter(this)
        binding.rvScheduledQuizNotification.adapter = adapter
        displayAndUpdateTopScheduledNotification()
    }

    private fun displayAndUpdateTopScheduledNotification() {
        val responseValue = viewModel.getCurrentNotif()
        if (responseValue != null) {
            scheduledQuizNotificationModelList.add(responseValue)
            adapter.updateList(scheduledQuizNotificationModelList)
            adapter.notifyDataSetChanged()
            quizAlertEventManager.NewQuizAlertShown(
                UserUtil.getStudentId(),
                responseValue.heading.orEmpty()
            )
            //set notification shown
            defaultPrefs().edit {
                putLong(Constants.QUIZ_LAST_SHOWN, System.currentTimeMillis())
            }
        } else {
            val quizIntent = Intent(this, SplashActivity::class.java)
            quizIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            quizIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(quizIntent)
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
            put(EventConstants.EVENT_UDID, udid)
            put(EventConstants.QUIZ_NOTIFICATION_VERSION, "new_quiz")
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
            put(EventConstants.EVENT_UDID, Utils.getUDID())
            put(EventConstants.QUIZ_NOTIFICATION_VERSION, "new_quiz")
            put(EventConstants.TIME_STAMP, timeStamp)
        }

        UNIQUE_LOG_TAG = ""
    }

    private fun sendEvent(@Suppress("SameParameterValue") eventName: String) {
        tracker.addEventNames(eventName)
            .addStudentId(UserUtil.getStudentId())
            .track()
    }

    override fun performAction(action: Any) {
        when (action) {
            is ScheduledQuizNotificationClicked -> {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        action.eventName,
                        action.eventParams
                    )
                )
            }
            else -> {
            }
        }
    }

}