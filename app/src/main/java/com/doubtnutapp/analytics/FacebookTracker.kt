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
import com.facebook.appevents.AppEventsLogger
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.observers.DisposableObserver
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2020-01-14.
 */
class FacebookTracker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreference: UserPreference
) : AnalyticsTracker<Pair<String, Bundle>> {

    private var disposable: DisposableObserver<Pair<String, Bundle>>? = null
    val mFacebookAnalytics: AppEventsLogger by lazy { AppEventsLogger.newLogger(context) }

    override fun subscribe(eventPubSub: Observable<CoreAnalyticsEvent>, scheduler: Scheduler) {
        disposable = eventPubSub
            .subscribeOn(scheduler)
            .observeOn(scheduler)
            .filter { filterEvent(it) }
            .doOnNext { logEvent(it) }
            .map { transformEvent(it) }
            .subscribeWith(object : DisposableObserver<Pair<String, Bundle>>() {
                override fun onNext(facebookEvent: Pair<String, Bundle>) {
                    postEvent(facebookEvent)
                }

                override fun onError(throwable: Throwable) {
                }

                override fun onComplete() {
                }
            })
    }

    override fun filterEvent(event: CoreAnalyticsEvent): Boolean {
        return !event.ignoreFacebook
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

            if (CoreRemoteConfigUtils.getFacebookBlacklist()
                    .containsKey(event.name) ||
                CoreRemoteConfigUtils.getGlobalBlacklist()
                    .containsKey(event.name)
            ) return

            AnalyticsInterceptor.intercept(
                AnalyticsInterceptor.Event(
                    arrayListOf(
                        EventDestinations.FACEBOOK
                    ),
                    event.name, event.params.toString()
                )
            )
        }
    }

    /**
     * https://play.google.com/console/u/0/developers/8879759867892395282/app/4973323575198231399/vitals/crashes/d19c4a7b/details?installedFrom=PLAY_STORE&days=60
     * https://play.google.com/console/u/0/developers/8879759867892395282/app/4973323575198231399/vitals/crashes/47f083e5/details?versionCode=894
     * https://play.google.com/console/u/0/developers/8879759867892395282/app/4973323575198231399/vitals/crashes/966abd46/details?days=30
     */
    override fun postEvent(event: Pair<String, Bundle>) {
        if (CoreRemoteConfigUtils.getFacebookBlacklist()
                .containsKey(event.first) ||
            CoreRemoteConfigUtils.getGlobalBlacklist()
                .containsKey(event.first)
        ) return

        mFacebookAnalytics.logEvent(event.first, event.second)
    }

    companion object {
        const val TAG = "FacebookTracker"
    }
}
