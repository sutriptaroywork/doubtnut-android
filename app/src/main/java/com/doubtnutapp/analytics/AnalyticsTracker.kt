package com.doubtnutapp.analytics

import android.os.Bundle
import androidx.annotation.WorkerThread
import com.doubtnut.core.entitiy.CoreAnalyticsEvent
import com.doubtnut.core.utils.CoreRemoteConfigUtils
import io.reactivex.Observable
import io.reactivex.Scheduler

/**
 * Created by Anand Gaurav on 2019-07-17.
 */
interface AnalyticsTracker<T> {
    fun subscribe(eventPubSub: Observable<CoreAnalyticsEvent>, scheduler: Scheduler)
    fun filterEvent(event: CoreAnalyticsEvent): Boolean
    fun transformEvent(event: CoreAnalyticsEvent): T
    fun logEvent(event: CoreAnalyticsEvent)

    @WorkerThread
    fun postEvent(event: T)
}
