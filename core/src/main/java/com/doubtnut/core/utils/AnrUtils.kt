package com.doubtnut.core.utils

import com.doubtnut.core.constant.ErrorConstants
import com.github.anrwatchdog.ANRWatchDog
import com.google.firebase.crashlytics.FirebaseCrashlytics

object AnrUtils {

    /**
     * Using Random number to restrict ANR watch dog usage..
     * Currently: 25% perctage of user
     */
    fun initAnrWatchDog() {
        ANRWatchDog()
            .setReportMainThreadOnly()
            .setANRListener { anrError ->
                FirebaseCrashlytics.getInstance()
                    .setCustomKey(ErrorConstants.IS_ANR, true)
                FirebaseCrashlytics.getInstance().recordException(anrError)
            }
            .start()
    }
}
