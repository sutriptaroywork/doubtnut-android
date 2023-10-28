package com.doubtnutapp.teacherchannel

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.*
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.Widgets
import com.doubtnutapp.databinding.ActivityTeacherChannelBinding
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.teacherchannel.viewmodel.TeacherChannelViewModel
import com.doubtnutapp.ui.answer.TagsEndlessRecyclerOnScrollListener
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.widgetmanager.WidgetTypes
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import javax.inject.Inject

class TeacherChannelActivity :
    BaseBindingActivity<TeacherChannelViewModel, ActivityTeacherChannelBinding>(),
    ActionPerformer {

    companion object {
        val TAG = "TeacherChannelActivity"
        fun getStartIntent(
            context: Context, teacherId: Int,teacherType: String?,
            tabFilter: String?, subFilter: String?, contentFilter: String?, source: String
        ) =
            Intent(context, TeacherChannelActivity::class.java).apply {
                putExtra(Constants.TEACHER_ID, teacherId)
                putExtra(Constants.TEACHER_TYPE, teacherType)
                putExtra(Constants.SOURCE, source)
                putExtra(Constants.TAB_FILTER, tabFilter)
                putExtra(Constants.SUB_FILTER, subFilter)
                putExtra(Constants.CONTENT_FILTER, contentFilter)
            }
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private var isSubscribtionStateChanged = false

    private lateinit var headerAdapter: WidgetLayoutAdapter
    private lateinit var adapter: WidgetLayoutAdapter

    private var scrollListener: TagsEndlessRecyclerOnScrollListener? = null

    override fun provideViewBinding(): ActivityTeacherChannelBinding {
        return ActivityTeacherChannelBinding.inflate(layoutInflater)
    }

    override fun providePageName() = TAG

    override fun provideViewModel(): TeacherChannelViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        viewModel.initDataFromIntentExtra(intent?.extras)

        val eventParams = hashMapOf<String, Any>()
        eventParams[Constants.SOURCE] = viewModel.source
        eventParams[Constants.TEACHER_ID] = viewModel.teacherId
        analyticsPublisher?.publishEvent(
            AnalyticsEvent(
                EventConstants.TEACHER_PAGE_OPEN,
                eventParams
            )
        )

        adapter = WidgetLayoutAdapter(this, this, "teacher_channel_page")
        binding.rvWidget.layoutManager = LinearLayoutManager(this)
        binding.rvWidget.adapter = adapter
        scrollListener =
            object : TagsEndlessRecyclerOnScrollListener(binding.rvWidget.layoutManager) {
                override fun onLoadMore(current_page: Int) {
                    viewModel.fetchTeacherPageData()
                    viewModel.currentPage = current_page
                }
            }
        binding.rvWidget.addOnScrollListener(scrollListener!!)
        scrollListener?.setStartPage(1)


        headerAdapter = WidgetLayoutAdapter(this, this, "teacher_channel_page")
        binding.rvHeaderWidget.layoutManager = LinearLayoutManager(this)
        binding.rvHeaderWidget.adapter = headerAdapter

        viewModel.fetchTeacherPageData()

        binding.ivBack.setOnClickListener {
            onBackPressed()
        }

    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.channelPageData.observe(this, {
            when (it) {
                is Outcome.Progress -> {
                    scrollListener?.setDataLoading(true)
                    binding.progress.show()
                }
                is Outcome.Success -> {
                    scrollListener?.setDataLoading(false)
                    binding.progress.hide()
                    onWidgetListFetched(it.data)
                }
                is Outcome.Failure -> {
                    scrollListener?.setDataLoading(false)
                    binding.progress.hide()
                    apiErrorToast(it.e)
                }
                is Outcome.ApiError -> {
                    scrollListener?.setDataLoading(false)
                    binding.progress.hide()
                    apiErrorToast(it.e)
                }
            }
        })

        viewModel.eventLiveData.observe(this, {
            it?.getContentIfNotHandled()?.let {
                if (it is DownloadFailed) {
                    toast(it.message ?: "Download failed", Toast.LENGTH_LONG)
                }
            }
        })
    }

    private fun onWidgetListFetched(data: Widgets) {
        if (data.widgets.isNullOrEmpty()) {
            scrollListener?.isLastPageReached = true
            return
        }

        binding.rvWidget.show()
        if (scrollListener?.currentPage == 1) {
            if (data.widgets.isNullOrEmpty() || !hasContent(data.widgets)) {
                binding.tvNoContent.show()
            } else {
                binding.tvNoContent.hide()
            }
            val stickyWidgets = arrayListOf<WidgetEntityModel<*, *>>()
            val notStickyWidget = arrayListOf<WidgetEntityModel<*, *>>()
            for (widget in data.widgets) {
                when (widget.type) {
                    WidgetTypes.TYPE_TEACHER_HEADER -> {
                        stickyWidgets.add(widget)
                    }
                    else -> {
                        notStickyWidget.add(widget)
                    }
                }
                headerAdapter.setWidgets(stickyWidgets)
                adapter.setWidgets(notStickyWidget)
            }
        } else {
            adapter.addWidgets(data.widgets)
        }
    }

    private fun hasContent(widgets: List<WidgetEntityModel<*, *>>): Boolean {
        var hasContent = false
        for (widget in widgets) {
            when (widget.type) {
                WidgetTypes.TYPE_TEACHER_HEADER, WidgetTypes.TYPE_CHANNEL_FILTER,
                WidgetTypes.TYPE_CHANNEL_FILTER_TAB, WidgetTypes.TYPE_CHANNEL_FILTER_CONTENT -> {
                }
                else -> {
                    hasContent = true
                }
            }
        }
        return hasContent
    }

    private fun showProfileBottomSheet(channelHeading: String?) {
        supportFragmentManager.beginTransaction().apply {
            replace(
                R.id.fragmentContainer,
                TeacherProfileBottomsheetFragment.newInstance(
                    channelHeading,
                    viewModel.teacherId
                ),
                TeacherProfileBottomsheetFragment.TAG
            ).addToBackStack(TeacherProfileBottomsheetFragment.TAG)
            commitAllowingStateLoss()
        }

    }

    fun closeProfileBottomSheet() {
        val fragment =
            supportFragmentManager.findFragmentByTag(TeacherProfileBottomsheetFragment.TAG)
        if (fragment != null) {
            supportFragmentManager!!.beginTransaction().apply {
                remove(fragment)
                commitAllowingStateLoss()
            }
        }
    }

    override fun performAction(action: Any) {
        when (action) {
            is ShowTeacherProfile -> {
                showProfileBottomSheet(action.channelHeading)
            }
            is ChannelTabFilterSelected -> {
                val eventParams = hashMapOf<String, Any>()
                eventParams[EventConstants.TAB_NAME] = action.filter.value
                eventParams[Constants.TEACHER_ID] = viewModel.teacherId
                analyticsPublisher?.publishEvent(
                    AnalyticsEvent(
                        EventConstants.TEACHER_PAGE_TAB_CLICKED,
                        eventParams
                    )
                )
                viewModel.updateTabFilter(action.filter.key)
                onFilterUpdate()
            }
            is ChannelSubTabFilterSelected -> {
                val eventParams = hashMapOf<String, Any>()
                eventParams[EventConstants.TAB_NAME] = action.filter.value
                eventParams[Constants.TEACHER_ID] = viewModel.teacherId
                analyticsPublisher?.publishEvent(
                    AnalyticsEvent(
                        EventConstants.TEACHER_PAGE_SUBTAB_FILTER_CLICKED,
                        eventParams,
                        ignoreSnowplow = true
                    )
                )
                viewModel.updateSubFilter(action.filter.key)
                onFilterUpdate()
            }
            is ChannelContentFilterSelected -> {
                val eventParams = hashMapOf<String, Any>()
                eventParams[EventConstants.TAB_NAME] = action.filter.value
                eventParams[Constants.TEACHER_ID] = viewModel.teacherId
                analyticsPublisher?.publishEvent(
                    AnalyticsEvent(
                        EventConstants.TEACHER_PAGE_CONTENT_TYPE_FILTER_CLICKED,
                        eventParams
                    )
                )
                viewModel.updateContentFilter(action.filter.key)
                onFilterUpdate()
            }
            is DownloadPDF -> {
                viewModel.downloadPdf(action.url)
            }
            is SubscribeChannel -> {
                isSubscribtionStateChanged = !isSubscribtionStateChanged
            }
        }
    }

    private fun onFilterUpdate() {
        scrollListener?.setStartPage(1)
        scrollListener?.setDataLoading(true)
        scrollListener?.isLastPageReached = false
    }

    override fun onBackPressed() {
        if (isSubscribtionStateChanged) {
            DoubtnutApp.INSTANCE.bus()?.send(RefreshUI())
        }
        super.onBackPressed()
    }
}