package com.doubtnutapp.liveclass.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.base.OnHomeWorkListClicked
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.data.remote.models.HomeWorkListResponse
import com.doubtnutapp.databinding.ActivityHomeWorkListBinding
import com.doubtnutapp.liveclass.viewmodel.HomeworkViewModel
import com.doubtnutapp.setVisibleState
import com.doubtnutapp.ui.answer.TagsEndlessRecyclerOnScrollListener
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import javax.inject.Inject

class MyHomeWorkActivity : BaseBindingActivity<HomeworkViewModel, ActivityHomeWorkListBinding>(),
    ActionPerformer {

    companion object {
        const val TAG = "MyHomeWorkActivity"

        fun startActivity(context: Context, start: Boolean = true): Intent {
            return Intent(context, MyHomeWorkActivity::class.java).also {
                if (start) context.startActivity(it)
            }
        }
    }

    private lateinit var adapter: WidgetLayoutAdapter
    private lateinit var infiniteScrollListener: TagsEndlessRecyclerOnScrollListener

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun provideViewBinding(): ActivityHomeWorkListBinding {
        return ActivityHomeWorkListBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): HomeworkViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun getStatusBarColor(): Int {
        return R.color.colorSecondaryDark
    }

    override fun setupView(savedInstanceState: Bundle?) {
        init()
        setUpObserver()
        initiateRecyclerListenerAndFetchInitialData()
    }

    private fun initiateRecyclerListenerAndFetchInitialData() {
        val startPage = 1
        binding.rvWidgets.clearOnScrollListeners()
        infiniteScrollListener =
            object : TagsEndlessRecyclerOnScrollListener(binding.rvWidgets.layoutManager) {
                override fun onLoadMore(currentPage: Int) {
                    viewModel.getHomeworkList(currentPage)
                }
            }.also {
                it.setStartPage(startPage)
            }

        binding.rvWidgets.addOnScrollListener(infiniteScrollListener)
        viewModel.getHomeworkList(startPage)
    }

    private fun setUpObserver() {
        viewModel.homeworkListLiveData.observeK(
            this,
            ::onHomeWorkListSuccess,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgressBarState
        )
    }

    private fun init() {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.HW_LIST_VIEW,
                hashMapOf(EventConstants.STUDENT_ID to UserUtil.getStudentId()),
                ignoreSnowplow = true
            )
        )
        viewModel = viewModelProvider(viewModelFactory)
        adapter = WidgetLayoutAdapter(this, this)
        binding.rvWidgets.layoutManager = LinearLayoutManager(this)
        binding.rvWidgets.adapter = adapter
        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    private fun onHomeWorkListSuccess(data: HomeWorkListResponse) {
        if (data.widgets.isEmpty()) {
            infiniteScrollListener.setLastPageReached(true)
        }
        if (infiniteScrollListener.currentPage == 1) {
            binding.tvToolbarTitle.text = data.title.orEmpty()
            adapter.setWidgets(data.widgets)
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

    private fun updateProgressBarState(state: Boolean) {
        binding.progressBar.setVisibleState(state)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            initiateRecyclerListenerAndFetchInitialData()
        }
    }

    override fun performAction(action: Any) {
        if (action is OnHomeWorkListClicked) {
            if (action.status) {
                HomeWorkSolutionActivity.startActivity(this, true, action.qid)
            } else {
                startActivityForResult(HomeWorkActivity.getIntent(this, action.qid), 1)
            }
        }
    }

}