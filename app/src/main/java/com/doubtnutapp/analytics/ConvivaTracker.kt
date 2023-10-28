package com.doubtnutapp.analytics

import android.content.Context
import com.doubtnut.analytics.EventDestinations
import com.doubtnut.analytics.debug.AnalyticsInterceptor
import com.doubtnut.core.entitiy.CoreAnalyticsEvent
import com.doubtnutapp.BuildConfig
import com.doubtnutapp.analytics.model.ConvivaEvent
import com.doubtnutapp.data.base.di.qualifier.ApplicationContext
import com.doubtnutapp.data.common.UserPreference
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.observers.DisposableObserver
import javax.inject.Inject

/**
 * Created by Sachin Kumar on 2022-04-21.
 */
class ConvivaTracker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreference: UserPreference,
    /*private val trackerController: TrackerController,*/
    private val gson: Gson
) : AnalyticsTracker<ConvivaEvent> {

    private var disposable: DisposableObserver<ConvivaEvent>? = null

    override fun subscribe(eventPubSub: Observable<CoreAnalyticsEvent>, scheduler: Scheduler) {
        disposable = eventPubSub
            .subscribeOn(scheduler)
            .observeOn(scheduler)
            .filter { filterEvent(it) }
            .doOnNext { logEvent(it) }
            .map { transformEvent(it) }
            .subscribeWith(object : DisposableObserver<ConvivaEvent>() {
                override fun onNext(convivaEvent: ConvivaEvent) {
                    postEvent(convivaEvent)
                }

                override fun onError(throwable: Throwable) {
                }

                override fun onComplete() {
                }
            })
    }

    override fun filterEvent(event: CoreAnalyticsEvent): Boolean {
        return !event.ignoreConviva
    }

    override fun transformEvent(event: CoreAnalyticsEvent): ConvivaEvent {
        return ConvivaEvent(
            eventName = event.name,
            eventParams = event.params.apply {
                put("student_id", userPreference.getUserStudentId())
                put("class", userPreference.getUserClass())
                put("language", userPreference.getSelectedLanguage())
            }
        )
    }

    override fun logEvent(event: CoreAnalyticsEvent) {
/*        if (BuildConfig.DEBUG || BuildConfig.ENABLE_ADMIN_OPTIONS) {
            AnalyticsInterceptor.intercept(
                AnalyticsInterceptor.Event(
                    arrayListOf(
                        EventDestinations.CONVIVA
                    ),
                    event.name, event.params.toString()
                )
            )
        }*/
    }

    override fun postEvent(event: ConvivaEvent) {
        /*if (event.eventName.isEmpty()) return
        ThreadUtils.runOnAnalyticsThread {
            trackerController.trackCustomEvent(event.eventName, gson.toJson(event.params))
        }*/
    }
}