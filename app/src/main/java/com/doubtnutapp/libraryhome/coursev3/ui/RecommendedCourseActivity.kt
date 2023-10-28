package com.doubtnutapp.libraryhome.coursev3.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.EventBus.CloseEvent
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.data.remote.models.CourseListData
import com.doubtnutapp.databinding.ActivityRecommendedCourseBinding
import com.doubtnutapp.liveclass.viewmodel.CourseSwitchViewModel
import com.doubtnutapp.setVisibleState
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class RecommendedCourseActivity : BaseBindingActivity<CourseSwitchViewModel, ActivityRecommendedCourseBinding>() {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    companion object {
        const val TAG = "RecommendedCourseActivity"
        private const val SELECTED_CLASS = "selected_class"
        private const val SELECTED_EXAM = "selected_exam"
        private const val SELECTED_EXAM_YEAR = "selected_exam_year"
        private const val SELECTED_MEDIUM = "selected_medium"
        private const val ASSORTMENT_ID = "assortment_id"

        fun getStartIntent(context: Context,
                           selectedClass: String, selectedExam: String, selectedExamYear: String,
                           selectedMedium: String, assortmentId: String): Intent {
            return Intent(context, RecommendedCourseActivity::class.java).apply {
                putExtra(SELECTED_CLASS, selectedClass)
                putExtra(SELECTED_EXAM, selectedExam)
                putExtra(SELECTED_EXAM_YEAR, selectedExamYear)
                putExtra(SELECTED_MEDIUM, selectedMedium)
                putExtra(ASSORTMENT_ID, assortmentId)
            }
        }
    }

    private var selectedClass = ""
    private var selectedExam = ""
    private var selectedExamYear = ""
    private var selectedMedium = ""
    private var assortmentId = ""
    private var adapter: WidgetLayoutAdapter? = null
    private var closeObserver: Disposable? = null

    override fun provideViewBinding(): ActivityRecommendedCourseBinding =
            ActivityRecommendedCourseBinding.inflate(layoutInflater)

    override fun provideViewModel(): CourseSwitchViewModel = viewModelProvider(viewModelFactory)

    override fun setupView(savedInstanceState: Bundle?) {
        adapter = WidgetLayoutAdapter(
                this)
        binding.rvWidgets.adapter = adapter
        selectedClass = intent.extras?.getString(SELECTED_CLASS, "").orEmpty()
        selectedExam = intent.extras?.getString(SELECTED_EXAM, "").orEmpty()
        selectedExamYear = intent.extras?.getString(SELECTED_EXAM_YEAR, "").orEmpty()
        selectedMedium = intent.extras?.getString(SELECTED_MEDIUM, "").orEmpty()
        assortmentId = intent.extras?.getString(ASSORTMENT_ID, "").orEmpty()
        viewModel.getCourseListData(selectedClass, selectedExam, selectedExamYear, selectedMedium, assortmentId)
        analyticsPublisher.publishEvent(
                AnalyticsEvent(
                        EventConstants.RECOMMENDED_COURSES_PAGE_VISIT,
                        hashMapOf(
                                EventConstants.ASSORTMENT_ID to assortmentId,
                        ), ignoreSnowplow = true
                )
        )
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.courseListLiveData.observeK(this,
                this::onCourseListSuccess,
                this::onApiError,
                this::unAuthorizeUserError,
                this::ioExceptionHandler,
                this::updateProgressBarState)

        closeObserver = DoubtnutApp.INSTANCE.bus()?.toObservable()?.subscribe { event ->
            if (event is CloseEvent) {
                finish()
            }
        }
    }

    private fun ioExceptionHandler() {
        if (NetworkUtils.isConnected(this).not()) {
            toast(getString(R.string.string_noInternetConnection))
        } else {
            toast(getString(R.string.somethingWentWrong))
        }
    }

    private fun updateProgressBarState(state: Boolean) {
        binding.progressBar.setVisibleState(state)
    }

    override fun providePageName(): String {
        return TAG
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(supportFragmentManager, "BadRequestDialog")
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    override fun onDestroy() {
        super.onDestroy()
        closeObserver?.dispose()
    }

    private fun onCourseListSuccess(data: CourseListData) {
        binding.tvToolbarTitle.text = data.title.orEmpty()
        if (!data.widgets.isNullOrEmpty()) {
            adapter?.addWidgets(data.widgets)
        } else {
            binding.noCourseTv.text = data.message.orEmpty()
        }
        binding.noCourseTv.setVisibleState(data.widgets.isNullOrEmpty())
    }
}