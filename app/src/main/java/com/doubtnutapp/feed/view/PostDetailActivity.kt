package com.doubtnutapp.feed.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.databinding.ActivityPostDetailBinding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.feed.tracking.ViewTrackingBus
import com.doubtnutapp.feed.viewmodel.FeedViewModel
import com.doubtnutapp.hide
import com.doubtnutapp.matchquestion.ui.fragment.MatchQuestionFragment
import com.doubtnutapp.rvexoplayer.RecyclerViewExoPlayerHelper
import com.doubtnutapp.rvexoplayer.RvPlayStrategy
import com.doubtnutapp.show
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.utils.showApiErrorToast
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import javax.inject.Inject
import kotlin.math.absoluteValue

class PostDetailActivity : BaseBindingActivity<DummyViewModel, ActivityPostDetailBinding>() {

    companion object {
        const val TAG = "PostDetailActivity"

        fun getStartIntent(context: Context, postId: String, start: Boolean = false): Intent {
            return Intent(context, PostDetailActivity::class.java).apply {
                putExtra(Constants.POST_ID, postId)
            }.also {
                if (start)
                    context.startActivity(it)
            }
        }
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private var viewTrackingBus: ViewTrackingBus? = null

    var rVExoPlayerHelper: RecyclerViewExoPlayerHelper? = null

    override fun provideViewBinding(): ActivityPostDetailBinding {
        return ActivityPostDetailBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): DummyViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun getStatusBarColor(): Int {
        return R.color.redTomato
    }

    override fun setupView(savedInstanceState: Bundle?) {
        val postId = intent.getStringExtra(Constants.POST_ID)

        val adapter = FeedAdapter(supportFragmentManager, true, source = "post_detail")

        binding.rvFeed.layoutManager = LinearLayoutManager(this)
        binding.rvFeed.adapter = adapter

        viewTrackingBus = ViewTrackingBus(
            onSuccess = { trackView(it) },
            onError = { }
        )

        adapter.registerViewTracking(viewTrackingBus!!)

        rVExoPlayerHelper = RecyclerViewExoPlayerHelper(
            mContext = this,
            id = R.id.rvPlayer,
            autoPlay = true,
            autoPlayInitiation = 100L,
            playStrategy = RvPlayStrategy.DEFAULT,
            defaultMute = false,
            loop = 0,
            useController = true,
            progressRequired = true,
            defaultMinBufferMs = MatchQuestionFragment.DEFAULT_MIN_BUFFER_MS,
            defaultMaxBufferMs = MatchQuestionFragment.DEFAULT_MAX_BUFFER_MS,
            reBufferDuration = 0 // For autoplay no need to buffer extra bytes
        ).apply {
            getPlayerView()?.apply {
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
            }
            makeLifeCycleAware(this@PostDetailActivity)
        }
        rVExoPlayerHelper?.attachToRecyclerView(binding.rvFeed)

        binding.rvFeed.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy.absoluteValue > 150) viewTrackingBus?.pause()
                else viewTrackingBus?.resume()
            }
        })


        DataHandler.INSTANCE.teslaRepository.getPost(postId.orEmpty()).observe(this) {
            when (it) {
                is Outcome.Progress -> {
                    binding.progressBar.show()
                }
                is Outcome.ApiError -> {
                    binding.progressBar.hide()
                    showApiErrorToast(this)
                }
                is Outcome.Failure -> {
                    binding.progressBar.hide()
                    showApiErrorToast(this)
                }
                is Outcome.BadRequest -> {
                    binding.progressBar.hide()
                    showApiErrorToast(this)
                }
                is Outcome.Success -> {
                    binding.progressBar.hide()
                    if (it.data.data != null) {
                        adapter.addItem(it.data.data, 0)
                        rVExoPlayerHelper?.playCurrent(binding.rvFeed)
                    } else {
                        toast("Post not found")
                        finish()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        rVExoPlayerHelper?.rvPlayerHelper?.resumePlayer()
    }

    fun trackView(state: ViewTrackingBus.State) {
        when (state.state) {
            ViewTrackingBus.VIEW_ADDED -> {
                eventWith(
                    StructuredEvent(
                        EventConstants.CATEGORY_FEED, EventConstants.EVENT_NAME_FEED_POST_SHOWN,
                        label = state.trackId,
                        property = state.position.toString(),
                        eventParams = state.trackParams
                    )
                )

            }
            ViewTrackingBus.VIEW_REMOVED -> {
                eventWith(
                    StructuredEvent(
                        EventConstants.CATEGORY_FEED, EventConstants.EVENT_NAME_FEED_POST_HIDDEN,
                        label = state.trackId,
                        property = state.position.toString(),
                        eventParams = state.trackParams
                    )
                )

            }
            ViewTrackingBus.TRACK_VIEW_DURATION -> {
                eventWith(
                    StructuredEvent(
                        EventConstants.CATEGORY_FEED,
                        EventConstants.EVENT_NAME_FEED_POST_IMPRESSION,
                        label = state.trackId,
                        property = state.position.toString(),
                        value = state.time.toDouble(),
                        eventParams = state.trackParams
                    )
                )
            }
        }
    }

    fun eventWith(snowplowEvent: StructuredEvent) {
        analyticsPublisher.publishEvent(snowplowEvent.apply {
            if (!eventParams.containsKey(FeedViewModel.SOURCE)) {
                eventParams[FeedViewModel.SOURCE] = TAG
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        viewTrackingBus?.unsubscribe()
    }

}