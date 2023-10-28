package com.doubtnutapp.liveclass.ui.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.base.Dismiss
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.databinding.DialogCouponListBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.getScreenWidth
import com.doubtnutapp.libraryhome.course.viewmodel.CoursesViewModel
import com.doubtnutapp.setVisibleState
import com.doubtnutapp.ui.base.BaseBindingDialogFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class CouponListDialogFragment :
    BaseBindingDialogFragment<CoursesViewModel, DialogCouponListBinding>(),
    ActionPerformer {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    companion object {
        const val TAG = "CouponListDialogFragment"
        const val EVENT_TAG = "coupon_list_dialog_fragment"
        const val PAGE = "page"

        fun newInstance(page: String?): CouponListDialogFragment {
            return CouponListDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(PAGE, page)
                }
            }
        }
    }

    private var data: CouponDialogData? = null
    private lateinit var v: View
    private var page: String = ""
    private var adapter: WidgetLayoutAdapter? = null

    private fun initUI() {
        binding.closeBtn.setOnClickListener {
            dialog?.dismiss()
            activity?.finish()
        }
        binding.viewIvCloseClickHandler.setOnClickListener {
            dialog?.dismiss()
            activity?.finish()
        }
        page = arguments?.getString(PAGE, "").orEmpty()
        adapter = WidgetLayoutAdapter(
            context = requireContext(),
            actionPerformer = this,
            source = TAG
        )
        binding.rvWidgets.adapter = adapter
        val map = hashMapOf<String, Any>()
        map["payment_for"] = page
        viewModel.getCouponData(map)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fullScreenMode()
    }

    private fun fullScreenMode() {
        dialog?.window?.setLayout(requireActivity().getScreenWidth() - 16.dpToPx(), 600.dpToPx())
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.couponLiveData.observeK(
            viewLifecycleOwner,
            this::onCouponListSuccess,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgressBarState
        )
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        activity?.finish()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        activity?.finish()
    }

    private fun ioExceptionHandler() {
        context?.let {
            if (NetworkUtils.isConnected(it).not()) {
                toast(it.getString(R.string.string_noInternetConnection))
            } else {
                toast(it.getString(R.string.somethingWentWrong))
            }
        }
    }

    private fun updateProgressBarState(state: Boolean) {
        binding.progressBar.setVisibleState(state)
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(requireFragmentManager(), "BadRequestDialog")
    }

    private fun onCouponListSuccess(data: CouponDialogData) {
        this.data = data
        data.widgets?.forEach {
            if (it.extraParams == null) {
                it.extraParams = HashMap()
            }
            it.extraParams?.put(EventConstants.PAGE, page)
        }
        binding.ivClose.isVisible = data.showCloseBtn ?: false
        binding.viewIvCloseClickHandler.isVisible = data.showCloseBtn ?: false

        if (data.wrapHeight == true) {
            binding.rvWidgets.updateLayoutParams<ConstraintLayout.LayoutParams> {
                height = ConstraintLayout.LayoutParams.WRAP_CONTENT
            }

            dialog?.window?.setLayout(
                requireActivity().getScreenWidth() - 16.dpToPx(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        binding.closeBtn.isVisible = data.ctaText.isNullOrEmpty().not()
        binding.closeBtn.text = data.ctaText.orEmpty()
        adapter?.setWidgets(data.widgets.orEmpty())
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogCouponListBinding {
        return DialogCouponListBinding.inflate(inflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): CoursesViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        initUI()
        analyticsPublisher.publishEvent(
            hashMapOf<String, Any>(
                EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                EventConstants.PAGE to page,
            ).let {
                AnalyticsEvent(
                    "${EVENT_TAG}_${EventConstants.VIEWED}",
                    it
                )
            }
        )
    }

    override fun performAction(action: Any) {
        when (action) {
            is Dismiss -> {
                dialog?.dismiss()
                activity?.finish()
            }
            else -> {
            }
        }
    }

}

@Keep
data class CouponDialogData(
    @SerializedName("show_close_btn") val showCloseBtn: Boolean?,
    @SerializedName("wrap_height") val wrapHeight: Boolean?,
    @SerializedName("button_text") val ctaText: String?,
    @SerializedName("widgets") val widgets: List<WidgetEntityModel<*, *>>?,
)
