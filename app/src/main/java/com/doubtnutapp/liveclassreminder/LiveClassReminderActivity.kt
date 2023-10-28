package com.doubtnutapp.liveclassreminder

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.View
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.CoreApplication
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnutapp.databinding.ActivityLiveClassReminderBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.splash.SplashActivity
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.utils.Utils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class LiveClassReminderActivity :
    BaseBindingActivity<DummyViewModel, ActivityLiveClassReminderBinding>() {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var disposable: CompositeDisposable

    var data: HashMap<String, String>? = null
    private var reminderData: LiveClassReminderNotificationData? = null

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private var UNIQUE_LOG_TAG = ""

    companion object {
        private const val TAG = "LiveClassReminderActivity"
    }

    private fun openSplash() {
        startActivity(
            Intent(this, SplashActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        finish()
    }

    private fun sendEvent(eventName: String) {
        val tracker = (applicationContext as DoubtnutApp).getEventTracker()
        tracker.addEventNames(eventName)
            .addStudentId(UserUtil.getStudentId())
            .addScreenName(EventConstants.LIVE_CLASS_ALERT)
            .track()
    }

    override fun onStart() {
        super.onStart()
        val udid = Utils.getUDID()
        UNIQUE_LOG_TAG = "id_" + udid + "_" + System.currentTimeMillis().toString()
        val timeStamp = System.currentTimeMillis()
        val params = hashMapOf<String, Any>().apply {
            put(EventConstants.STATUS, EventConstants.LIVE_CLASS_ALERT_ACTIVITY_ENTERED_FOREGROUND)
            put(EventConstants.EVENT_NAME_ID, UNIQUE_LOG_TAG)
            put(EventConstants.EVENT_UDID, udid)
            put(EventConstants.TIME_STAMP, timeStamp)
        }
        analyticsPublisher.publishEvent(
            StructuredEvent(
                EventConstants.LIVE_CLASS_ALERT_ACTIVITY_STATE,
                EventConstants.LIVE_CLASS_ALERT_ACTIVITY_ENTERED_FOREGROUND_ACTION,
                eventParams = params
            )
        )
    }

    override fun onStop() {
        super.onStop()
        val timeStamp = System.currentTimeMillis()
        val params = hashMapOf<String, Any>().apply {
            put(EventConstants.STATUS, EventConstants.LIVE_CLASS_ALERT_ACTIVITY_ENTERED_BACKGROUND)
            put(EventConstants.EVENT_NAME_ID, UNIQUE_LOG_TAG)
            put(EventConstants.EVENT_UDID, Utils.getUDID())
            put(EventConstants.TIME_STAMP, timeStamp)
        }
        analyticsPublisher.publishEvent(
            StructuredEvent(
                EventConstants.LIVE_CLASS_ALERT_ACTIVITY_STATE,
                EventConstants.LIVE_CLASS_ALERT_ACTIVITY_ENTERED_BACKGROUND_ACTION,
                eventParams = params
            )
        )
        UNIQUE_LOG_TAG = ""
    }

    private fun handleTimer(count: Long) {
        disposable.add(
            Observable.interval(0, 1, TimeUnit.SECONDS).take(count)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<Long>() {
                    override fun onComplete() {
                        binding.textViewTimer.text = "00:00"
                        disposable.clear()
                    }

                    override fun onNext(t: Long) {
                        val timeLeft = count - t
                        if (timeLeft < 0) return
                        val timeLeftText = if (timeLeft <= 3600) {
                            String.format(
                                "%02d:%02d",
                                TimeUnit.SECONDS.toMinutes(timeLeft) % TimeUnit.HOURS.toMinutes(1),
                                TimeUnit.SECONDS.toSeconds(timeLeft) % TimeUnit.MINUTES.toSeconds(1)
                            )
                        } else {
                            String.format(
                                "%02d:%02d:%02d",
                                TimeUnit.SECONDS.toHours(timeLeft),
                                TimeUnit.SECONDS.toMinutes(timeLeft) % TimeUnit.HOURS.toMinutes(1),
                                TimeUnit.SECONDS.toSeconds(timeLeft) % TimeUnit.MINUTES.toSeconds(1)
                            )
                        }
                        binding.textViewTimer.text = timeLeftText
                    }

                    override fun onError(e: Throwable) {
                        disposable.clear()
                    }
                })
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }

    override fun provideViewBinding(): ActivityLiveClassReminderBinding {
        return ActivityLiveClassReminderBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): DummyViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        binding.imageViewClose.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.LIVE_CLASS_REMINDER_CLOSE_CLICK,
                    EventConstants.SUBJECT, reminderData?.subject.orEmpty()
                )
            )
            finish()
        }

        binding.parentView.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.LIVE_CLASS_REMINDER_OUTSIDE_CLICK,
                    EventConstants.SUBJECT, reminderData?.subject.orEmpty()
                )
            )
            finish()
        }

        val countToSendEvent: Int =
            Utils.getCountToSend(RemoteConfigUtils.getEventInfo(), EventConstants.SESSION_START)
        repeat((0 until countToSendEvent).count()) {
            sendEvent(EventConstants.SESSION_START)
        }

        Utils.sendClassLangEvents(EventConstants.EVENT_NAME_APP_OPEN_DN)
        Utils.sendClassLanguageSpecificEvent(EventConstants.EVENT_NAME_APP_OPEN_DN)

        DoubtnutApp.INSTANCE.getEventTracker()
            .addEventNames(EventConstants.EVENT_NAME_APP_OPEN_DN)
            .addEventParameter(EventConstants.SOURCE, EventConstants.LIVE_CLASS_ALERT)
            .addStudentId(UserUtil.getStudentId())
            .track()

