package com.doubtnutapp.liveclass.ui

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.OnNudgeClicked
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.course.widgets.NudgePopupWidget
import com.doubtnutapp.data.remote.models.NudgeData
import com.doubtnutapp.databinding.FragmentWidgetNudgePopupBinding
import com.doubtnutapp.libraryhome.course.viewmodel.CoursesViewModel
import com.doubtnutapp.ui.base.BaseBindingDialogFragment
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.utils.showApiErrorToast
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import javax.inject.Inject

class NudgeFragment :
    BaseBindingDialogFragment<CoursesViewModel, FragmentWidgetNudgePopupBinding>(),
    ActionPerformer {

    companion object {
        const val TAG = "NudgeFragment"
        fun newInstance(
            id: String,
            nudgeType: String,
            page: String?,
            type: String?,
            isTransparent: Boolean = true
        ) = NudgeFragment()
            .apply {
                arguments = Bundle().apply {
                    putString(Constants.NUDGE_ID, id)
                    putString(Constants.NUDGE_TYPE, nudgeType)
                    putString(Constants.PAGE, page)
                    putString(Constants.TYPE, type)
                    putBoolean(IS_TRANSPARENT, isTransparent)
                }
            }

        const val IS_TRANSPARENT = "is_transparent"
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    var ids = ""
    var nudgeId: String? = null
    var nudgeType: String? = null
    private val page: String? by lazy {
        arguments?.getString(Constants.PAGE)
    }
    private val type: String? by lazy {
        arguments?.getString(Constants.TYPE)
    }
    var isTransparent: Boolean = true
    private var model: NudgePopupWidget.NudgePopupWidgetModel? = null
    private var actionPerformer: ActionPerformer? = null

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentWidgetNudgePopupBinding =
        FragmentWidgetNudgePopupBinding.inflate(layoutInflater)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): CoursesViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        nudgeId = arguments?.getString(Constants.NUDGE_ID)
        nudgeType = arguments?.getString(Constants.NUDGE_TYPE)
        isTransparent = arguments?.getBoolean(IS_TRANSPARENT) ?: true

        fullScreenMode()
        setUpRecyclerView()
        fetchNudgeDetails()
        mBinding?.ivClose?.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.EVENT_NUDGE_CLOSED,
                    hashMapOf<String, Any>(
                        EventConstants.NUDGE_ID to model?.data?.widgetId.orEmpty(),
                        EventConstants.NUDGE_TYPE to model?.data?.nudgeType.orEmpty(),
                    )
                )
            )
            dialog?.dismiss()
        }

        dialog?.setOnDismissListener {
            activity?.finish()
        }
        dialog?.setOnCancelListener {

        }
    }

    private fun fullScreenMode() {
        val width = requireActivity().getScreenWidth() - 100
        dialog?.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        if (isTransparent) {
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        } else {
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        activity?.finish()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        activity?.finish()
    }

    private fun fetchNudgeDetails() {
        viewModel.getNudgeData(nudgeId, page, type)
    }

    override fun setupObservers() {
        viewModel.nudgesLiveData.observeK(
            this,
            this::onNudgesDatafetched,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgress
        )
    }

    private fun onNudgesDatafetched(nudgeData: NudgeData) {
        adapter?.setWidgets(nudgeData.widgets)
        mBinding?.ivClose?.loadImageEtx(nudgeData.closeImageUrl.orEmpty())
        mBinding?.ivClose?.setOnClickListener {
            activity?.finish()
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.EVENT_NUDGE_CLOSED,
                    hashMapOf<String, Any>(
                        EventConstants.NUDGE_ID to model?.data?.widgetId.orEmpty(),
                        EventConstants.NUDGE_TYPE to model?.data?.nudgeType.orEmpty(),
                    ), ignoreSnowplow = true
                )
            )
        }
        nudgeData.widgets.find { it is NudgePopupWidget.NudgePopupWidgetModel }.apply {
            if (this != null) {
                model = this as NudgePopupWidget.NudgePopupWidgetModel
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.EVENT_NUDGE_VIEW,
                        hashMapOf<String, Any>().apply {
                            put(EventConstants.NUDGE_ID, model?.data?.widgetId.orEmpty())
                            put(EventConstants.NUDGE_TYPE, model?.data?.nudgeType.orEmpty())
                            putAll(model?.extraParams ?: hashMapOf())
                        }
                    ))
            } else {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.EVENT_NUDGE_VIEW,
                        hashMapOf<String, Any>().apply {
                            putAll(nudgeData.extraParams ?: hashMapOf())
                        }
                    ))
            }
        }

        if (nudgeData.bgColor.isNotNullAndNotEmpty()) {
            mBinding?.mainLayout?.setBackgroundColor(Utils.parseColor(nudgeData.bgColor))
        }
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

    private var adapter: WidgetLayoutAdapter? = null

    private fun setUpRecyclerView() {
        adapter = WidgetLayoutAdapter(requireContext(), this)
        mBinding?.rvWidgets?.layoutManager = LinearLayoutManager(requireContext())
        mBinding?.rvWidgets?.adapter = adapter
    }

    override fun onDestroy() {
        super.onDestroy()
        analyticsPublisher.publishEvent(
            AnalyticsEvent(EventConstants.BUNDLE_BACK,
                hashMapOf<String, Any>()
                    .apply {
                        put(EventConstants.ASSORTMENT_IDS, ids)
                    })
        )
    }

    override fun performAction(action: Any) {
        if (action is OnNudgeClicked) {
            dismiss()
        }
    }
}