package com.doubtnutapp.liveclass.ui

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.events.Dismiss
import com.doubtnut.core.utils.applyTextColor
import com.doubtnut.core.utils.applyTextSize
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.RequestCheckout
import com.doubtnutapp.base.RequestVipTrial
import com.doubtnutapp.base.ShowSaleDialog
import com.doubtnutapp.course.event.TrialActivated
import com.doubtnutapp.course.widgets.PackageDetailWidgetModel
import com.doubtnutapp.databinding.FragmentBundleV2Binding
import com.doubtnutapp.domain.payment.entities.PlanDetail
import com.doubtnutapp.ui.base.adapter.BaseBindingBottomSheetDialogFragment
import com.doubtnutapp.utils.EventObserver
import com.doubtnutapp.vipplan.viewmodel.VipPlanViewModel
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.Disposable
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 03/10/20.
 *
 * Bottom Sheet Dialog Fragment used to show multiple payment options.
 */
class BundleFragmentV2 : BaseBindingBottomSheetDialogFragment<VipPlanViewModel, FragmentBundleV2Binding>(),
    ActionPerformer {

    companion object {
        const val TAG = "BundleFragment"
        const val ASSORTMENT_ID = "id"
        const val SOURCE = "source"
        fun newInstance(assortmentId: String, source: String?) = BundleFragmentV2()
            .apply {
                arguments = Bundle().apply {
                    putString(ASSORTMENT_ID, assortmentId)
                    putString(SOURCE, source)
                }
            }
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    var ids = ""
    var source: String? = null
    private var actionPerformer: ActionPerformer? = null

    private var appStateObserver: Disposable? = null

    val assortmentId: String by lazy {
        arguments?.getString(ASSORTMENT_ID).orEmpty()
    }
    private var mediaPlayer: MediaPlayer? = null


    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentBundleV2Binding {
        return FragmentBundleV2Binding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): VipPlanViewModel {
       return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        source = arguments?.getString(SOURCE)
        setUpRecyclerView()
        fetchPlanDetail()
        mBinding?.ivClose?.setOnClickListener {
            dialog?.dismiss()
        }
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

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    private fun fetchPlanDetail() {
        viewModel.fetchPlanDetail(assortmentId, true, source)
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.planDetail.observe(viewLifecycleOwner, EventObserver {
            addDataToView(it)
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.BUNDLE_SHEET_VIEW,
                    hashMapOf<String, Any>(
                        EventConstants.ASSORTMENT_ID to assortmentId,
                        EventConstants.SOURCE to source.orEmpty()
                    ).apply {
                        putAll(it.extraParams.orEmpty())
                    }, ignoreBranch = false , ignoreMoengage = false
                )
            )
        })

        viewModel.isLoading.observe(viewLifecycleOwner, {
            mBinding?.progressBar?.setVisibleState(it)
        })

        appStateObserver = DoubtnutApp.INSTANCE
            .bus()?.toObservable()?.subscribe {
                when (it) {
                    is TrialActivated -> {
                        dismiss()
                    }
                    is Dismiss -> {
                        if (it.tag == TAG) {
                            dismiss()
                        }
                    }
                }
            }

    }

    fun setActionListener(actionPerformer: ActionPerformer) {
        this.actionPerformer = actionPerformer
    }

    private fun addDataToView(planDetail: PlanDetail) {
        adapter?.setWidgets(planDetail.widgets)
        val shouldShowSaleDialog = planDetail.shouldShowSaleDialog ?: false
        val nudgeId = planDetail.nudgeId ?: 0
        val nudgeMaxCount = planDetail.nudgeCount ?: 0
        val savedNudgeId = defaultPrefs().getInt(Constants.NUDGE_ID_BUNDLE, 0)
        if (savedNudgeId == 0 || savedNudgeId != nudgeId) {
            defaultPrefs().edit().putInt(Constants.NUDGE_ID_BUNDLE, nudgeId).apply()
            defaultPrefs().edit().putInt(Constants.NUDGE_BUNDLE_COUNT, 0).apply()
        }
        mBinding?.title?.text = planDetail.title.orEmpty()

        mBinding?.flHeader?.isVisible =
            planDetail.headerTitle.isNotNullAndNotEmpty() || planDetail.headerIcon.isNotNullAndNotEmpty()
        mBinding?.tvHeader?.isVisible = planDetail.headerTitle.isNotNullAndNotEmpty()
        mBinding?.tvHeader?.text = planDetail.headerTitle.orEmpty()
        mBinding?.tvHeader?.applyTextColor(planDetail.headerTitleColor)
        mBinding?.tvHeader?.applyTextSize(planDetail.headerTitleSize)
        mBinding?.ivHeaderIcon?.isVisible = planDetail.headerIcon.isNotNullAndNotEmpty()
        mBinding?.ivHeaderIcon?.loadImage(planDetail.headerIcon)
        actionPerformer?.performAction(
            ShowSaleDialog(
                shouldShowSaleDialog,
                nudgeMaxCount, nudgeId, TAG
            )
        )
        ids = planDetail.widgets
            .filterIsInstance<PackageDetailWidgetModel>()
            .joinToString {
                it.data.items?.joinToString { packageItem ->
                    packageItem.assortmentId.orEmpty()
                }.orEmpty()
            }

        if (!planDetail.widgets.isNullOrEmpty() && planDetail.widgets.size > 2) {
            (dialog?.findViewById(R.id.design_bottom_sheet) as? View)?.let {
                BottomSheetBehavior.from(it)
                    .setState(BottomSheetBehavior.STATE_EXPANDED)
            }
        }
        if (!planDetail.voiceUrl.isNullOrBlank()) {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(planDetail.voiceUrl)
                prepareAsync()
            }
            mediaPlayer?.setOnPreparedListener {
                mediaPlayer?.start()
            }
        }
    }

    private var adapter: WidgetLayoutAdapter? = null

    private fun setUpRecyclerView() {
        adapter = WidgetLayoutAdapter(requireContext(), this, source = source)
        mBinding?.rvWidgets?.layoutManager = LinearLayoutManager(requireContext())
        mBinding?.rvWidgets?.adapter = adapter
    }

    override fun performAction(action: Any) {
        if (action is RequestVipTrial) {
            viewModel.requestVipTrial(action.id)
        } else if (action is RequestCheckout) {
            viewModel.requestCheckoutData(action.variantId, action.coupon)
        }
    }

    override fun onPause() {
        super.onPause()
        releaseMediaPlayer()
    }

    private fun releaseMediaPlayer() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        appStateObserver?.dispose()
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
}