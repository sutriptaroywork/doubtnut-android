package com.doubtnutapp.libraryhome.coursev3.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.apiErrorToast

import com.doubtnutapp.base.OnHomeWorkListClicked
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.data.remote.models.ApiSubjectDetailData
import com.doubtnutapp.libraryhome.coursev3.viewmodel.CourseViewModelV3
import com.doubtnutapp.liveclass.ui.HomeWorkActivity
import com.doubtnutapp.liveclass.ui.HomeWorkSolutionActivity
import com.doubtnutapp.setVisibleState
import com.doubtnutapp.sticker.BaseActivity
import com.doubtnutapp.ui.answer.TagsEndlessRecyclerOnScrollListener
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_home_work_list.*
import javax.inject.Inject

class SubjectDetailActivity : BaseActivity(), ActionPerformer {

    companion object {
        const val TAG = "SubjectDetailActivity"

        fun startActivity(context: Context, subject: String?, assortmentId: String?): Intent {
            return Intent(context, SubjectDetailActivity::class.java).apply {
                putExtra(Constants.SUBJECT, subject)
                putExtra(Constants.ASSORTMENT_ID, assortmentId)
            }
        }
    }

    private lateinit var viewModel: CourseViewModelV3
    private lateinit var adapter: WidgetLayoutAdapter
    private var subject = ""
    private var assortmentId = ""
    private lateinit var infiniteScrollListener: TagsEndlessRecyclerOnScrollListener

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subject_detail)
        init()
        setUpObserver()
        initiateRecyclerListenerAndFetchInitialData()
    }

    private fun initiateRecyclerListenerAndFetchInitialData() {
        val startPage = 1
        rvWidgets.clearOnScrollListeners()
        infiniteScrollListener = object : TagsEndlessRecyclerOnScrollListener(rvWidgets.layoutManager) {
            override fun onLoadMore(currentPage: Int) {
                viewModel.getSubjectDetailData(subject, assortmentId, currentPage)
            }
        }.also {
            it.setStartPage(startPage)
        }

        rvWidgets.addOnScrollListener(infiniteScrollListener)
        viewModel.getSubjectDetailData(subject, assortmentId, startPage)
    }

    private fun setUpObserver() {
        viewModel.subjectDetailData.observeK(this,
                ::onSuccess,
                ::onApiError,
                ::unAuthorizeUserError,
                ::ioExceptionHandler,
                ::updateProgressBarState)
    }

    private fun init() {
        analyticsPublisher.publishEvent(
                AnalyticsEvent(EventConstants.HW_LIST_VIEW,
                        hashMapOf(EventConstants.STUDENT_ID to UserUtil.getStudentId()), ignoreSnowplow = true))
        viewModel = viewModelProvider(viewModelFactory)
        subject = intent.getStringExtra(Constants.SUBJECT).orEmpty()
        assortmentId = intent.getStringExtra(Constants.ASSORTMENT_ID).orEmpty()
        adapter = WidgetLayoutAdapter(this, this)
        rvWidgets.layoutManager = LinearLayoutManager(this)
        rvWidgets.adapter = adapter
    }

    private fun onSuccess(data: ApiSubjectDetailData) {
        if (data.widgets.isEmpty()) {
            infiniteScrollListener.setLastPageReached(true)
        }
        if (infiniteScrollListener.currentPage == 1) {
            tvToolbarTitle.text = data.title.orEmpty()
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
        progressBar.setVisibleState(state)
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