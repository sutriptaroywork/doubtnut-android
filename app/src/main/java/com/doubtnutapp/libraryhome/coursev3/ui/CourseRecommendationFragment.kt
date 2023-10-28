package com.doubtnutapp.libraryhome.coursev3.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.CourseRecommendationRadioButtonSelected
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.course.widgets.CourseRecommendationParentWidget
import com.doubtnutapp.course.widgets.CourseRecommendationSubmittedAnswerWidget
import com.doubtnutapp.data.remote.models.CourseRecommendationResponse
import com.doubtnutapp.databinding.FragmentCourseRecommendationBinding
import com.doubtnutapp.libraryhome.coursev3.viewmodel.CourseRecommendationViewModel
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.widgetmanager.WidgetTypes
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import java.util.*
import javax.inject.Inject

class CourseRecommendationFragment :
    BaseBindingFragment<CourseRecommendationViewModel, FragmentCourseRecommendationBinding>(),
    ActionPerformer {

    companion object {
        private const val TAG = "CourseRecommendationFragment"
        private const val IS_BACK = "is_back"
        private const val PAGE = "page"
        fun newInstance(isBack: Boolean, page: String?) = CourseRecommendationFragment()
                .apply {
                    arguments = Bundle().apply {
                        putBoolean(IS_BACK, isBack)
                        putString(PAGE, page)
                    }
                }
    }

    private lateinit var widgetLayoutAdapter: WidgetLayoutAdapter

    private var sessionId = ""

    private val isBack: Boolean
        get() = arguments?.getBoolean(IS_BACK, false) ?: false

    private val page: String
        get() = arguments?.getString(PAGE) ?: ""

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCourseRecommendationBinding =
        FragmentCourseRecommendationBinding.inflate(inflater, container, false)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): CourseRecommendationViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        putLastVisitedTime()
        widgetLayoutAdapter = WidgetLayoutAdapter(requireContext(), this)
        binding.recyclerView.run {
            layoutManager = LinearLayoutManager(context)
                .apply {
                    stackFromEnd = true
                }
            adapter = widgetLayoutAdapter
        }
        binding.imageViewClose.setOnClickListener {
            goBack()
        }
        viewModel.extraParams.apply {
            put(EventConstants.EVENT_SCREEN_PREFIX + EventConstants.NAME, TAG)
            put(EventConstants.IS_BACK, isBack)
        }
        analyticsPublisher.publishEvent(
            AnalyticsEvent(EventConstants.COURSE_RECOMMENDATION_PAGE_VIEW,
                hashMapOf<String, Any>().apply {
                    putAll(viewModel.extraParams)
                }, ignoreSnowplow = true)
        )
        viewModel.fetchCourseRecommendationData(
                initiate = true,
                isBack = isBack,
                sessionId = "",
                messageId = "",
                selectedOptionKey = "",
                page = page
        )
    }

    private fun putLastVisitedTime() {
        defaultPrefs().edit { putLong(Constants.CR_LAST_VISITED_TIME, Date().time) }
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.widgetsLiveData.observeK(
            viewLifecycleOwner,
            this::onWidgetListFetched,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgress
        )
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(childFragmentManager, "BadRequestDialog")
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun ioExceptionHandler() {
        context?.let { currentContext ->
            if (NetworkUtils.isConnected(currentContext)) {
                toast(getString(R.string.somethingWentWrong))
            } else {
                toast(getString(R.string.string_noInternetConnection))
            }
        }
    }

    private fun updateProgress(state: Boolean) {
        binding.progressBar.setVisibleState(state)
    }

    private fun onWidgetListFetched(baseResponse: CourseRecommendationResponse) {
        if (!baseResponse.widgets.isNullOrEmpty()) {
            widgetLayoutAdapter.widgets.filterIsInstance<CourseRecommendationParentWidget.WidgetChildModel>()
                .map {
                    it._data?.hideAnimation = true
                }
            val lastCRWidgetIndex =
                baseResponse.widgets.indexOfLast { it is CourseRecommendationParentWidget.WidgetChildModel }
            if (lastCRWidgetIndex != -1) {
                baseResponse.widgets.filterIsInstance<CourseRecommendationParentWidget.WidgetChildModel>()
                    .forEachIndexed { index, widgetEntityModel ->
                        widgetEntityModel._data?.hideAnimation = index != lastCRWidgetIndex
                    }
            }
            widgetLayoutAdapter.addWidgets(baseResponse.widgets)
            binding.recyclerView.smoothScrollToPosition(widgetLayoutAdapter.itemCount - 1)
        }
        sessionId = baseResponse.sessionId.orEmpty()
    }

    override fun performAction(action: Any) {
        if (action is CourseRecommendationRadioButtonSelected) {
            val widget = CourseRecommendationSubmittedAnswerWidget.Model()
                .apply {
                    _type = WidgetTypes.TYPE_WIDGET_COURSE_RECOMMENDATION_SUBMITTED_ANSWER
                    _data = CourseRecommendationSubmittedAnswerWidget.Data(
                        action.response
                    )
                }
            widgetLayoutAdapter.addWidget(widget)
            binding.recyclerView.smoothScrollToPosition(widgetLayoutAdapter.itemCount - 1)
            viewModel.fetchCourseRecommendationData(
                    initiate = false,
                    isBack = isBack,
                    sessionId = sessionId,
                    messageId = action.submitId,
                    selectedOptionKey = action.responseId,
                    page = page
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        analyticsPublisher.publishEvent(
            AnalyticsEvent(EventConstants.COURSE_RECOMMENDATION_CLOSE,
                hashMapOf<String, Any>().apply {
                    if (isViewModelInitialized) {
                        putAll(viewModel.extraParams)
                        put(
                            EventConstants.COURSE_RECOMMENDATION_LAST_SHOWN_WIDGET,
                            viewModel.lastShownWidgetType
                        )
                    }
                }, ignoreSnowplow = true)
        )
    }

}