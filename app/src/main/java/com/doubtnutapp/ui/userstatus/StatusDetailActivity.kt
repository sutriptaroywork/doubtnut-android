package com.doubtnutapp.ui.userstatus

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.EventBus.ApplicationStateEvent
import com.doubtnutapp.EventBus.StatusBottomSheetClosed
import com.doubtnutapp.addEventNames
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.userstatus.StatusMetaDetailItem
import com.doubtnutapp.data.remote.models.userstatus.UserStatus
import com.doubtnutapp.databinding.ActivityStatusDetailBinding
import com.doubtnutapp.feed.UserStatusActionType
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.utils.showApiErrorToast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.android.AndroidInjection
import io.reactivex.disposables.Disposable
import java.util.*
import javax.inject.Inject

class StatusDetailActivity : AppCompatActivity(),
    StatusDetailFragment.OnStatusListPositionChangeListener {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    lateinit var userStatusList: ArrayList<UserStatus>
    var statusAdsList: ArrayList<UserStatus>? = arrayListOf()

    lateinit var adapter: StatusPagerAdapter

    private var startPostion = 0

    private var appStateObserver: Disposable? = null

    private var totalEngagementTime: Int = 0
    private var engamentTimeToSend: Number = 0
    private var timerTask: TimerTask? = null
    private var engageTimer: Timer? = null
    private var handler: Handler? = null
    var isApplicationBackground = false
    private var source: String = EventConstants.STATUS_SOURCE_HEADER

    private lateinit var binding: ActivityStatusDetailBinding

    companion object {
        fun getStartIntent(
            context: Context,
            source: String,
            userStatus: List<UserStatus>,
            statusAds: List<UserStatus>?,
            startPosition: Int
        ) =
            Intent(context, StatusDetailActivity::class.java).apply {
                putExtra(Constants.SOURCE, source)
                putExtra(Constants.DATA, userStatus as ArrayList)
                putExtra(Constants.AD_DATA, statusAds as ArrayList?)
                putExtra(Constants.ITEM_POSITION, startPosition)
            }

        fun getStartIntent(
            context: Context,
            source: String,
            userStatus: String,
            statusAds: List<UserStatus>?,
            startPosition: Int
        ) =
            Intent(context, StatusDetailActivity::class.java).apply {
                putExtra(Constants.SOURCE, source)
                putExtra(Constants.DATA, getUserStatusListFromJson(userStatus))
                putExtra(Constants.AD_DATA, statusAds as ArrayList?)
                putExtra(Constants.ITEM_POSITION, startPosition)
            }

        private fun getUserStatusListFromJson(userStatus: String): ArrayList<UserStatus>? {
            if(userStatus.isNullOrEmpty()){
                return arrayListOf()
            }
            return Gson().fromJson(userStatus,object : TypeToken<ArrayList<UserStatus>?>() {}.type)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        binding = ActivityStatusDetailBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        setupEngagementTracking()
        source = intent.getStringExtra(Constants.SOURCE).orEmpty()
        userStatusList = intent.getParcelableArrayListExtra<UserStatus>(Constants.DATA) as ArrayList<UserStatus>
        statusAdsList = intent.getParcelableArrayListExtra<UserStatus>(Constants.AD_DATA) as ArrayList<UserStatus>? ?: arrayListOf()
        insertAdData()

        startPostion = intent.getIntExtra(Constants.ITEM_POSITION, 0)
        adapter = StatusPagerAdapter(supportFragmentManager, source, userStatusList, this)
        binding.statusPager.adapter = adapter
        binding.statusPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {
                val fragment = adapter.getItem(position)
                if(fragment is StatusDetailFragment) {
                    fragment.resumeStatus()
                }
                if(fragment is StatusAdFragment){
                    fragment.resumeStatus()
                }
            }

            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                val fragment = adapter.getItem(position)
                if(fragment is StatusDetailFragment) {
                    fragment.pauseStatus()
                }
                if(fragment is StatusAdFragment){
                    fragment.pauseStatus()
                }
            }
        })

        binding.statusPager.currentItem = startPostion

        appStateObserver = DoubtnutApp.INSTANCE.bus()?.toObservable()?.subscribe { event ->
            if (event is ApplicationStateEvent) {
                isApplicationBackground = !event.state
            }
        }

    }

    private fun insertAdData() {
        if (!statusAdsList.isNullOrEmpty() && !userStatusList.isNullOrEmpty()) {
            for (ad in statusAdsList!!) {
                val position = ad.position
                position?.let {
                    if (position > 0 && position < userStatusList.size && !userStatusList[position].type.equals("ad")) {
                        userStatusList.add(position, ad)
                    }
                }
            }
        }
    }

    override fun moveNext() {
        val position = binding.statusPager.currentItem
        if (position < userStatusList.size - 1) {
            binding.statusPager.currentItem = position + 1
        } else {
            finish()
        }
    }

    override fun movePrevious() {
        val position = binding.statusPager.currentItem
        if (position - 1 >= 0) {
            binding.statusPager.currentItem = position - 1
        } else {
            finish()
        }
    }

    override fun openStatusBottomSheet(position: Int, action: String) {
        if (position < userStatusList[binding.statusPager.currentItem].statusItem!!.size) {
            val status = userStatusList[binding.statusPager.currentItem].statusItem!![position]
            val statusId = status.getStatusId()
            DataHandler.INSTANCE.microService.get().getStoryMetaDetail(statusId, action).observe(this, {
                when (it) {
                    is Outcome.Progress -> {
                    }
                    is Outcome.ApiError -> {
                        apiErrorToast(it.e)
                    }
                    is Outcome.Failure -> {
                        apiErrorToast(it.e)
                    }
                    is Outcome.BadRequest -> {
                        showApiErrorToast(this)
                    }
                    is Outcome.Success -> {
                        var data: ArrayList<StatusMetaDetailItem>? = null
                        if (action == UserStatusActionType.LIKE) {
                            data = it.data.data.likedDetail
                            status.likeCount = data?.size ?: 0
                        } else if (action == UserStatusActionType.VIEW) {
                            data = it.data.data.viewedDetail
                            status.viewCount = data?.size ?: 0
                        }
                        if (!data.isNullOrEmpty()) {
                            val bottomSheet = StatusActionBottomFragment.newInstance(
                                data,
                                UserStatusActionType.getActionLabel(action, data.size)
                            )
                            bottomSheet.show(supportFragmentManager, "UserStatusActionBottomSheet")

                            val params = hashMapOf<String, Any>().apply {
                                put(EventConstants.TIME_STAMP, System.currentTimeMillis())
                                put(EventConstants.TYPE, action)
                            }
                            analyticsPublisher.publishEvent(
                                AnalyticsEvent(
                                    EventConstants.STATUS_BOTTOMSHEET_OPEN,
                                    params = params,
                                    ignoreFirebase = false,
                                    ignoreApxor = false,
                                    ignoreSnowplow = true
                                )
                            )
                        } else {
                            DoubtnutApp.INSTANCE.bus()?.send(StatusBottomSheetClosed())
                        }
                    }
                }
            })
        }
    }

    override fun onStop() {
        super.onStop()
        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        (timerTask?.let { handler?.removeCallbacks(it) })
        sendEventForEngagement(EventConstants.EVENT_STATUS_ENGAGEMENT, engamentTimeToSend)

        analyticsPublisher.publishEvent(
            StructuredEvent(
                action = EventConstants.EVENT_STATUS_ENGAGEMENT,
                value = engamentTimeToSend.toDouble(),
                category = EventConstants.CATEGORY_STATUS,
                eventParams = hashMapOf(
                    EventConstants.PARAM_TIMESTAMP to System.currentTimeMillis(),
                    EventConstants.SOURCE to EventConstants.SOURCE_STATUS_ENGAGEMENT_VIEW_STATUS,
                    EventConstants.PAGE to source
                )
            )
        )

        totalEngagementTime = 0
    }

    fun setupEngagementTracking() {
        handler = Handler(Looper.getMainLooper())
        startEngagementTimer()
    }

    private fun startEngagementTimer() {
        if (engageTimer == null) {
            engageTimer = Timer()
        }
        timerTask = object : TimerTask() {
            override fun run() {
                handler?.post {
                    if (!isApplicationBackground) {
                        engamentTimeToSend = totalEngagementTime
                        totalEngagementTime++
                    }
                }
            }
        }
        totalEngagementTime = 0
        engageTimer!!.schedule(timerTask, 0, 1000)
    }

    private fun sendEventForEngagement(
        @Suppress("SameParameterValue") eventName: String,
        engagementTimeToSend: Number
    ) {
        val app = DoubtnutApp.INSTANCE
        app.getEventTracker().addEventNames(eventName)
            .addNetworkState(NetworkUtils.isConnected(app.applicationContext).toString())
            .addStudentId(UserUtil.getStudentId())
            .addScreenName("StatusDetail")
            .addEventParameter(EventConstants.ENGAGEMENT_TIME, engagementTimeToSend)
            .track()
    }

    override fun onDestroy() {
        super.onDestroy()
        engageTimer?.cancel()
        engageTimer = null

        timerTask?.cancel()
        timerTask = null

        appStateObserver?.dispose()
    }
}