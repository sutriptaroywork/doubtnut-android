package com.doubtnutapp.quiztfs.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.base.FilterSelectAction
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.data.remote.models.quiztfs.AnalysisData
import com.doubtnutapp.databinding.ActivityQuizTfsAnalysisBinding
import com.doubtnutapp.quiztfs.viewmodel.QuizTfsAnalysisViewModel
import com.doubtnutapp.setVisibleState
import com.doubtnutapp.ui.answer.TagsEndlessRecyclerOnScrollListener
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import javax.inject.Inject

class QuizTfsAnalysisActivity :
    BaseBindingActivity<QuizTfsAnalysisViewModel, ActivityQuizTfsAnalysisBinding>(),
    ActionPerformer {

    companion object {
        private const val TAG = "QuizTfsAnalysisActivity"

        fun getStartIntent(context: Context, source: String, date: String) =
            Intent(context, QuizTfsAnalysisActivity::class.java).apply {
                putExtra(Constants.SOURCE, source)
                putExtra(Constants.DATE, date)
            }
    }

    private lateinit var adapter: WidgetLayoutAdapter
    private lateinit var infiniteScrollListener: TagsEndlessRecyclerOnScrollListener
    private var date: String? = null

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private var filter: String? = null

    override fun providePageName(): String = TAG

    override fun provideViewBinding(): ActivityQuizTfsAnalysisBinding =
        ActivityQuizTfsAnalysisBinding.inflate(layoutInflater)

    override fun provideViewModel(): QuizTfsAnalysisViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(savedInstanceState: Bundle?) {

        date = intent?.getStringExtra(Constants.DATE)
        binding.ivBack.setOnClickListener {
            goBack()
        }
        setupRecyclerView()
    }

    override fun performAction(action: Any) {
        if (action is FilterSelectAction) {
            if (action.type == "subject") {
                filter = action.filterText
                initiateRecyclerListenerAndFetchInitialData()
            }
        }
    }

    override fun setupObservers() {
        super.setupObservers()

        viewModel.analysisData.observeK(
            this,
            ::onSuccess,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgress
        )
    }

    private fun onSuccess(analysisData: AnalysisData) {
        setData(analysisData)
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

    private fun setData(analysisData: AnalysisData) {
        binding.pageTitle.text = analysisData.pageTitle
        if (analysisData.widgets.isNullOrEmpty()) {
            infiniteScrollListener.isLastPageReached = true
        } else {
            adapter.addWidgets(analysisData.widgets)
        }
    }

    private fun initiateRecyclerListenerAndFetchInitialData() {
        adapter.clearData()
        val startPage = 1
        binding.rvWidgets.clearOnScrollListeners()
        infiniteScrollListener =
            object : TagsEndlessRecyclerOnScrollListener(binding.rvWidgets.layoutManager) {
                override fun onLoadMore(currentPage: Int) {
                    fetchList(currentPage, date)
                }
            }.also {
                it.setStartPage(startPage)
            }
        binding.rvWidgets.addOnScrollListener(infiniteScrollListener)
        fetchList(startPage, date)
    }

    private fun fetchList(page: Int, date: String?) {
        viewModel.fetchAnalysisData(page, date, filter)
    }

    private fun setupRecyclerView() {
        adapter = WidgetLayoutAdapter(this, this)
        binding.rvWidgets.layoutManager = LinearLayoutManager(this)
        binding.rvWidgets.adapter = adapter

        initiateRecyclerListenerAndFetchInitialData()
    }
}