package com.doubtnut.core.utils

import com.doubtnut.core.CoreApplication
import com.google.android.gms.common.util.concurrent.NamedThreadFactory
import java.util.concurrent.Executors

object ThreadUtils {

    private const val ANALYTICS_THREAD = "Analytics-Thread"

    val logEventThread =
        Executors.newSingleThreadExecutor(NamedThreadFactory(ANALYTICS_THREAD))!!

    fun runOnAnalyticsThread(block: () -> Unit) {
        logEventThread.execute {
            try {
                block()
            } catch (e: Exception) {
                CoreApplication.INSTANCE.runOnMainThread {
                    ToastUtils.makeTextInDev(
                        message = e.message ?: "Error in runOnAnalyticsThread()"
                    )?.show()
                }
            }
        }
    }

}