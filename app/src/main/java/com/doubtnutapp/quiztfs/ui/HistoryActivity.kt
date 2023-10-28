package com.doubtnutapp.quiztfs.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.data.remote.models.quiztfs.HistoryData
import com.doubtnutapp.databinding.ActivityHistoryBinding
import com.doubtnutapp.quiztfs.viewmodel.HistoryViewModel
import com.doubtnutapp.setVisibleState
import com.doubtnutapp.ui.answer.TagsEndlessRecyclerOnScrollListener
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import javax.inject.Inject

class HistoryActivity : BaseBindingActivity<HistoryViewModel, ActivityHistoryBinding>() {

    companion object {
        private const val TAG = "HistoryActivity"
        fun getStartIntent(context: Context, source: String) =
            Intent(context, HistoryActivity::class.java).apply {
                putExtra(Constants.SOURCE, source)
            }
    }

    private lateinit var adapter: WidgetLayoutAdapter
    private lateinit var infiniteScrollListener: TagsEndlessRecyclerOnScrollListener

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun provideViewBinding(): ActivityHistoryBinding =
        ActivityHistoryBinding.inflate(layoutInflater)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): HistoryViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(savedInstanceState: Bundle?) {
        binding.ivBack.setOnClickListener {
            goBack()
        }
        setupRecyclerView()
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.history.observeK(
            this,
            ::onSuccess,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgress
        )
    }

    private fun onSuccess(historyData: HistoryData) {
        setData(historyData)
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(supportFragmentManager, "BadRequestDialog")
    }

    private fun ioExceptionHandler() {
        if (NetworkUtils.isConnected(this).not()) {
            toast(getString(R.string.string_noInternetConnection))
        } else {
            toast(getString(R.string.somethingWentWrong))
        }
    }

    private fun updateProgress(state: Boolean) {
        infiniteScrollListener.setDataLoading(state)
        binding.progressBar.setVisibleState(state)
    }

    private fun setData(historyData: HistoryData) {
        binding.pageTitle.text = historyData.pageTitle
        if (historyData.widgets.isNullOrEmpty()) {
            infiniteScrollListener.isLastPageReached = true
        } else {
            adapter.addWidgets(historyData.widgets)
        }
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
        viewModel.fetchHistory(page)
    }

    private fun setupRecyclerView() {
        adapter = WidgetLayoutAdapter(this)
        binding.rvWidgets.layoutManager = LinearLayoutManager(this)
        binding.rvWidgets.adapter = adapter

        initiateRecyclerListenerAndFetchInitialData()
    }
}