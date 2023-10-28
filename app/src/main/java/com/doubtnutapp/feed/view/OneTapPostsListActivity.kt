package com.doubtnutapp.feed.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.EventBus.OneTapPostCreatedEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.base.OnAutoPostItemSelected
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.databinding.ActivityOneTapPostsListBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.feed.viewmodel.OneTapPostsListViewModel
import com.doubtnutapp.isNotNullAndNotEmpty
import com.doubtnutapp.ui.answer.TagsEndlessRecyclerOnScrollListener
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.utils.showApiErrorToast
import com.doubtnutapp.utils.showToast
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import javax.inject.Inject

class OneTapPostsListActivity :
    BaseBindingActivity<OneTapPostsListViewModel, ActivityOneTapPostsListBinding>(),
    ActionPerformer {

    companion object {
        const val TAG = "OneTapPostsActivity"
        const val EVENT_TAG_ONE_TAP_POSTS_LIST = "one_tap_posts_list"
        const val EVENT_TAG_ONE_TAP_POST = "one_tap_post_selected"
        const val START_PAGE_NO = 0
        const val PAGE_THRESHOLD = 10
        const val CAROUSEL_TYPE = "carousel_type"

        fun getStartIntent(context: Context, carouselType: String?) =
            Intent(context, OneTapPostsListActivity::class.java)
                .apply {
                    putExtra(CAROUSEL_TYPE, carouselType)
                }

    }

    // to prevent multiple click of post at one time
    private var isPostItemClickable = true

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    //page count for pagination
    var page = 0;

    private val carouselType: String? by lazy {
        intent.getStringExtra(CAROUSEL_TYPE)
    }

    val widgetLayoutAdapter: WidgetLayoutAdapter by lazy {
        WidgetLayoutAdapter(this@OneTapPostsListActivity, this, TAG)
    }

    val infiniteScrollListener: TagsEndlessRecyclerOnScrollListener by lazy {
        object : TagsEndlessRecyclerOnScrollListener(binding.rvOneTapPosts.layoutManager) {
            override fun onLoadMore(current_page: Int) {
                viewModel.getOneTapPosts(page.toString(), carouselType)
            }

        }
    }

    override fun provideViewBinding(): ActivityOneTapPostsListBinding {
        return ActivityOneTapPostsListBinding.inflate(layoutInflater, null, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): OneTapPostsListViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        binding.rvOneTapPosts.setHasFixedSize(true)
        binding.rvOneTapPosts.layoutManager = LinearLayoutManager(this)
        binding.rvOneTapPosts.adapter = widgetLayoutAdapter

        infiniteScrollListener.setStartPage(START_PAGE_NO)
        infiniteScrollListener.setVisibleThreshold(PAGE_THRESHOLD)
        binding.rvOneTapPosts.addOnScrollListener(infiniteScrollListener)

        viewModel.getOneTapPosts(START_PAGE_NO.toString(), carouselType)

        binding.shimmerLayoutProgress.startShimmer()
        binding.shimmerLayoutProgress.visibility = View.VISIBLE

        analyticsPublisher.publishEvent(
            AnalyticsEvent("${EVENT_TAG_ONE_TAP_POSTS_LIST}_${EventConstants.PAGE_OPENED}")
        )

        binding.imageBack.setOnClickListener {
            finish()
        }

    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.liveDataPosts.observe(this, Observer {
            when (it) {
                is Outcome.Success -> {
                    stopShimmer()
                    binding.textViewToolbarTitle.text = it.data.title
                    if (it.data.page == null || it.data.widgets.isNullOrEmpty()) {
                        infiniteScrollListener.isLastPageReached = true
                        return@Observer
                    } else {
                        page = it.data.page!!
                        widgetLayoutAdapter.addWidgets(it.data.widgets!!)
                        infiniteScrollListener.setDataLoading(false)
                    }
                }
                is Outcome.Progress -> {
                    // show circular progress only from 2nd page
                    if (page > 0) {
                        showCircularProgress(it.loading)
                    }
                }
                is Outcome.ApiError -> {
                    apiErrorToast(it.e)
                }
                else -> {
                    showApiErrorToast(this@OneTapPostsListActivity)
                }

            }
        })

        viewModel.liveDataCreateOneTapPost.observe(this, Observer {
            when (it) {
                is Outcome.Success -> {
                    it.data.data?.let { baseResponse ->
                        deeplinkAction.performAction(
                            this@OneTapPostsListActivity,
                            baseResponse.deeplink
                        )

                        if (baseResponse.message.isNotNullAndNotEmpty()) {
                            showToast(this@OneTapPostsListActivity, baseResponse.message!!)
                        }

                        isPostItemClickable = true

                        DoubtnutApp.INSTANCE.bus()?.send(OneTapPostCreatedEvent())
                    }

                }
                is Outcome.Progress -> {
                    showCircularProgress(it.loading)
                }
                is Outcome.ApiError -> {
                    apiErrorToast(it.e)
                    isPostItemClickable = true
                }
                else -> {
                    showApiErrorToast(this@OneTapPostsListActivity)
                }

            }
        })
    }

    private fun stopShimmer() {
        if (binding.shimmerLayoutProgress.isShimmerStarted) {
            binding.shimmerLayoutProgress.visibility = View.GONE
            binding.shimmerLayoutProgress.stopShimmer()
        }
    }

    private fun showCircularProgress(state: Boolean) {
        if (state) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }

    override fun performAction(action: Any) {

        if (action is OnAutoPostItemSelected && isPostItemClickable) {
            viewModel.createOneTapPost(action.id)
            isPostItemClickable = false

            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EVENT_TAG_ONE_TAP_POST,
                    hashMapOf<String, Any>(
                        EventConstants.SOURCE to TAG,
                        EventConstants.ID to action.id
                    )
                )
            )
        }

    }

}