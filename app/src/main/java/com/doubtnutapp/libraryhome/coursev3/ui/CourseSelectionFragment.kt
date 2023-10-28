package com.doubtnutapp.libraryhome.coursev3.ui

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.base.OnCourseSelected
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.databinding.FragmentCourseSelectionBinding
import com.doubtnutapp.domain.payment.entities.PurchasedCourseDetail
import com.doubtnutapp.libraryhome.coursev3.viewmodel.CourseViewModelV3
import com.doubtnutapp.moengage.MoEngageUtils
import com.doubtnutapp.setVisibleState
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.utils.showApiErrorToast
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import javax.inject.Inject

class CourseSelectionFragment :
    BaseBindingFragment<CourseViewModelV3, FragmentCourseSelectionBinding>(),
    ActionPerformer {

    companion object {
        const val TAG = "CourseSelectionFragment"
        const val PAGE = "page"
        const val REQUEST_CODE = "requestCode"
        const val ASSORTMENT_ID = "assortment_id"
        const val CATEGORY_ID = "category_id"

        fun newInstance(page: String, requestCode: String) = CourseSelectionFragment()
            .apply {
                arguments = Bundle().apply {
                    putString(PAGE, page)
                    putString(REQUEST_CODE, requestCode)
                }
            }
    }

    private var requestCode: String = ""

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    var ids = ""
    var page: String? = null
    private var actionPerformer: ActionPerformer? = null

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCourseSelectionBinding =
        FragmentCourseSelectionBinding.inflate(inflater, container, false)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): CourseViewModelV3 {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        page = arguments?.getString(PAGE)
        requestCode = arguments?.getString(REQUEST_CODE).orEmpty()
        fetchPlanDetail(page.orEmpty())
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.COURSE_BOTTOM_SHEET_VIEW,
                hashMapOf(EventConstants.SOURCE to EventConstants.BOTTOM_ICON),
                ignoreSnowplow = true
            )
        )
    }

    private fun fetchPlanDetail(page: String) {
        viewModel.getPurchasedCourses(page)
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.purchasedCourseLiveData.observeK(
            this,
            this::onSuccess,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgress
        )
    }

    private fun onSuccess(data: PurchasedCourseDetail) {
        addDataToView(data)
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.BUNDLE_SHEET_VIEW,
                hashMapOf<String, Any>(
                    EventConstants.SOURCE to page.orEmpty()
                ).apply {
                    putAll(data.extraParams.orEmpty())
                }, ignoreBranch = false, ignoreMoengage = false
            )
        )
        MoEngageUtils.setUserAttribute(requireContext(), "dn_bnb_clicked",true)

    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun ioExceptionHandler() {
        showApiErrorToast(requireContext())
    }

    private fun unAuthorizeUserError() {
        showApiErrorToast(requireContext())
    }

    private fun updateProgress(state: Boolean) {
        binding.progressBar.setVisibleState(state)
    }

    fun setActionListener(actionPerformer: ActionPerformer) {
        this.actionPerformer = actionPerformer
    }

    private fun addDataToView(purchasedCourseDetail: PurchasedCourseDetail) {
        setUpRecyclerView()
        adapter?.setWidgets(purchasedCourseDetail.widgets)
        binding.title.text = purchasedCourseDetail.title.orEmpty()
    }

    private var adapter: WidgetLayoutAdapter? = null

    private fun setUpRecyclerView() {
        adapter = WidgetLayoutAdapter(requireContext(), this)
        binding.rvWidgets.layoutManager = LinearLayoutManager(requireContext())
        binding.rvWidgets.adapter = adapter
    }

    override fun performAction(action: Any) {
        if (action is OnCourseSelected) {
            val intent = Intent()
            intent.putExtra(ASSORTMENT_ID, action.assortmentId)
            intent.putExtra(CATEGORY_ID, action.categoryId)
            val bundle = Bundle()
            bundle.putString(ASSORTMENT_ID, action.assortmentId)
            bundle.putString(CATEGORY_ID, action.categoryId)
            setFragmentResult(requestCode, bundle)
        }
    }
}