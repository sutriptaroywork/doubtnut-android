package com.doubtnutapp.analytics

import android.os.Bundle
import com.apxor.androidsdk.core.Attributes
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.analytics.IAnalyticsPublisher
import com.doubtnut.core.entitiy.CoreAnalyticsEvent
import com.doubtnut.core.utils.DateTimeUtils
import com.doubtnut.core.utils.ThreadUtils
import com.doubtnutapp.analytics.di.qualifier.*
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnutapp.utils.UserUtil
import com.moe.pushlibrary.PayloadBuilder
import io.branch.referral.util.BranchEvent
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Anand Gaurav on 2019-07-17.
 */
@Singleton
class AnalyticsPublisher @Inject constructor(
    @FirebaseTrackerInfo firebaseTracker: AnalyticsTracker<Pair<String, Bundle>>,
    @ApxorTrackerInfo apxorTracker: AnalyticsTracker<Pair<String, Attributes>>,
    @FacebookTrackerInfo facebookTracker: AnalyticsTracker<Pair<String, Bundle>>,
    @SnowPlowTrackerInfo snowPlowTracker: AnalyticsTracker<StructuredEvent>,
    @BranchIoTrackerInfo branchIoTracker: AnalyticsTracker<Pair<String, BranchEvent>>,
    @MoEngageTrackerInfo moEngageTracker: AnalyticsTracker<Pair<String, PayloadBuilder>>,
    /*@ConvivaTrackerInfo convivaTracker: AnalyticsTracker<ConvivaEvent>*/
) : IAnalyticsPublisher {

    private val eventPubSub: PublishSubject<CoreAnalyticsEvent> = PublishSubject.create()
    private val snowplowEventSub: PublishSubject<CoreAnalyticsEvent> = PublishSubject.create()
    private val branchIoEventSub: PublishSubject<CoreAnalyticsEvent> = PublishSubject.create()
    private val moEngageEventSub: PublishSubject<CoreAnalyticsEvent> = PublishSubject.create()
    private val convivaEventSub: PublishSubject<CoreAnalyticsEvent> = PublishSubject.create()

    private val logEventThread = ThreadUtils.logEventThread

    init {
        eventPubSub.run {
            apxorTracker.subscribe(this, Schedulers.from(logEventThread))
            facebookTracker.subscribe(this, Schedulers.from(logEventThread))
            branchIoTracker.subscribe(this, Schedulers.from(logEventThread))
            snowPlowTracker.subscribe(this, Schedulers.from(logEventThread))
            firebaseTracker.subscribe(this, Schedulers.from(logEventThread))
            moEngageTracker.subscribe(this, Schedulers.from(logEventThread))
            /*convivaTracker.subscribe(this, Schedulers.from(logEventThread))*/
        }
        snowplowEventSub.run {
            snowPlowTracker.subscribe(this, Schedulers.from(logEventThread))
        }

        branchIoEventSub.run {
            branchIoTracker.subscribe(this, Schedulers.from(logEventThread))
        }

        moEngageEventSub.run {
            moEngageTracker.subscribe(this, Schedulers.from(logEventThread))
        }

        convivaEventSub.run {
            moEngageTracker.subscribe(this, Schedulers.from(logEventThread))
        }
    }

    override fun publishEvent(event: CoreAnalyticsEvent) {
        eventPubSub.onNext(
            event.apply {
                params.apply {
                    putAll(getDefaultParams())
                }
            }
        )
    }

    // send structured event to snowplow only
    fun publishEvent(event: StructuredEvent) {
        snowplowEventSub.onNext(
            event.apply {
                ignoreApxor = true
                ignoreBranch = true
                ignoreFacebook = true
                ignoreSnowplow = false
                ignoreMoengage = true
                ignoreFirebase = true
                ignoreConviva = true
            }.apply {
                params.apply {
                    putAll(getDefaultParams())
                }
            }
        )
    }

    fun publishBranchIoEvent(event: CoreAnalyticsEvent) {
        branchIoEventSub.onNext(
            event.apply {
                ignoreApxor = true
                ignoreBranch = false
                ignoreFacebook = true
                ignoreSnowplow = true
                ignoreMoengage = true
                ignoreFirebase = true
                ignoreConviva = true
            }.apply {
                params.apply {
                    putAll(getDefaultParams())
                }
            }
        )
    }

    fun publishFirebaseEvent(event: CoreAnalyticsEvent) {
        eventPubSub.onNext(
            event.apply {
                ignoreApxor = true
                ignoreBranch = true
                ignoreFacebook = true
                ignoreSnowplow = true
                ignoreMoengage = true
                ignoreFirebase = false
                ignoreConviva = true
            }.apply {
                params.apply {
                    putAll(getDefaultParams())
                }
            }
        )
    }

    fun publishMoEngageEvent(event: CoreAnalyticsEvent) {
        moEngageEventSub.onNext(
            event.apply {
                ignoreApxor = true
                ignoreBranch = true
                ignoreFacebook = true
                ignoreSnowplow = true
                ignoreMoengage = false
                ignoreFirebase = true
                ignoreConviva = true
            }.apply {
                params.apply {
                    putAll(getDefaultParams())
                }
            }
        )
    }

    fun publishConvivaEvent(event: CoreAnalyticsEvent) {
        convivaEventSub.onNext(
            event.apply {
                ignoreApxor = true
                ignoreBranch = true
                ignoreFacebook = true
                ignoreSnowplow = true
                ignoreMoengage = true
                ignoreFirebase = true
                ignoreConviva = true // Disable conviva for now
            }.apply {
                params.apply {
                    putAll(getDefaultParams())
                }
            }
        )
    }

    private fun getDefaultParams() =
        hashMapOf<String, Any>().apply {
            put(EventConstants.DEVICE_TIME_STAMP, System.currentTimeMillis())
            put(EventConstants.EVENT_TIME, DateTimeUtils.getEventTime())
            put(EventConstants.EVENT_TIME2, DateTimeUtils.getValidatedEventTime())
            put("selected_board", UserUtil.getUserBoard())
            put("selected_exam", UserUtil.getUserExams())
        }
}