//        DoubtnutApp.INSTANCE.analyticsPublisher.get().publishBranchIoEvent(
//            AnalyticsEvent(EventConstants.EVENT_NAME_APP_OPEN_DN, hashMapOf<String, Any>().apply {
//                put(EventConstants.SOURCE, EventConstants.LIVE_CLASS_ALERT)
//            })
//        )

        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.EVENT_NAME_APP_OPEN_DN,
                hashMapOf<String, Any>().apply {
                    put(EventConstants.SOURCE, EventConstants.LIVE_CLASS_ALERT)
                }, ignoreSnowplow = true
            )
        )

        analyticsPublisher.publishEvent(
            StructuredEvent(category = EventConstants.EVENT_NAME_APP_OPEN_DN_CATEGORY,
                action = EventConstants.EVENT_NAME_APP_OPEN_DN,
                eventParams = hashMapOf<String, Any>().apply {
                    put(EventConstants.SOURCE, EventConstants.LIVE_CLASS_ALERT)
                })
        )



        try {
            data = intent?.extras?.getSerializable("data") as HashMap<String, String>?
            reminderData = Gson().fromJson(
                data?.get("data"),
                object : TypeToken<LiveClassReminderNotificationData?>() {}.type
            )
        } catch (exception: Exception) {

        }
        if (data == null || reminderData == null) {
            openSplash()
        } else {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.LIVE_CLASS_REMINDER_PAGE_OPEN,
                    EventConstants.SUBJECT, reminderData?.subject.orEmpty()
                )
            )
            val colorCode = Utils.parseColor(reminderData?.colorCode.orEmpty())
            binding.textViewOne.text = reminderData?.titleOne.orEmpty()
            binding.textViewTwo.text = reminderData?.titleTwo.orEmpty()
            binding.textViewThree.text = reminderData?.titleThree.orEmpty()
            binding.imageView.loadImageEtx(reminderData?.imageUrl.orEmpty())
            binding.imageViewBackground.loadImageEtx(reminderData?.imageUrlBackground.orEmpty())
            binding.button.text = reminderData?.buttonText.orEmpty()
            binding.layoutTop.setBackgroundColor(colorCode)
            val liveAt = reminderData?.liveAt?.toLongOrNull() ?: System.currentTimeMillis()
            val timeLeft = (liveAt - System.currentTimeMillis()) / 1000
            if (timeLeft > 0) {
                binding.textViewTimer.background = Utils.getShape(
                    "#ffffff",
                    "#ffffff", 2f
                )
                binding.textViewTimer.visibility = View.VISIBLE
                if (Build.VERSION.SDK_INT >= 23)
                    binding.textViewTimer.compoundDrawableTintList = ColorStateList.valueOf(colorCode)
                binding.textViewTimer.setTextColor(colorCode)
                handleTimer(timeLeft)
            } else {
                binding.textViewTimer.visibility = View.INVISIBLE
            }

        }

        binding.cardContainer.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.LIVE_CLASS_REMINDER_JOIN_CLICK,
                    EventConstants.SUBJECT, reminderData?.subject.orEmpty()
                )
            )
            val deepLink = reminderData?.deeplink
            if (deepLink == null) {
                openSplash()
            } else {
                if (defaultPrefs(this).getString(Constants.STUDENT_LOGIN, "false") != "true"
                    || !defaultPrefs(this).getBoolean(Constants.ONBOARDING_COMPLETED, false)
                ) {
                    CoreApplication.pendingDeeplink = deepLink
                    startActivity(
                        Intent(
                            this,
                            SplashActivity::class.java
                        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    )
                    finish()
                } else {
                    deeplinkAction.performAction(this, deepLink)
                    finish()
                }
            }
        }
    }

}
