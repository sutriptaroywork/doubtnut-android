package com.doubtnutapp.analytics

import android.content.Context
import com.apxor.androidsdk.core.Attributes
import com.doubtnut.analytics.EventDestinations
import com.doubtnut.analytics.debug.AnalyticsInterceptor
import com.doubtnut.core.entitiy.CoreAnalyticsEvent
import com.doubtnut.core.utils.CoreRemoteConfigUtils
import com.doubtnutapp.BuildConfig
import com.doubtnutapp.data.base.di.qualifier.ApplicationContext
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.utils.ApxorUtils
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.observers.DisposableObserver
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-07-17.
 */
class ApxorTracker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreference: UserPreference
) : AnalyticsTracker<Pair<String, Attributes>> {

    private var disposable: DisposableObserver<Pair<String, Attributes>>? = null

    override fun subscribe(eventPubSub: Observable<CoreAnalyticsEvent>, scheduler: Scheduler) {
        disposable = eventPubSub
            .subscribeOn(scheduler)
            .observeOn(scheduler)
            .filter { filterEvent(it) }
            .doOnNext { logEvent(it) }
            .map { transformEvent(it) }
            .subscribeWith(object : DisposableObserver<Pair<String, Attributes>>() {
                override fun onNext(apxorEvent: Pair<String, Attributes>) {
                    postEvent(apxorEvent)
                }

                override fun onError(throwable: Throwable) {
                }

                override fun onComplete() {
                }
            })
    }

    override fun filterEvent(event: CoreAnalyticsEvent): Boolean {
        return !event.ignoreApxor
    }

    override fun transformEvent(event: CoreAnalyticsEvent): Pair<String, Attributes> {
        return event.name to Attributes().apply {
            event.params.entries.forEach { entry ->
                putAttribute(entry.key, entry.value.toString())
            }
            putAttribute("student_id", userPreference.getUserStudentId())
            putAttribute("class", userPreference.getUserClass())
            putAttribute("language", userPreference.getSelectedLanguage())
        }
    }



    override fun logEvent(event: CoreAnalyticsEvent) {
        if (BuildConfig.DEBUG || BuildConfig.ENABLE_ADMIN_OPTIONS) {

            if (CoreRemoteConfigUtils.getApxorBlacklist()
                    .containsKey(event.name) ||
                CoreRemoteConfigUtils.getGlobalBlacklist()
                    .containsKey(event.name)
            ) return

            AnalyticsInterceptor.intercept(
                AnalyticsInterceptor.Event(
                    arrayListOf(
                        EventDestinations.APXOR
                    ),
                    event.name, event.params.toString()
                )
            )
        }
    }

    override fun postEvent(event: Pair<String, Attributes>) {
        if (event.first.isEmpty()) return
        if (CoreRemoteConfigUtils.getApxorBlacklist()
                .containsKey(event.first) ||
            CoreRemoteConfigUtils.getGlobalBlacklist()
                .containsKey(event.first)
        ) return

        ApxorUtils.logAppEvent(event.first, event.second)
    }
}
