package com.doubtnutapp.liveclass.ui


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager.widget.ViewPager
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.ApplicationStateEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnutapp.course.widgets.StoryWidgetItem
import com.doubtnutapp.liveclass.adapter.StoryPagerAdapter
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.android.AndroidInjection
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_status_detail.*
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class StoryDetailActivity : AppCompatActivity(), StoryDetailFragment.OnStatusListPositionChangeListener {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    lateinit var userStatusList: ArrayList<StoryWidgetItem>
    lateinit var adapter: StoryPagerAdapter

    private var startPostion = 0

    private var appStateObserver: Disposable? = null

    private var totalEngagementTime: Int = 0
    private var engamentTimeToSend: Number = 0
    private var timerTask: TimerTask? = null
    private var engageTimer: Timer? = null
    private var handler: Handler? = null
    var isApplicationBackground = false
    private var source: String = EventConstants.STATUS_SOURCE_HEADER


    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>

    companion object {
        fun getStartIntent(context: Context, source: String, storyList: ArrayList<StoryWidgetItem>, startPosition: Int) =
                Intent(context, StoryDetailActivity::class.java).apply {
                    putExtra(Constants.SOURCE, source)
                    putParcelableArrayListExtra(Constants.DATA, storyList)
                    putExtra(Constants.ITEM_POSITION, startPosition)
                }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_status_detail)
        setupEngagementTracking()
        source = intent.getStringExtra(Constants.SOURCE).orEmpty()
        userStatusList = intent.getParcelableArrayListExtra<StoryWidgetItem>(Constants.DATA) as ArrayList<StoryWidgetItem>
        startPostion = intent.getIntExtra(Constants.ITEM_POSITION, 0)
        adapter = StoryPagerAdapter(supportFragmentManager, source, userStatusList, this)
        statusPager.adapter = adapter
        statusPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {
                (adapter.getItem(position) as StoryDetailFragment).resumeStatus()
            }

            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                (adapter.getItem(position) as StoryDetailFragment).pauseStatus()
            }
        })

        statusPager.currentItem = startPostion

        appStateObserver = DoubtnutApp.INSTANCE.bus()?.toObservable()?.subscribe { event ->
            if (event is ApplicationStateEvent) {
                isApplicationBackground = !event.state
            }
        }


    }

    override fun moveNext() {
        val position = statusPager.currentItem
        if (position < userStatusList.size - 1) {
            statusPager.currentItem = position + 1;
        } else {
            finish()
        }
    }

    override fun movePrevious() {
        val position = statusPager.currentItem
        if (position - 1 >= 0) {
            statusPager.currentItem = position - 1;
        } else {
            finish()
        }
    }

    override fun openStatusBottomSheet(position: Int, action: String) {
        //todo check
    }

    /*  override fun openStatusBottomSheet(position: Int, action: String) {
          val status = userStatusList[statusPager.currentItem].statusItem!![position]
          var statusId = status.id
          DataHandler.INSTANCE.microService.getStoryMetaDetail(statusId, action).observe(this, Observer {
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
                          val bottomSheet = StatusActionBottomFragment.newInstance(data!!, UserStatusActionType.getActionLabel(action, data.size))
                          bottomSheet.show(supportFragmentManager, "UserStatusActionBottomSheet")

                          val params = hashMapOf<String, Any>().apply {
                              put(EventConstants.TIME_STAMP, System.currentTimeMillis())
                              put(EventConstants.TYPE, action)
                          }
                          analyticsPublisher.publishEvent(StructuredEvent(EventConstants.CATEGORY_STATUS,
                                  EventConstants.STATUS_BOTTOMSHEET_OPEN,
                                  eventParams = params)
                          )
                          analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.STATUS_BOTTOMSHEET_OPEN, params = params, ignoreFirebase = false, ignoreApxor = false, ignoreSnowplow = true))
                      } else {
                          DoubtnutApp.INSTANCE.bus()?.send(StatusBottomSheetClosed())
                      }
                  }
              }
          })

      }
  */
    override fun onStop() {
        super.onStop()
        timerTask?.let { handler?.removeCallbacks(it) }
        sendEventForEngagement(EventConstants.EVENT_STATUS_ENGAGEMENT, engamentTimeToSend)

        analyticsPublisher.publishEvent(StructuredEvent(action = EventConstants.EVENT_STATUS_ENGAGEMENT,
                value = engamentTimeToSend.toDouble(),
                category = EventConstants.CATEGORY_STATUS,
                eventParams = hashMapOf(
                        EventConstants.PARAM_TIMESTAMP to System.currentTimeMillis(),
                        EventConstants.SOURCE to EventConstants.SOURCE_STATUS_ENGAGEMENT_VIEW_STATUS,
                        EventConstants.PAGE to source
                )))

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

    private fun sendEventForEngagement(eventName: String, engagementTimeToSend: Number) {

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