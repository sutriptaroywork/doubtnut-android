package com.doubtnutapp.examcorner

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.databinding.ActivityExamCornerBookmarkBinding
import com.doubtnutapp.examcorner.model.ApiExamCornerBookmarkData
import com.doubtnutapp.examcorner.viewmodel.ExamCornerBookmarkViewModel
import com.doubtnutapp.ui.answer.TagsEndlessRecyclerOnScrollListener
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import javax.inject.Inject

class ExamCornerBookmarkActivity :
    BaseBindingActivity<ExamCornerBookmarkViewModel, ActivityExamCornerBookmarkBinding>() {

    companion object {
        private const val TAG = "ExamCornerBookmarkActivity"
        fun getStartIntent(context: Context) =
            Intent(context, ExamCornerBookmarkActivity::class.java)
    }

    override fun provideViewBinding(): ActivityExamCornerBookmarkBinding =
        ActivityExamCornerBookmarkBinding.inflate(layoutInflater)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): ExamCornerBookmarkViewModel =
        viewModelProvider(viewModelFactory)

    private lateinit var adapter: WidgetLayoutAdapter
    private lateinit var infiniteScrollListener: TagsEndlessRecyclerOnScrollListener

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun setupView(savedInstanceState: Bundle?) {
        adapter = WidgetLayoutAdapter(this)
        binding.rvWidgets.layoutManager = LinearLayoutManager(this)
        binding.rvWidgets.adapter = adapter
        binding.ivBack.setOnClickListener {
            goBack()
        }
        viewModel.extraParams.apply {
            put(EventConstants.EVENT_SCREEN_PREFIX + EventConstants.NAME, TAG)
        }
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.EXAM_CORNER_BOOKMARK_SCREEN_VIEW,
                hashMapOf<String, Any>().apply {
                    putAll(viewModel.extraParams)
                },
                ignoreSnowplow = true
            )
        )
        initiateRecyclerListenerAndFetchInitialData()
    }

    private fun initiateRecyclerListenerAndFetchInitialData() {
        adapter.clearData()
        val startPage = 1
        binding.rvWidgets.clearOnScrollListeners()
        infiniteScrollListener =
            object : TagsEndlessRecyclerOnScrollListener(binding.rvWidgets.layoutManager) {
                override fun onLoadMore(currentPage: Int) {
                    fetchList(currentPage)
                }
            }.also {
                it.setStartPage(startPage)
            }
        binding.rvWidgets.addOnScrollListener(infiniteScrollListener)
        fetchList(startPage)
    }

    private fun fetchList(page: Int) {
        viewModel.fetchExamCornerBookmarkData(page)
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.examCornerLiveData.observeK(
            this,
            this::onWidgetListFetched,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgress
        )
    }

    private fun onWidgetListFetched(data: ApiExamCornerBookmarkData) {
        if (data.widgets.isNullOrEmpty()) {
            infiniteScrollListener.isLastPageReached = true
        } else {
            adapter.addWidgets(data.widgets)
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
        infiniteScrollListener.setDataLoading(state)
        binding.progressBar.setVisibleState(state)
    }
}
