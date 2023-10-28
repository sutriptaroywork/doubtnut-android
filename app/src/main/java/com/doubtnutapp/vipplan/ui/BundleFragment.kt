package com.doubtnutapp.vipplan.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.activityViewModelProvider
import com.doubtnutapp.Constants
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.RequestCheckout
import com.doubtnutapp.base.RequestVipTrial
import com.doubtnutapp.base.ShowSaleDialog
import com.doubtnutapp.course.widgets.PackageDetailWidgetModel
import com.doubtnutapp.databinding.FragmentBundleBinding
import com.doubtnutapp.defaultPrefs
import com.doubtnutapp.domain.payment.entities.PlanDetail
import com.doubtnutapp.setVisibleState
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.utils.EventObserver
import com.doubtnutapp.vipplan.PaymentHelpViewItem
import com.doubtnutapp.vipplan.viewmodel.VipPlanViewModel
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.uxcam.UXCam
import kotlinx.android.synthetic.main.fragment_bundle.*
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 03/10/20.
 */

class BundleFragment : BaseBindingFragment<VipPlanViewModel, FragmentBundleBinding>(),
    ActionPerformer {

    companion object {
        const val TAG = "BundleFragment"
        const val ASSORTMENT_ID = "id"
        fun newInstance(assortmentId: String) = BundleFragment()
            .apply {
                arguments = Bundle().apply {
                    putString(ASSORTMENT_ID, assortmentId)
                }
            }
    }


    private var paymentHelpViewItem = mutableListOf<PaymentHelpViewItem>()

    private var paymentHelpTitle = ""

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    var ids = ""
    private var actionPerformer: ActionPerformer? = null

    private fun fetchPlanDetail() {
        val assortmentId = arguments?.getString(ASSORTMENT_ID).orEmpty()
        viewModel.fetchPlanDetail(assortmentId)
    }

    private fun setUpObserver() {
        viewModel.planDetail.observe(viewLifecycleOwner, EventObserver {
            addDataToView(it)
        })

        viewModel.isLoading.observe(viewLifecycleOwner, Observer {
            progressBar.setVisibleState(it)
        })

        buttonBack.setOnClickListener {
            activity?.onBackPressed()
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
        actionPerformer?.performAction(ShowSaleDialog(shouldShowSaleDialog,
                nudgeMaxCount, nudgeId, TAG))
        ids = planDetail.widgets
                .filterIsInstance<PackageDetailWidgetModel>()
                .joinToString {
                    it.data.items?.joinToString { packageItem ->
                        packageItem.assortmentId.orEmpty()
                    }.orEmpty()
                }

        analyticsPublisher.publishEvent(
                AnalyticsEvent(EventConstants.BUNDLE_PAGE_VIEW,
                        hashMapOf<String, Any>().apply {
                            put(EventConstants.ASSORTMENT_IDS, ids)
                            put(EventConstants.ASSORTMENT_ID, arguments?.getString(ASSORTMENT_ID).orEmpty())
                        })
        )

        paymentHelpViewItem = planDetail.paymentHelp?.list?.map {
            PaymentHelpViewItem(it.name.orEmpty(), it.value.orEmpty())
        }?.toMutableList() ?: mutableListOf()

        paymentHelpTitle = planDetail.paymentHelp?.title.orEmpty()
        if (paymentHelpTitle.isBlank().not()) {
            textViewPaymentHelp.text = paymentHelpTitle
            textViewPaymentHelp.setOnClickListener {
                viewModel.publishEventWith(EventConstants.VIP_PAYMENT_HELP_CLICK, "", ignoreSnowplow = true)
                PaymentHelpFragment.newInstance(paymentHelpTitle,
                        paymentHelpViewItem as ArrayList<PaymentHelpViewItem>)
                        .show(childFragmentManager, PaymentHelpFragment.TAG)
            }
        }
    }

    private var adapter: WidgetLayoutAdapter? = null

    private fun setUpRecyclerView() {
        adapter = WidgetLayoutAdapter(requireContext(), this)
        rvWidgets.layoutManager = LinearLayoutManager(requireContext())
        rvWidgets.adapter = adapter
    }

    override fun performAction(action: Any) {
        if (action is RequestVipTrial) {
            viewModel.requestVipTrial(action.id)
        } else if (action is RequestCheckout) {
            viewModel.requestCheckoutData(action.variantId, action.coupon, "course_package")
        }
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

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentBundleBinding {
        return FragmentBundleBinding.inflate(layoutInflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): VipPlanViewModel {
        return activityViewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        UXCam.tagScreenName(TAG)
        setUpObserver()
        setUpRecyclerView()
        fetchPlanDetail()
    }

}