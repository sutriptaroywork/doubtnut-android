package com.doubtnutapp.scheduledquiz

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.*
import com.doubtnutapp.scheduledquiz.db.repository.ScheduledQuizNotificationRepository
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.utils.Utils
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.util.*
import javax.inject.Inject

class ScheduledQuizNotificationWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    companion object {
        const val CURRENT_DAY = "current_day"
        const val FETCH_NOTIFICATION_TAG = "fetch_scheduled_notification"

        private const val TAG = "ScheduledQuizNotifWorker"
    }

    @Inject
    lateinit var scheduledQuizNotificationRepository: ScheduledQuizNotificationRepository

    private var params = hashMapOf<String, String>()

    override suspend fun doWork(): Result {

        DoubtnutApp.INSTANCE.getEventTracker()
            .addEventNames(EventConstants.EVENT_NAME_APP_OPEN_DN + "_quiz_req")
            .addEventParameter(EventConstants.SOURCE, "quiz_req")
            .addStudentId(UserUtil.getStudentId())
            .track()

        //Check if local database table scheduled_notifications is empty
        val notificationList =
            scheduledQuizNotificationRepository.getScheduledQuizNotificationsFromLocalDB()

        var result = Result.failure()
        if (notificationList.isEmpty() && FeaturesManager.isFeatureEnabled(
                DoubtnutApp.INSTANCE,
                Features.QUIZ_POPUP
            ) && isNetworkCallNotBetweenOneAndSix()
        ) {
            //Empty DB => Call API
            params["current_day"] =
                defaultPrefs().getString(Constants.QUIZ_CURRENT_DAY, "0").orDefaultValue("0")

            scheduledQuizNotificationRepository.getScheduledQuizNotifFromAPI(params)
                .map {
                    it.data
                }
                .catch { error ->
                    try {
                        if (error is HttpException) {
                            val code = error.response()?.code()
                            if (code != null && code >= 400 && code < 500) {
                                result = Result.failure()
                            }
                        } else {
                            result = Result.retry()
                        }
                    } catch (e: Exception) {
                        result = Result.retry()
                    }
                }
                .collect {
                    scheduledQuizNotificationRepository.addListToDB(it.quizData)
                    result = Result.success()
                }
            return result
        } else {
            return Result.success()
        }
    }

    private fun isNetworkCallNotBetweenOneAndSix(): Boolean {
        val cal = Calendar.getInstance()
        cal.time = Date()
        val hour: Int = cal[Calendar.HOUR_OF_DAY]
        return (hour < 1 || hour >= 6)
    }

}