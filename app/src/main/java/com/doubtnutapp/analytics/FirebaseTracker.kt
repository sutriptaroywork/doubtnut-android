package com.doubtnutapp.analytics

import android.content.Context
import android.os.Bundle
import com.doubtnut.analytics.EventDestinations
import com.doubtnut.analytics.debug.AnalyticsInterceptor
import com.doubtnut.core.entitiy.CoreAnalyticsEvent
import com.doubtnut.core.utils.CoreRemoteConfigUtils
import com.doubtnutapp.BuildConfig
import com.doubtnutapp.data.base.di.qualifier.ApplicationContext
import com.doubtnutapp.data.common.UserPreference
import com.google.firebase.analytics.FirebaseAnalytics
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.observers.DisposableObserver
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-07-17.
 */
class FirebaseTracker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreference: UserPreference
) : AnalyticsTracker<Pair<String, Bundle>> {
    private var disposable: DisposableObserver<Pair<String, Bundle>>? = null
    private val mFirebaseAnalytics: FirebaseAnalytics by lazy {
        FirebaseAnalytics.getInstance(context).apply {
            setUserId(userPreference.getUserStudentId())
        }
    }

    override fun subscribe(eventPubSub: Observable<CoreAnalyticsEvent>, scheduler: Scheduler) {
        disposable = eventPubSub
            .subscribeOn(scheduler)
            .observeOn(scheduler)
            .filter { filterEvent(it) }
            .doOnNext { logEvent(it) }
            .map { transformEvent(it) }
            .subscribeWith(object : DisposableObserver<Pair<String, Bundle>>() {
                override fun onNext(firebaseEvent: Pair<String, Bundle>) {
                    postEvent(firebaseEvent)
                }

                override fun onError(throwable: Throwable) {
                }

                override fun onComplete() {
                }
            })
    }

    override fun filterEvent(event: CoreAnalyticsEvent): Boolean {
        return !event.ignoreFirebase
    }

    override fun transformEvent(event: CoreAnalyticsEvent): Pair<String, Bundle> {
        return event.name to Bundle().apply {
            for ((key, value) in event.params) {
                when (value) {
                    is Int -> putInt(key, value)
                    is String -> putString(key, value)
                    is Long -> putLong(key, value)
                    is Float -> putFloat(key, value)
                    is Boolean -> putBoolean(key, value)
                }
            }
            putString("student_id", userPreference.getUserStudentId())
            putString("class", userPreference.getUserClass())
        }
    }

    override fun logEvent(event: CoreAnalyticsEvent) {
        if (BuildConfig.DEBUG || BuildConfig.ENABLE_ADMIN_OPTIONS) {

            if (CoreRemoteConfigUtils.getFirebaseBlacklist()
                    .containsKey(event.name) ||
                CoreRemoteConfigUtils.getGlobalBlacklist()
                    .containsKey(event.name)
            ) return

            AnalyticsInterceptor.intercept(
                AnalyticsInterceptor.Event(
                    arrayListOf(
                        EventDestinations.FIREBASE
                    ),
                    event.name, event.params.toString()
                )
            )
        }
    }

    override fun postEvent(event: Pair<String, Bundle>) {
        if (CoreRemoteConfigUtils.getFirebaseBlacklist()
                .containsKey(event.first) ||
            CoreRemoteConfigUtils.getGlobalBlacklist()
                .containsKey(event.first)
        ) return
        mFirebaseAnalytics.logEvent(event.first, event.second)

    }
}
