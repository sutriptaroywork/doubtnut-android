package com.doubtnutapp

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.EventBus.CloseEvent
import com.doubtnutapp.EventBus.UpdateExploreEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.dnr.model.DnrCoursePurchaseReward
import com.doubtnutapp.dnr.viewmodel.DnrRewardViewModel
import com.doubtnutapp.liveclass.viewmodel.ReferralData
import com.doubtnutapp.liveclass.viewmodel.ReferralViewModel
import com.doubtnutapp.liveclass.viewmodel.ShareFeedData
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.showToast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_payment_successful_bottom_sheet.*
import javax.inject.Inject

class PaymentSuccessfulBottomSheet : BottomSheetDialogFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var userPreference: UserPreference

    private var data: ReferralData? = null

    private lateinit var viewModel: ReferralViewModel
    private lateinit var dnrRewardViewModel: DnrRewardViewModel

    private val assortmentType: String? by lazy { arguments?.getString(ASSORTMENT_TYPE) }
    private val assortmentId: String? by lazy { arguments?.getString(ASSORTMENT_ID) }

    companion object {
        const val TAG = "PaymentSuccessfulBottom"
        const val TYPE = "type"
        const val TYPE_PAYMENT = "payment"
        private const val ASSORTMENT_TYPE = "assortment_type"
        private const val ASSORTMENT_ID = "assortment_id"

        fun newInstance(assortmentType: String?, assortmentId: String?) = PaymentSuccessfulBottomSheet().apply {
            arguments = Bundle().apply {
                putString(ASSORTMENT_TYPE, assortmentType)
                putString(ASSORTMENT_ID, assortmentId)
            }
        }

    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_payment_successful_bottom_sheet, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
    }

    override fun getTheme(): Int {
        return R.style.BaseBottomSheetDialog
    }

    private fun initUI() {

        when (assortmentType) {
            AssortmentType.COURSE.type -> {
                layout_payment1.setVisibleState(true)
                layout_payment2.setVisibleState(false)
            }
            AssortmentType.PDF.type, AssortmentType.VIDEO.type -> {
                layout_payment1.setVisibleState(false)
                layout_payment2.setVisibleState(true)
            }
        }

        viewModel = viewModelProvider<ReferralViewModel>(viewModelFactory)
        dnrRewardViewModel = viewModelProvider<DnrRewardViewModel>(viewModelFactory)
        setUpObserver()
        setUpListeners()
        viewModel.getReferralData(TYPE_PAYMENT, assortmentType, assortmentId)
    }

    override fun onDestroy() {
        super.onDestroy()
        DoubtnutApp.INSTANCE.bus()?.send(UpdateExploreEvent())
    }

    private fun setUpListeners() {
        btnCross.setOnClickListener {
            dialog?.dismiss()
        }

        /**
         * for layout_payment1
         * */
        tvCouponCode.setOnClickListener {
            copyText(requireContext(), data?.couponCode.orEmpty())
            showToast(context, "Code Copied")
            analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.REFERRAL_COPY, hashMapOf(
                    EventConstants.SOURCE to EventConstants.PAYMENT_PAGE
            ), ignoreSnowplow = true))
        }
        btnInvite.setOnClickListener {
            val text = data?.inviteMessage.orEmpty()
            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                `package` = "com.whatsapp"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.REFERRAL_SHARE_WHATSAPP, hashMapOf(
                    EventConstants.SOURCE to EventConstants.PAYMENT_PAGE
            ), ignoreSnowplow = true))
            startActivity(intent)
            dialog?.dismiss()
        }

        btnShare.setOnClickListener {
            //dnfeed
            viewModel.postFeed(data?.feedMessage)
            analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.REFERRAL_SHARE_FEED, hashMapOf(
                    EventConstants.SOURCE to EventConstants.PAYMENT_PAGE
            ), ignoreSnowplow = true))
        }

        /**
         * for layout_payment2
         * */
        btnExplore.setOnClickListener {
            deeplinkAction.performAction(requireContext(), data?.exploreTextDeeplink)
            dialog?.dismiss()
        }

        btnOpen.setOnClickListener {
            deeplinkAction.performAction(requireContext(), data?.buttonDeeplink)
            dialog?.dismiss()
        }

    }

    private fun copyText(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        val clip = ClipData.newPlainText("", text)
        clipboard?.setPrimaryClip(clip)
    }


    private fun setUpObserver() {
        viewModel.referralData.observeK(this,
                this::onPaymentSuccess,
                this::onApiError,
                this::unAuthorizeUserError,
                this::ioExceptionHandler,
                this::updateProgressBarState)

        viewModel.shareFeedData.observeK(this,
                this::onPostFeedSuccess,
                this::onApiError,
                this::unAuthorizeUserError,
                this::ioExceptionHandler,
                this::updateProgressBarState)
    }

    private fun onPostFeedSuccess(data: ShareFeedData) {
        showToast(context, "Shared on feed")
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun ioExceptionHandler() {
        lifecycleScope.launchWhenResumed {
            if (NetworkUtils.isConnected(requireContext()).not()) {
                toast(getString(R.string.string_noInternetConnection))
            } else {
                toast(getString(R.string.somethingWentWrong))
            }
        }
    }

    private fun updateProgressBarState(state: Boolean) {
        progressBar.setVisibleState(state)
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(childFragmentManager, "BadRequestDialog")
    }

    private fun onPaymentSuccess(data: ReferralData) {
        this.data = data
        setData()
        DoubtnutApp.INSTANCE.bus()?.send(CloseEvent())
    }

    private fun setData() {
        if (data == null)
            return

        // DNR region start
        if (assortmentType.isNotNullAndNotEmpty() && assortmentId.isNotNullAndNotEmpty()) {
            dnrRewardViewModel.claimReward(
                DnrCoursePurchaseReward(
                    assortmentId = assortmentId.orEmpty(),
                    assortmentType = assortmentType.orEmpty(),
                    type = assortmentType.orEmpty()
                )
            )
        }
        // DNR region end

        when (assortmentType) {
            AssortmentType.COURSE.type -> {
                btnTitle.text = data?.title.orEmpty()
                tvSubTitle.text = data?.subTitle.orEmpty()
                ivGift.loadImage(data?.imageUrl, R.drawable.ic_icon_small_gift)
                tvHeader.text = data?.header.orEmpty()
                tvDescription.text = data?.description.orEmpty()
                tvCouponText.text = data?.couponText.orEmpty()
                tvCouponCode.text = data?.couponCode.orEmpty()
                btnInvite.text = data?.buttonText.orEmpty()
                btnShare.text = data?.shareText.orEmpty()
            }
            AssortmentType.PDF.type, AssortmentType.VIDEO.type -> {
                ivSuccess.loadImage(data?.imageUrl, R.drawable.ic_tick)
                tvHeader2.text = data?.header.orEmpty()
                tvDescription2.text = data?.description.orEmpty()
                btnOpen.text = data?.buttonText.orEmpty()
                btnExplore.text = data?.exploreText.orEmpty()
            }
        }
    }

    enum class AssortmentType(val type: String) {
        COURSE("course"),
        PDF("resource_pdf"),
        VIDEO("resource_video")
    }
}
