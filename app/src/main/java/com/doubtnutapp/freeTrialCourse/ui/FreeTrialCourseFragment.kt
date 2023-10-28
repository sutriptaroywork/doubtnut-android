package com.doubtnutapp.freeTrialCourse.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.EventBus.VipStateEvent
import com.doubtnutapp.MainActivity
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.base.OnFreeCourseWidgetClicked
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.databinding.ActivityFreeTrialCourseBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.freeTrialCourse.model.FreeTrialCourseActivationResponse
import com.doubtnutapp.freeTrialCourse.model.FreeTrialCourseResponse
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.utils.showApiErrorToast
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class FreeTrialCourseFragment :
    BaseBindingFragment<FreeTrialCourseViewModel, ActivityFreeTrialCourseBinding>(),
    ActionPerformer,
    DialogConfirmationAndSuccessActionListener {

    companion object {
        const val TAG = "FreeTrialCoursePage"
    }

    private val widgetsAdapter: WidgetLayoutAdapter by lazy {
        WidgetLayoutAdapter(requireActivity(), this, TAG)
    }

    var disposable: Disposable? = null

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private var doPopFreeTrialCourseFragment = false

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): FreeTrialCourseViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): ActivityFreeTrialCourseBinding {
        return ActivityFreeTrialCourseBinding.inflate(inflater)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {

        binding.imageBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.recyclerViewWidgets.adapter = widgetsAdapter
        viewModel.fetchFreeLiveCourses()

        disposable = DoubtnutApp.INSTANCE.bus()?.toObservable()?.subscribe {
            if (it is VipStateEvent) {
                if (it.state) {
                    doPopFreeTrialCourseFragment = true
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (doPopFreeTrialCourseFragment) {
            requireActivity().onBackPressed()
        }
    }

    override fun setupObservers() {
        super.setupObservers()

        viewModel.freeCoursesLiveData.observeK(
            viewLifecycleOwner,
            this::handleSuccessfulResponseForCourseList,
            this::onApiError,
            this::onOtherError,
            this::onOtherError,
            this::onProgress
        )

        viewModel.subscribeToCourseLiveData.observeK(
            viewLifecycleOwner,
            this::handleSuccessfulResponseForCourseActivation,
            this::onApiError,
            this::onOtherError,
            this::onOtherError,
            this::onActivationProgress
        )
    }

    private fun handleSuccessfulResponseForCourseList(freeTrialCourseResponse: FreeTrialCourseResponse?) {
        binding.recyclerViewWidgets.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewWidgets.adapter = widgetsAdapter
        if (freeTrialCourseResponse != null && freeTrialCourseResponse.data.widgets != null) {
            widgetsAdapter.setWidgets(freeTrialCourseResponse.data.widgets)

            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.FREE_COURSE_LIST_OPENED,
                    hashMapOf(), ignoreSnowplow = false
                )
            )

            freeTrialCourseResponse.data.pageTitle?.let {
                binding.textViewToolbarTitle.text = freeTrialCourseResponse.data.pageTitle
            }
        }
        // response is null open MainActivity
        else {
            openMainActivityHomePage()
        }

    }

    private fun showCourseActivateConfirmationDialog(id: Int) {
        viewModel.freeTrialCourseListResponse?.let {
            val response = it.data.prePurchasePopup
            val dialog = DialogTrialCourseConfirmation.newInstance(
                response?.title, response?.cta2?.title,
                response?.cta1?.title
            )
            dialog.show(this.childFragmentManager, TAG)
        }
    }

    /** Open activation success dialog after success response */
    private fun handleSuccessfulResponseForCourseActivation(freeActivationResponse: FreeTrialCourseActivationResponse) {
        viewModel.freeTrialCourseListResponse?.let {
            val response = it.data.postPurchasePopupData
            val dialog = DialogTrialFreeCourseActivatedSuccess.newInstance(
                response?.title,
                viewModel.deeplinkForCardSelected
            )
            dialog.show(childFragmentManager, TAG)
        }

        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.FREE_COURSE_SUCCESS_DIALOG_OPENED))

    }

    override fun onViewCourseClickedAfterPurchaseSuccess() {
        requireActivity().onBackPressed()
        deeplinkAction.performAction(requireContext(), viewModel.deeplinkForCardSelected)
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.FREE_COURSES_OPEN_COURSE_CLICKED))
    }

    private fun onApiError(error: Throwable) {
        apiErrorToast(error)
    }

    private fun onOtherError() {
        showApiErrorToast(requireContext())
    }

    private fun onProgress(setVisible: Boolean) {
        if (setVisible) {
            binding.shimmerProgress.isVisible = setVisible
            binding.shimmerProgress.startShimmer()
        } else {
            binding.shimmerProgress.visibility = View.GONE
        }
    }

    private fun onActivationProgress(setVisible: Boolean) {
        if (setVisible) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }

    override fun confirmActivationOfWidget() {
        viewModel.activateCourse(viewModel.courseIdSelected)
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.FREE_COURSE_CARD_CONFIRMATION_YES_CLICKED))
    }

    override fun onConfirmationDialogClosed() {
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.FREE_COURSE_CARD_CONFIRMATION_CLOSE_CLICKED))
    }

    override fun onConfirmationDialogNoClicked() {
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.FREE_COURSE_CARD_CONFIRMATION_NO_CLICKED))
    }

    override fun performAction(action: Any) {
        if (action is OnFreeCourseWidgetClicked) {
            showCourseActivateConfirmationDialog(action.id)
            viewModel.courseIdSelected = action.id
            action.deeplinkForCourseSelected?.let {
                viewModel.deeplinkForCardSelected = it
            }
        }
    }

    private fun openMainActivityHomePage() {
        startActivity(
            MainActivity.getStartIntent(
                requireContext(),
                doOpenHomePage = true
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposable?.dispose()
    }
}