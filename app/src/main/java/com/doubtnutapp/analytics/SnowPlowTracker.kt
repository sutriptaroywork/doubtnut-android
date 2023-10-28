package com.doubtnutapp.analytics

import android.content.Context
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.EventDestinations
import com.doubtnut.analytics.debug.AnalyticsInterceptor
import com.doubtnutapp.BuildConfig
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.entitiy.CoreAnalyticsEvent
import com.doubtnut.core.utils.CoreRemoteConfigUtils
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnutapp.data.base.di.qualifier.AppVersion
import com.doubtnutapp.data.base.di.qualifier.AppVersionCode
import com.doubtnutapp.data.base.di.qualifier.ApplicationContext
import com.doubtnutapp.data.common.UserPreference
import com.snowplowanalytics.snowplow.Snowplow
import com.snowplowanalytics.snowplow.event.Structured
import com.snowplowanalytics.snowplow.payload.SelfDescribingJson
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.observers.DisposableObserver
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2020-01-14.
 */
class SnowPlowTracker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreference: UserPreference,
    @AppVersionCode private val appVersionCode: Int,
    @AppVersion val appVersionName: String
) : AnalyticsTracker<StructuredEvent> {
    private var disposable: DisposableObserver<StructuredEvent>? = null

    override fun subscribe(eventPubSub: Observable<CoreAnalyticsEvent>, scheduler: Scheduler) {
        disposable = eventPubSub
            .subscribeOn(scheduler)
            .observeOn(scheduler)
            .filter { filterEvent(it) }
            .map { transformEvent(it) }
            .doOnNext { logEvent(it) }
            .subscribeWith(object : DisposableObserver<StructuredEvent>() {
                override fun onNext(snowPlowEvent: StructuredEvent) {
                    postEvent(snowPlowEvent)
                }

                override fun onError(throwable: Throwable) {
                }

                override fun onComplete() {
                }
            })
    }

    override fun filterEvent(event: CoreAnalyticsEvent): Boolean {
        return !event.ignoreSnowplow
    }

    override fun transformEvent(event: CoreAnalyticsEvent): StructuredEvent {

        event.params.apply {
            put("student_id", userPreference.getUserStudentId())
            put("class", userPreference.getUserClass())
            put("language", userPreference.getSelectedLanguage())
            put(EventConstants.DN_VERSION_CODE, appVersionCode)
            put(EventConstants.DN_VERSION_NAME, appVersionName)
        }

        return if (event is StructuredEvent) {
            event
        } else {
            StructuredEvent(
                category = "${event.name}-category",
                action = event.name,
                eventParams = event.params
            )
        }
    }

    override fun logEvent(event: CoreAnalyticsEvent) {
        if (BuildConfig.DEBUG || BuildConfig.ENABLE_ADMIN_OPTIONS) {

            if (CoreRemoteConfigUtils.getSnowPlowBlacklist()
                    .containsKey(event.name) ||
                CoreRemoteConfigUtils.getGlobalBlacklist()
                    .containsKey(event.name)
            ) return

            val eventParam = if (event is StructuredEvent) {
                "Category - " + event.category + " | Label - " + event.label + " | Value - " +
                    event.value + " | Property - " + event.property + " | Params - " + event.params.toString()
            } else {
                "Category - " + "${event.name}-category" + event.params.toString()
            }
            AnalyticsInterceptor.intercept(
                AnalyticsInterceptor.Event(
                    arrayListOf(
                        EventDestinations.SNOWPLOW
                    ),
                    event.name, eventParam
                )
            )
        }
    }

    override fun postEvent(event: StructuredEvent) {
        if (event.action.isEmpty()) return

        if (CoreRemoteConfigUtils.getSnowPlowBlacklist()
                .containsKey(event.action) ||
            CoreRemoteConfigUtils.getGlobalBlacklist()
                .containsKey(event.action)
        ) return


        val context = SelfDescribingJson(
            "iglu:com.snowplowanalytics.iglu/anything-a/jsonschema/1-0-0",
            event.eventParams
        )

        val eventBuilder = Structured(event.category, event.action)

        if (event.label != null && event.label.isNotEmpty()) {
            eventBuilder.label(event.label)
        }
        if (event.property != null && event.property.isNotEmpty()) {
            eventBuilder.property(event.property)
        }
        if (event.value != null && !event.value.isNaN()) {
            eventBuilder.value(event.value)
        }

        eventBuilder.contexts(listOf(context))
        Snowplow.getDefaultTracker()?.track(eventBuilder)

    }
}
