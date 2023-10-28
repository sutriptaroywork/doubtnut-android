package com.doubtnutapp.analytics

import android.content.Context
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.EventDestinations
import com.doubtnut.analytics.debug.AnalyticsInterceptor
import com.doubtnut.core.entitiy.CoreAnalyticsEvent
import com.doubtnut.core.utils.CoreRemoteConfigUtils
import com.doubtnutapp.BuildConfig
import com.doubtnutapp.data.common.UserPreference
import com.moe.pushlibrary.MoEHelper
import com.moe.pushlibrary.PayloadBuilder
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.observers.DisposableObserver
import java.util.*
import javax.inject.Inject

class MoEngageTracker @Inject constructor(
    context: Context,
    private val userPreference: UserPreference
) : AnalyticsTracker<Pair<String, PayloadBuilder>> {

    private var disposable: DisposableObserver<Pair<String, PayloadBuilder>>? = null
    private val moEngageHelper: MoEHelper by lazy { MoEHelper.getInstance(context) }

    override fun subscribe(eventPubSub: Observable<CoreAnalyticsEvent>, scheduler: Scheduler) {
        disposable = eventPubSub
            .subscribeOn(scheduler)
            .observeOn(scheduler)
            .filter { filterEvent(it) }
            .doOnNext { logEvent(it) }
            .map { transformEvent(it) }
            .subscribeWith(object : DisposableObserver<Pair<String, PayloadBuilder>>() {
                override fun onNext(t: Pair<String, PayloadBuilder>) {
                    postEvent(t)
                }

                override fun onComplete() {}

                override fun onError(e: Throwable) {}
            })
    }

    override fun filterEvent(event: CoreAnalyticsEvent): Boolean {
        return !event.ignoreMoengage
    }

    override fun transformEvent(event: CoreAnalyticsEvent): Pair<String, PayloadBuilder> {
        return event.name to PayloadBuilder().apply {
            for ((key, value) in event.params) {
                when (value) {
                    is Int -> putAttrInt(key, value)
                    is Float -> putAttrFloat(key, value)
                    is String -> putAttrString(key, value)
                    is Date -> putAttrDate(key, value)
                    is Long -> putAttrLong(key, value)
                    else -> putAttrObject(key, value)
                }
            }
            putAttrString("student_id", userPreference.getUserStudentId())
            putAttrString("class", userPreference.getUserClass())

            val currentTime: Calendar = Calendar.getInstance()
            val currentHour: Int = currentTime.get(Calendar.HOUR_OF_DAY)
            putAttrInt(EventConstants.EVENT_HOUR, currentHour)
        }
    }

    override fun logEvent(event: CoreAnalyticsEvent) {
        if (BuildConfig.DEBUG || BuildConfig.ENABLE_ADMIN_OPTIONS) {

            if (CoreRemoteConfigUtils.getMoengageBlacklist()
                    .containsKey(event.name) ||
                CoreRemoteConfigUtils.getGlobalBlacklist()
                    .containsKey(event.name)
            ) return

            AnalyticsInterceptor.intercept(
                AnalyticsInterceptor.Event(
                    arrayListOf(
                        EventDestinations.MOENGAGE
                    ),
                    event.name, event.params.toString()
                )
            )
        }
    }

    override fun postEvent(event: Pair<String, PayloadBuilder>) {
        if (CoreRemoteConfigUtils.getMoengageBlacklist()
                .containsKey(event.first) ||
            CoreRemoteConfigUtils.getGlobalBlacklist()
                .containsKey(event.first)
        ) return
        moEngageHelper.trackEvent(event.first, event.second)

    }
}
