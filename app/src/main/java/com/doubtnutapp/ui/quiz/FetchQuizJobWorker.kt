package com.doubtnutapp.ui.quiz

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import androidx.work.*
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.camera.ui.CameraActivity
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.fallbackquiz.ui.QuizFallbackActivity
import com.doubtnutapp.liveclass.ui.LiveClassActivity
import com.doubtnutapp.liveclass.ui.LiveClassChatActivity
import com.doubtnutapp.liveclassreminder.LiveClassReminderActivity
import com.doubtnutapp.matchquestion.ui.activity.MatchQuestionActivity
import com.doubtnutapp.scheduledquiz.db.repository.ScheduledQuizNotificationRepository
import com.doubtnutapp.scheduledquiz.ui.ScheduledQuizNotificationActivity
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.videoPage.ui.VideoPageActivity
import com.theartofdev.edmodo.cropper.CropImageActivity
import kotlinx.coroutines.runBlocking
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * This is similar to FetchQuizJob and called in its replacement
 * when EVERNOTE_ANDROID_JOB feature is disabled. Currently, enabled for all users.
 */
class FetchQuizJobWorker(context: Context, workerParams: WorkerParameters) : Worker(
    context,
    workerParams
) {

    @Inject
    lateinit var userPreference: UserPreference

    @Inject
    lateinit var scheduledQuizNotificationRepository: ScheduledQuizNotificationRepository

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    private var reasonForQuizPopup = ""

    override fun doWork(): Result {

        if (!userPreference.getUserLoggedIn()) {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.QUIZ_NOTIFICATION_TYPE, hashMapOf(
                        EventConstants.QUIZ_NOTIFICATION_TITLE to "No_quiz_shown_logout",
                        EventConstants.QUIZ_NOTIFICATION_STATUS to "TRYING",
                        EventConstants.QUIZ_NOTIFICATION_REASON to "No_quiz_shown_login_" + userPreference.getUserLoggedIn(),
                        EventConstants.QUIZ_STUDENT_ID to UserUtil.getStudentId(),
                        EventConstants.FEATURE to TAG
                    ), ignoreSnowplow = true
                )
            )
            enqueue(applicationContext)
            return Result.success()
        }
        if (isPopupNotAllowed()) {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.QUIZ_NOTIFICATION_TYPE, hashMapOf(
                        EventConstants.QUIZ_NOTIFICATION_TITLE to "No_quiz_shown_duetovideopage",
                        EventConstants.QUIZ_NOTIFICATION_STATUS to "TRYING",
                        EventConstants.QUIZ_NOTIFICATION_REASON to "No_quiz_shown_duetovideopage",
                        EventConstants.QUIZ_STUDENT_ID to UserUtil.getStudentId(),
                        EventConstants.FEATURE to TAG
                    ), ignoreSnowplow = true
                )
            )
            enqueue(applicationContext)
            return Result.success()
        }

        if (FeaturesManager.isFeatureEnabled(DoubtnutApp.INSTANCE, Features.QUIZ_POPUP)) {
            if (isLocalDataEmpty()) {
                return launchFallbackActivity()
            } else {
                reasonForQuizPopup = "filled_local_DB"
                try {
                    val quizIntent =
                        Intent(applicationContext, ScheduledQuizNotificationActivity::class.java)
                    quizIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    quizIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                    applicationContext.startActivity(quizIntent)

                    //Add event on trying new quiz
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.QUIZ_NOTIFICATION_TYPE, hashMapOf(
                                EventConstants.QUIZ_NOTIFICATION_TITLE to "new_quiz",
                                EventConstants.QUIZ_NOTIFICATION_STATUS to "TRYING",
                                EventConstants.QUIZ_NOTIFICATION_REASON to reasonForQuizPopup,
                                EventConstants.QUIZ_STUDENT_ID to UserUtil.getStudentId(),
                                EventConstants.FEATURE to TAG
                            ), ignoreSnowplow = true
                        )
                    )
                    enqueue(applicationContext)
                    return Result.success()
                } catch (e: Exception) {
                    reasonForQuizPopup = e.message.orDefaultValue("exception occured")
                    return launchFallbackActivity()
                }
            }

        } else {
            reasonForQuizPopup = "Feature_disabled"
            return launchOldQuizActivity()
        }
    }

    private fun isLocalDataEmpty(): Boolean {
        var size: Int
        runBlocking {
            scheduledQuizNotificationRepository.deleteExpiredNotification()
            val quizNotificationList =
                scheduledQuizNotificationRepository.getScheduledQuizNotificationsFromLocalDB()
            size = quizNotificationList.size
        }
        if (size == 0) {
            reasonForQuizPopup = "Empty_local_DB"
        }
        return size == 0
    }

    private fun launchOldQuizActivity(): Result {
        //If feature is disabled, launch QuizNotificationActivity
        val quizIntent = Intent(applicationContext, QuizNotificationActivity::class.java)
        quizIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        quizIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        applicationContext.startActivity(quizIntent)

        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.QUIZ_NOTIFICATION_TYPE, hashMapOf(
                    EventConstants.QUIZ_NOTIFICATION_TITLE to "old_quiz",
                    EventConstants.QUIZ_NOTIFICATION_STATUS to "TRYING",
                    EventConstants.QUIZ_NOTIFICATION_REASON to reasonForQuizPopup,
                    EventConstants.QUIZ_STUDENT_ID to UserUtil.getStudentId(),
                    EventConstants.FEATURE to TAG
                ), ignoreSnowplow = true
            )
        )
        enqueue(applicationContext)
        return Result.success()
    }

    fun launchFallbackActivity(): Result {
        //In any fallback, launch QuizNotificationActivity
        val quizIntent = Intent(applicationContext, QuizFallbackActivity::class.java)
        quizIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        quizIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        applicationContext.startActivity(quizIntent)

        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.QUIZ_NOTIFICATION_TYPE, hashMapOf(
                    EventConstants.QUIZ_NOTIFICATION_TITLE to "fallback_quiz",
                    EventConstants.QUIZ_NOTIFICATION_STATUS to "TRYING",
                    EventConstants.QUIZ_NOTIFICATION_REASON to reasonForQuizPopup,
                    EventConstants.QUIZ_STUDENT_ID to UserUtil.getStudentId(),
                    EventConstants.FEATURE to TAG
                ), ignoreSnowplow = true
            )
        )
        enqueue(applicationContext)
        return Result.success()
    }

    private fun isPopupNotAllowed(): Boolean {
        try {
            val manager: ActivityManager =
                applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val taskList: List<ActivityManager.RunningTaskInfo> = manager.getRunningTasks(10)
            val list = listOf(
                LiveClassActivity::class.java.name,
                CameraActivity::class.java.name,
                MatchQuestionActivity::class.java.name,
                CropImageActivity::class.java.name,
                VideoPageActivity::class.java.name,
                LiveClassReminderActivity::class.java.name,
                LiveClassChatActivity::class.java.name,
                LiveClassReminderActivity::class.java.name
            )
            return list.contains(taskList[0].topActivity?.className)
        } catch (e: Exception) {
            return true
        }
    }

    companion object {
        const val TAG = "FetchQuizJobWorker"

        fun enqueue(
            context: Context,
            hour: Int? = null,
            min: Int? = null
        ) {
            val todaysCalander = Calendar.getInstance()
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour ?: 20)
                set(Calendar.MINUTE, min ?: 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (cal.before(todaysCalander)) {
                cal.add(Calendar.DAY_OF_MONTH, 1)
            }
            enqueue(context, cal.timeInMillis - todaysCalander.timeInMillis)
        }

        fun enqueue(context: Context, initialDelay: Long) {
            WorkManager
                .getInstance(context)
                .enqueueUniqueWork(
                    TAG,
                    ExistingWorkPolicy.REPLACE,
                    OneTimeWorkRequestBuilder<FetchQuizJobWorker>()
                        .setInitialDelay(
                            initialDelay, TimeUnit.MILLISECONDS
                        )
                        .build()
                )
        }
    }
}