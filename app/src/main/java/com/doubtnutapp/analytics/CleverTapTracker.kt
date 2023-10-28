package com.doubtnutapp.analytics

import android.content.Context
import com.doubtnut.analytics.EventDestinations
import com.doubtnut.analytics.debug.AnalyticsInterceptor
import com.doubtnut.core.entitiy.CoreAnalyticsEvent
import com.doubtnutapp.BuildConfig
import com.doubtnutapp.data.base.di.qualifier.ApplicationContext
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.observers.DisposableObserver
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-07-17.
 */
class CleverTapTracker @Inject constructor(@ApplicationContext private val context: Context) :
    AnalyticsTracker<CoreAnalyticsEvent> {
    private var disposable: DisposableObserver<CoreAnalyticsEvent>? = null

    override fun subscribe(eventPubSub: Observable<CoreAnalyticsEvent>, scheduler: Scheduler) {
        disposable = eventPubSub
            .subscribeOn(scheduler)
            .observeOn(scheduler)
            .filter { filterEvent(it) }
            .doOnNext { logEvent(it) }
            .map { transformEvent(it) }
            .subscribeWith(object : DisposableObserver<CoreAnalyticsEvent>() {
                override fun onNext(cleverTapEvent: CoreAnalyticsEvent) {
                    postEvent(cleverTapEvent)
                }

                override fun onError(throwable: Throwable) {
                }

                override fun onComplete() {
                }
            })
    }

    override fun filterEvent(event: CoreAnalyticsEvent): Boolean {
        return true
    }

    override fun transformEvent(event: CoreAnalyticsEvent): CoreAnalyticsEvent {
        return event
    }

    override fun logEvent(event: CoreAnalyticsEvent) {
        /*if (BuildConfig.DEBUG || BuildConfig.ENABLE_ADMIN_OPTIONS) {
            AnalyticsInterceptor.intercept(
                AnalyticsInterceptor.Event(
                    arrayListOf(
                        EventDestinations.CLEVERTAP
                    ),
                    event.name, event.params.toString()
                )
            )
        }*/
    }

    override fun postEvent(event: CoreAnalyticsEvent) {
    }
}
