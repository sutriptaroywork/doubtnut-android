package com.doubtnutapp.course

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*

import com.doubtnutapp.base.MultiSelectSubjectFilterClick
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.course.viewmodel.SchedulerListingViewModel
import com.doubtnutapp.databinding.ActivitySchedulerListingBinding
import com.doubtnutapp.domain.course.entities.SchedulerListingEntity
import com.doubtnutapp.feed.tracking.ViewTrackingBus
import com.doubtnutapp.ui.answer.TagsEndlessRecyclerOnScrollListener
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter

class SchedulerListingActivity :
    ActionPerformer,
    BaseBindingActivity<SchedulerListingViewModel, ActivitySchedulerListingBinding>() {

    companion object {
        private const val TAG = "SchedulerListingActivity"
        const val COMMA_SEPARATED_SUBJECTS = "subjects"
        const val SLOT = "slot"
        private const val START_PAGE = 1

        fun getStartIntent(context: Context, commaSeparatedFilters: String?, slot: String?) =
            Intent(context, SchedulerListingActivity::class.java).apply {
                putExtra(COMMA_SEPARATED_SUBJECTS, commaSeparatedFilters)
                putExtra(SLOT, slot)
            }
    }

    private var viewTrackingBus: ViewTrackingBus? = null
    private var nextPageToLoad: Int = START_PAGE

    private val filterAdapter: WidgetLayoutAdapter by lazy {
        WidgetLayoutAdapter(
            context = this@SchedulerListingActivity,
            actionPerformer = this,
            source = TAG
        )
    }

    private val widgetLayoutAdapter: WidgetLayoutAdapter by lazy {
        WidgetLayoutAdapter(
            context = this@SchedulerListingActivity,
            actionPerformer = this,
            source = TAG
        )
    }

    private val selectFilterList = mutableSetOf<String>()

    private val commaSeparatedFilters by lazy { intent.getStringExtra(COMMA_SEPARATED_SUBJECTS) }

    private val slot by lazy { intent.getStringExtra(SLOT) }

    private val infiniteScrollListener: TagsEndlessRecyclerOnScrollListener by lazy {
        object : TagsEndlessRecyclerOnScrollListener(binding.rvWidget.layoutManager) {
            override fun onLoadMore(currentPage: Int) {
                fetchData()
            }
        }
    }

    override fun providePageName(): String = TAG

    override fun provideViewBinding(): ActivitySchedulerListingBinding =
        ActivitySchedulerListingBinding.inflate(layoutInflater)

    override fun provideViewModel(): SchedulerListingViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(savedInstanceState: Bundle?) {
        binding.ivBack.setOnClickListener {
            onBackPressed()
        }
        selectFilterList.addAll(
            commaSeparatedFilters?.split(",")?.toMutableSet() ?: mutableSetOf()
        )
        setUpFiltersRecyclerView()
        setUpWidgetsRecyclerView()
        registerViewTracking()
        fetchData()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewTrackingBus?.unsubscribe()
    }

    private fun setUpFiltersRecyclerView() {
        binding.rvFilterWidgets.hide()
        binding.rvFilterWidgets.adapter = filterAdapter
    }

    private fun setUpWidgetsRecyclerView() {
        binding.rvWidget.adapter = widgetLayoutAdapter
        binding.rvWidget.clearOnScrollListeners()
        binding.rvWidget.addOnScrollListener(infiniteScrollListener)
    }

    private fun fetchData() {
        viewModel.getSchedulerListing(
            selectFilterIdsList = selectFilterList,
            slot = slot,
            page = nextPageToLoad
        )
    }

    private fun registerViewTracking() {
        viewTrackingBus = ViewTrackingBus({
            viewModel.trackView(it)
        }, {})
        widgetLayoutAdapter.registerViewTracking(viewTrackingBus!!)
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.schedulerListingData.observeK(
            this,
            ::onSuccess,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgress
        )
    }

    override fun performAction(action: Any) {
        when (action) {
            is MultiSelectSubjectFilterClick -> {
                if (action.isSelected) {
                    selectFilterList.add(action.filterId)
                } else {
                    selectFilterList.remove(action.filterId)
                }
                nextPageToLoad = START_PAGE
                infiniteScrollListener.isLastPageReached = false
                fetchData()
            }
            else -> {}
        }
    }

    private fun onSuccess(data: SchedulerListingEntity) {
        if (nextPageToLoad == START_PAGE) {
            widgetLayoutAdapter.clearData()
        }
        infiniteScrollListener.setDataLoading(false)
        nextPageToLoad++
        infiniteScrollListener.isLastPageReached = data.widgets.isEmpty()
        binding.tvTitle.text = data.title
        widgetLayoutAdapter.addWidgets(data.widgets)
        if (data.filterWidgets.isNullOrEmpty().not()) {
            binding.rvFilterWidgets.show()
            filterAdapter.setWidgets(data.filterWidgets)
        }
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(supportFragmentManager, "BadRequestDialog")
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun ioExceptionHandler() {
        if (NetworkUtils.isConnected(this)) {
            toast(getString(R.string.somethingWentWrong))
        } else {
            toast(getString(R.string.string_noInternetConnection))
        }
    }

    private fun updateProgress(state: Boolean) {
        binding.progressBar.setVisibleState(state)
        infiniteScrollListener.setDataLoading(state)
    }
}