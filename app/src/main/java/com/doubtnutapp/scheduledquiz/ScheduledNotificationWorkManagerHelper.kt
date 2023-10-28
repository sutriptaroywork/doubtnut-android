package com.doubtnutapp.scheduledquiz

import android.content.Context
import androidx.work.*
import com.doubtnutapp.Constants
import com.doubtnutapp.data.base.di.qualifier.ApplicationContext
import com.doubtnutapp.defaultPrefs
import com.doubtnutapp.orDefaultValue
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ScheduledNotificationWorkManagerHelper @Inject constructor(@ApplicationContext private val context: Context) {

    fun assignPeriodicWork(context: Context) {
        val workManager = WorkManager.getInstance(context)
        val request = PeriodicWorkRequestBuilder<ScheduledQuizNotificationWorker>(6, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.HOURS)
            .setInputData(
                Data.Builder()
                    .apply {
                        putString(
                            ScheduledQuizNotificationWorker.CURRENT_DAY,
                            defaultPrefs().getString(Constants.QUIZ_CURRENT_DAY, "0")
                                .orDefaultValue("0")
                        )
                    }
                    .build()
            )
            .addTag(ScheduledQuizNotificationWorker.FETCH_NOTIFICATION_TAG)
            .build()

        workManager.enqueueUniquePeriodicWork(
            ScheduledQuizNotificationWorker.FETCH_NOTIFICATION_TAG,
            ExistingPeriodicWorkPolicy.REPLACE,
            request
        )

    }

}