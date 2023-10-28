package com.doubtnutapp.libraryhome.coursev3.ui

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.OnCourseSelected
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.databinding.FragmentBundleV2Binding
import com.doubtnutapp.domain.payment.entities.PurchasedCourseDetail
import com.doubtnutapp.libraryhome.coursev3.viewmodel.CourseViewModelV3
import com.doubtnutapp.liveclass.ui.BundleFragmentV2
import com.doubtnutapp.moengage.MoEngageUtils
import com.doubtnutapp.ui.base.adapter.BaseBindingBottomSheetDialogFragment
import com.doubtnutapp.utils.showApiErrorToast
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import javax.inject.Inject

class CourseSelectionDialogFragment :
    BaseBindingBottomSheetDialogFragment<CourseViewModelV3, FragmentBundleV2Binding>(),
    ActionPerformer {

    companion object {
        const val TAG = "CourseSelectionDialogFragment"
        const val ASSORTMENT_ID = "id"
        const val SOURCE = "source"
        const val PAGE = "page"
        fun newInstance(page: String?) = CourseSelectionDialogFragment()
            .apply {
                arguments = Bundle().apply {
                    putString(PAGE, page)
                }
            }
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private val source: String? by lazy {
        arguments?.getString(SOURCE)
    }

    private val page: String? by lazy {
        arguments?.getString(PAGE)
    }

    private var actionPerformer: ActionPerformer? = null


val assortmentId: String by lazy {
        arguments?.getString(BundleFragmentV2.ASSORTMENT_ID).orEmpty()
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentBundleV2Binding {
        return FragmentBundleV2Binding.inflate(inflater, container, false)
    }

    override fun providePageName(): String = TAG

    override fun provideViewModel(): CourseViewModelV3 {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        dialog?.setCanceledOnTouchOutside(true)
        setUpRecyclerView()
        fetchPurchasedCourses()
        mBinding?.ivClose?.setOnClickListener {
            dialog?.dismiss()
        }
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.COURSE_BOTTOM_SHEET_VIEW,
                hashMapOf(EventConstants.SOURCE to EventConstants.TOP_ICON),
                ignoreSnowplow = true
            )
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnDismissListener {
            activity?.finish()
        }
        dialog.setOnCancelListener {

        }
        return dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BaseBottomSheetDialog)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        activity?.finish()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        activity?.finish()
    }

    private fun fetchPurchasedCourses() {
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
                    EventConstants.ASSORTMENT_ID to assortmentId,
                    EventConstants.SOURCE to source.orEmpty()
                ).apply {
                    putAll(data.extraParams.orEmpty())
                }, ignoreBranch = false , ignoreMoengage = false
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
        mBinding?.progressBar?.setVisibleState(state)
    }

    fun setActionListener(actionPerformer: ActionPerformer) {
        this.actionPerformer = actionPerformer
    }

    private fun addDataToView(purchasedCourseDetail: PurchasedCourseDetail) {
        adapter?.setWidgets(purchasedCourseDetail.widgets)
        mBinding?.title?.text = purchasedCourseDetail.title.orEmpty()
    }

    private var adapter: WidgetLayoutAdapter? = null

    private fun setUpRecyclerView() {
        adapter = WidgetLayoutAdapter(requireContext(), this)
        mBinding?.rvWidgets?.layoutManager = LinearLayoutManager(requireContext())
        mBinding?.rvWidgets?.adapter = adapter
    }

    override fun performAction(action: Any) {
        if (action is OnCourseSelected) {
            dialog?.dismiss()
            defaultPrefs().edit().putString(Constants.SELECTED_ASSORTMENT_ID, action.assortmentId)
                .apply()
        }
    }

}