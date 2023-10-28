package com.doubtnutapp

import android.util.Log
import com.doubtnut.core.constant.ErrorConstants
import com.doubtnut.core.utils.ToastUtils
import com.google.firebase.crashlytics.FirebaseCrashlytics

object Log {

    private val shouldLogOnFabric = DoubtnutApp.INSTANCE.isRelease()
    private const val DEFAULT_TAG = "DebugLog"

    /**
     * Log non fatal exception on fabric
     */
    fun e(exception: Throwable, tag: String? = DEFAULT_TAG) {
        if (shouldLogOnFabric) {
            exception.message?.let {
                ToastUtils.makeTextInDev(message = it)?.show()
                FirebaseCrashlytics.getInstance().setCustomKey(ErrorConstants.DN_FATAL, true)
                FirebaseCrashlytics.getInstance().recordException(exception)
            }
        }
        Log.e(tag, exception.message, exception)
        exception.message?.let {
            ToastUtils.makeTextInDev(message = it)
        }
    }

    /**
     * Used to track debug messages with Fabric
     * These messages will be attached in a subsequent crash
     */
    fun d(message: String, tag: String? = DEFAULT_TAG) {
        if (shouldLogOnFabric) {
            FirebaseCrashlytics.getInstance().log(message)
        }
        Log.d(tag, message)
    }
}