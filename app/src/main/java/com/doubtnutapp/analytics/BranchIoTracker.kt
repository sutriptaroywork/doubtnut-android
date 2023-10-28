package com.doubtnutapp.analytics

import android.content.Context
import com.doubtnut.analytics.EventDestinations
import com.doubtnut.analytics.debug.AnalyticsInterceptor
import com.doubtnut.core.entitiy.CoreAnalyticsEvent
import com.doubtnut.core.utils.CoreRemoteConfigUtils
import com.doubtnutapp.BuildConfig
import com.doubtnutapp.data.base.di.qualifier.ApplicationContext
import com.doubtnutapp.data.base.di.qualifier.Udid
import com.doubtnutapp.data.common.UserPreference
import io.branch.referral.util.BranchEvent
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.observers.DisposableObserver
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 24/04/20.
 */
class BranchIoTracker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreference: UserPreference,
    @Udid private val udid: String
) : AnalyticsTracker<Pair<String, BranchEvent>> {
    private var disposable: DisposableObserver<Pair<String, BranchEvent>>? = null

    override fun subscribe(eventPubSub: Observable<CoreAnalyticsEvent>, scheduler: Scheduler) {
        disposable = eventPubSub
            .subscribeOn(scheduler)
            .observeOn(scheduler)
            .filter { filterEvent(it) }
            .doOnNext { logEvent(it) }
            .map { transformEvent(it) }
            .subscribeWith(object : DisposableObserver<Pair<String, BranchEvent>>() {
                override fun onNext(branchIoEvent: Pair<String, BranchEvent>) {
                    postEvent(branchIoEvent)
                }

                override fun onError(throwable: Throwable) {
                }

                override fun onComplete() {
                }
            })
    }

    override fun filterEvent(event: CoreAnalyticsEvent): Boolean {
        return !event.ignoreBranch
    }

    override fun transformEvent(event: CoreAnalyticsEvent): Pair<String, BranchEvent> {
        return event.name to BranchEvent(event.name).apply {
            for ((key, value) in event.params) {
                addCustomDataProperty(key, value.toString())
            }
            addCustomDataProperty("OS", "Android")
            addCustomDataProperty("OS_version", android.os.Build.VERSION.SDK_INT.toString())
            addCustomDataProperty("android_id", udid)
            addCustomDataProperty("student_id", userPreference.getUserStudentId())
            addCustomDataProperty("class", userPreference.getUserClass())
            addCustomDataProperty("exams", userPreference.getUserSelectedExams())
        }
    }

    override fun logEvent(event: CoreAnalyticsEvent) {
        if (BuildConfig.DEBUG || BuildConfig.ENABLE_ADMIN_OPTIONS) {

            if (CoreRemoteConfigUtils.getBranchBlacklist()
                    .containsKey(event.name) ||
                CoreRemoteConfigUtils.getGlobalBlacklist()
                    .containsKey(event.name)
            ) return

            AnalyticsInterceptor.intercept(
                AnalyticsInterceptor.Event(
                    arrayListOf(
                        EventDestinations.BRANCH
                    ),
                    event.name, event.params.toString()
                )
            )
        }
    }

    override fun postEvent(event: Pair<String, BranchEvent>) {
        if (CoreRemoteConfigUtils.getBranchBlacklist()
                .containsKey(event.first) ||
            CoreRemoteConfigUtils.getGlobalBlacklist()
                .containsKey(event.first)
        ) return

        event.second.logEvent(context)
    }
}
