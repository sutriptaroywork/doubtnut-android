package com.doubtnutapp.vipplan.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.EventConstants.APB_CHECKOUT_CLICKED
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.activityViewModelProvider
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.WalletAmountUpdateEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.*
import com.doubtnutapp.databinding.FragmentCheckoutBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.domain.payment.entities.*
import com.doubtnutapp.feed.view.ImagesPagerActivity
import com.doubtnutapp.payment.ApbCashPaymentActivity
import com.doubtnutapp.paymentv2.ui.CouponBottomSheetDialogFragment
import com.doubtnutapp.qrpayment.QrPaymentActivity
import com.doubtnutapp.sharing.WhatsAppSharing
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.vipplan.viewmodel.VipPlanViewModel
import com.doubtnutapp.wallet.WalletActivity
import com.razorpay.Razorpay
import com.uxcam.UXCam
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.apb_banner_item.view.*
import kotlinx.android.synthetic.main.list_dialog.view.*
import kotlinx.android.synthetic.main.payment_options_item.view.*
import kotlinx.android.synthetic.main.preferred_method_item.view.*
import java.util.*
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 03/10/20.
 * Revamp by Akshat Jindal on 05/03/21
 */
class CheckoutFragment : BaseBindingFragment<VipPlanViewModel, FragmentCheckoutBinding>(),
    ActionPerformer {

    companion object {
        const val TAG = "CheckoutFragment"

        const val PAYMENT_FOR = "payment_for"
        const val VARIANT_ID = "variant_id"
        const val AMOUNT = "amount"
        const val COUPON_CODE = "coupon_code"
        const val IS_WALLET = "use_wallet_cash"
        const val USE_WALLET_REWARD = "use_wallet_reward"
        const val REMOVE_COUPON = "remove_coupon"
        const val PAYMENT_BODY = "payment_body"
        const val COURSE_PACKAGE = "course_package"
        const val PAYMENT_LINK = "payment_link"
        const val UPI_INTENT = "upi_intent"

        const val UPI = "upi"
        const val UPI_COLLECT = "upi_collect"
        const val UPI_SELECT = "upi_select"
        const val NETBANKING = "netbanking"
        const val CARD = "card"
        const val WALLET = "wallet"
        const val DOUBT = "doubt"
        const val SWITCH_ASSORTMENT = "switch_assortment"

        fun newInstance(paymentStartBody: PaymentStartBody) = CheckoutFragment().apply {
            arguments = Bundle().apply {
                putParcelable(PAYMENT_BODY, paymentStartBody)
            }
        }
    }

    private var actionPerformer: ActionPerformer? = null
    private var walletObserver: Disposable? = null
    private var codInfo: CodInfo? = null

    @Inject
    lateinit var whatsAppSharing: WhatsAppSharing

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    private var checkoutData: CheckoutData? = null
    private var listener: CheckoutListener? = null
    private var source: String? = null

    private var paymentOptionsAdapter: PaymentOptionsAdapter? = null
    private var paymentOptionsAdapterPreferred: PaymentOptionsAdapter? = null

    private var paymentStartBody: PaymentStartBody? = null

    private var isWallet: Boolean = true
    private var isReward: Boolean = true
    private var couponCode: String = ""
    private var isCouponApplied = false

    private var paymentActivityBody: PaymentActivityBody? = null
    private var isWalletSufficient: Boolean = false
    private var isPaymentMethodSelected: Boolean = false

    private var mediaPlayer: MediaPlayer? = null
    private var voiceUrl: String? = null
    private var switchAssortmentId: String = ""

    fun setActionListener(actionPerformer: ActionPerformer) {
        this.actionPerformer = actionPerformer
    }


    private fun setUpObserver() {

        walletObserver = DoubtnutApp.INSTANCE.bus()?.toObservable()?.subscribe { event ->
            if (event is WalletAmountUpdateEvent) {
                viewModel.getPackagePaymentInfo(
                    hashMapOf(
                        PAYMENT_FOR to paymentStartBody?.paymentFor.orEmpty(),
                        VARIANT_ID to paymentStartBody?.paymentStartInfo?.variantId.orEmpty(),
                        AMOUNT to paymentStartBody?.paymentStartInfo?.amount.orEmpty(),
                        COUPON_CODE to couponCode,
                        IS_WALLET to isWallet,
                        USE_WALLET_REWARD to isReward,
                        SWITCH_ASSORTMENT to switchAssortmentId
                    )
                )
            } else if (event is CouponApplied) {
                couponCode = event.couponCode
                viewModel.getPackagePaymentInfo(
                    hashMapOf(
                        PAYMENT_FOR to paymentStartBody?.paymentFor.orEmpty(),
                        VARIANT_ID to paymentStartBody?.paymentStartInfo?.variantId.orEmpty(),
                        AMOUNT to paymentStartBody?.paymentStartInfo?.amount.orEmpty(),
                        COUPON_CODE to couponCode,
                        IS_WALLET to isWallet,
                        USE_WALLET_REWARD to isReward,
                        SWITCH_ASSORTMENT to switchAssortmentId
                    )
                )
            }
        }

        viewModel.paymentLinkInoLiveData.observe(viewLifecycleOwner, Observer {
            val info: PaymentLinkInfo? = it.peekContent()
            if (info?.payLink != null) {
                val shouldShowSaleDialog = info.payLink!!.shouldShowSaleDialog ?: false
                val nudgeId = info.payLink!!.nudgeId ?: 0
                val nudgeMaxCount = info.payLink!!.nudgeCount ?: 0
                val savedNudgeId = defaultPrefs().getInt(Constants.NUDGE_ID_CHECKOUT, 0)
                if (savedNudgeId == 0 || savedNudgeId != nudgeId) {
                    defaultPrefs().edit().putInt(Constants.NUDGE_ID_CHECKOUT, nudgeId).apply()
                    defaultPrefs().edit().putInt(Constants.NUDGE_CHECKOUT_COUNT, 0).apply()
                }
                actionPerformer?.performAction(
                    ShowSaleDialog(
                        shouldShowSaleDialog,
                        nudgeId,
                        nudgeMaxCount,
                        TAG
                    )
                )
                mBinding?.textViewPaymentLinkInfoTitle?.text = info.payLink!!.title
                mBinding?.tvShareViaTitle?.text = info.payLink?.textShareTitle.orEmpty()
                mBinding?.tvShareViaSubTitle?.text = info.payLink?.textShareSubTitle.orEmpty()
                mBinding?.layoutSharePaymentLink?.text = info.payLink?.actionButtonText.orEmpty()

                if (!info.payLink!!.description.isNullOrEmpty()) {
                    mBinding?.textViewPaymentLinkDescription?.text = info.payLink!!.description!![0]
                    if (info.payLink!!.description!!.size > 1) {
                        mBinding?.textViewPaymentLinkKnowMore?.show()
                    } else {
                        mBinding?.textViewPaymentLinkKnowMore?.hide()
                    }
                } else {
                    mBinding?.textViewPaymentLinkDescription?.hide()
                    mBinding?.textViewPaymentLinkKnowMore?.hide()
                }

                mBinding?.textViewTitlePaymentLink?.text = info.title.orEmpty()

                mBinding?.textViewTitlePaymentLink?.show()
                mBinding?.layoutPaymentLink?.show()
            } else {
                mBinding?.textViewTitlePaymentLink?.hide()
                mBinding?.layoutPaymentLink?.hide()
            }

            if (info?.qrInfo != null) {
                mBinding?.layoutQr?.show()
                mBinding?.tvQrTitle?.text = info.qrInfo?.title.orEmpty()
                mBinding?.tvQrSubTitle?.text = info.qrInfo?.subTitle.orEmpty()
                mBinding?.buttonViewQr?.text = info.qrInfo?.actionButtonText.orEmpty()
            } else {
                mBinding?.layoutQr?.hide()
            }

            if (info?.bbpsInfo != null) {
                mBinding?.layoutBill?.show()
                mBinding?.tvBillTitle?.text = info.bbpsInfo?.title.orEmpty()
                mBinding?.tvBillSubTitle?.text = info.bbpsInfo?.subTitle.orEmpty()
                mBinding?.buttonViewBill?.text = info.bbpsInfo?.actionButtonText.orEmpty()
                mBinding?.buttonViewBill?.setOnClickListener {
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.BBPS_CLICK, hashMapOf(
                                EventConstants.VARIANT_ID to paymentStartBody?.paymentStartInfo?.variantId.orEmpty(),
                                EventConstants.PAYMENT_COUPON_ENTERED_VALUE to paymentStartBody?.paymentStartInfo?.couponCode.orEmpty(),
                                EventConstants.PAYMENT_FOR to paymentStartBody?.paymentFor.orEmpty(),
                                EventConstants.AMOUNT to paymentStartBody?.paymentStartInfo?.amount.orEmpty()
                            ), ignoreSnowplow = true
                        )
                    )
                    deeplinkAction.performAction(requireContext(), info.bbpsInfo?.deeplink)
                }
            } else {
                mBinding?.layoutBill?.hide()
            }
        })

        mBinding?.tvWhatIsWallet?.setOnClickListener {
            startActivity(WalletActivity.getStartIntent(requireContext()))
        }

        mBinding?.textViewPaymentLinkKnowMore?.setOnClickListener {
            val info = viewModel.paymentLinkInoLiveData.value?.peekContent()
            if (info != null && !info.payLink?.description.isNullOrEmpty()) {
                if (mBinding?.textViewPaymentLinkKnowMore?.text == Constants.SHOW_LESS) {
                    mBinding?.textViewPaymentLinkDescription?.text = info.payLink?.description!![0]
                    mBinding?.textViewPaymentLinkKnowMore?.text = Constants.KNOW_MORE
                    mBinding?.ivDropDown?.setImageResource(R.drawable.ic_arrow_down_filled)
                } else {
                    var description = ""
                    var lineNum = 1
                    for (line in info.payLink?.description!!) {
                        if (lineNum < info.payLink?.description!!.size) {
                            description += line + "\n\n"
                        } else {
                            description += line
                        }
                        lineNum++
                    }
                    mBinding?.textViewPaymentLinkDescription?.text = description
                    mBinding?.textViewPaymentLinkKnowMore?.text = Constants.SHOW_LESS
                    mBinding?.ivDropDown?.setImageResource(R.drawable.ic_up_filled)
                    mBinding?.scrollView?.post {
                        mBinding?.scrollView?.fullScroll(View.FOCUS_DOWN)
                    }
                }
            } else {
                it.hide()
            }
        }

        mBinding?.layoutSharePaymentLink?.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.SHARE_PAYMENT_LINK_CLICK,
                    ignoreSnowplow = true
                )
            )
            viewModel.requestPaymentLink(paymentStartBody?.apply {
                method = PAYMENT_LINK
                paymentStartInfo?.apply {
                    useWalletCash = isWallet
                    useWalletReward = isReward
                    couponCode = this@CheckoutFragment.couponCode
                }
            })

            viewModel.paymentLinkLiveData.observe(viewLifecycleOwner, Observer {
                val paymentLinkData = it?.getContentIfNotHandled()
                if (paymentLinkData != null) {
                    if (!paymentLinkData.errorMessage.isNullOrEmpty()) {
                        toast(paymentLinkData.errorMessage!!, Toast.LENGTH_SHORT)
                    }
                    if (paymentLinkData.shareMessage.isNullOrEmpty()) {
                        toast(
                            "Error in creating payment link, Please try later",
                            Toast.LENGTH_SHORT
                        )
                    } else {
                        sharePaymentLink(paymentLinkData.shareMessage)
                    }
                }
            })
        }

        mBinding?.buttonViewQr?.setOnClickListener {
            val qrIntent = QrPaymentActivity.getStartIntent(requireContext(),
                paymentStartBody?.apply {
                    method = UPI_INTENT
                    paymentStartInfo?.apply {
                        useWalletCash = isWallet
                        useWalletReward = isReward
                        couponCode = this@CheckoutFragment.couponCode
                    }
                })
            startActivity(qrIntent)
        }

        mBinding?.imageViewBack?.setOnClickListener {
            activity?.onBackPressed()
        }

        viewModel.isLoading.observe(viewLifecycleOwner, {
            mBinding?.progressBar?.setVisibleState(it)
        })

        viewModel.checkoutLiveData.observe(viewLifecycleOwner, {
            checkoutData = it.peekContent()
            setUp()
        })

        viewModel.packagePaymentInfo.observe(viewLifecycleOwner, {
            codInfo = it.codInfo
            if (paymentOptionsAdapter != null) {
                val requiredIndex: Int? =
                    paymentOptionsAdapter?.paymentInfoList?.indexOfFirst { paymentInformation ->
                        paymentInformation.method == PaymentOptionsAdapter.COD
                    }
                if (requiredIndex != null && requiredIndex != -1) {
                    paymentOptionsAdapter?.paymentInfoList?.filter { info ->
                        info.method == PaymentOptionsAdapter.COD
                    }?.map { mapInfo ->
                        mapInfo.codInfo = codInfo
                    }
                    paymentOptionsAdapter?.notifyItemChanged(requiredIndex)
                }
            }
            if (paymentOptionsAdapterPreferred != null) {
                val requiredIndex: Int? =
                    paymentOptionsAdapterPreferred?.paymentInfoList?.indexOfFirst { paymentInformation ->
                        paymentInformation.method == PaymentOptionsAdapter.COD
                    }
                if (requiredIndex != null && requiredIndex != -1) {
                    paymentOptionsAdapterPreferred?.paymentInfoList?.filter { info ->
                        info.method == PaymentOptionsAdapter.COD
                    }?.map { mapInfo ->
                        mapInfo.codInfo = codInfo
                    }
                    paymentOptionsAdapterPreferred?.notifyItemChanged(requiredIndex)
                }
            }
            updatePackageDetails(it)
        })
    }


    private fun updatePackageDetails(data: PackagePaymentInfo?) {

        if (data == null || data.cartInfoList.isNullOrEmpty()) {
            mBinding?.textViewTitle?.hide()
            mBinding?.layoutPaymentDetail?.hide()
            return
        } else {
            mBinding?.textViewTitle?.show()
            mBinding?.layoutPaymentDetail?.show()
        }

        when {
            (data.wallet == null || data.wallet?.showWallet == 0) -> {
                isWallet = false
                isReward = false
                mBinding?.layoutWallet?.hide()
            }
            else -> {
                mBinding?.layoutWallet?.show()

                mBinding?.tvWalletTitle?.text = data.wallet?.title.orEmpty()
                mBinding?.tvWhatIsWallet?.text = data.wallet?.deeplinkText.orEmpty()

                if (data.wallet?.cashAmount == null) {
                    isWallet = false
                    mBinding?.checkBoxCashBalance?.hide()
                    mBinding?.ivCashBalance?.hide()
                    mBinding?.tvWalletCashBalanceValue?.hide()
                } else {
                    mBinding?.checkBoxCashBalance?.show()
                    mBinding?.ivCashBalance?.show()
                    mBinding?.tvWalletCashBalanceValue?.show()
                }

                if (data.wallet?.rewardAmount == null) {
                    isReward = false
                    mBinding?.layoutReward?.hide()
                } else {
                    mBinding?.layoutReward?.show()
                }

                mBinding?.titleToolTipCash?.text = data.wallet?.cashAmount?.tooltipText.orEmpty()
                if (data.wallet?.cashAmount?.tooltipText.isNullOrBlank()) {
                    mBinding?.ivCashBalance?.hide()
                    mBinding?.ivCashBalance?.setOnClickListener {
                    }
                } else {
                    mBinding?.ivCashBalance?.show()
                    mBinding?.ivCashBalance?.setOnClickListener {
                        mBinding?.groupToolTipReward?.hide()
                        mBinding?.groupToolTipCash?.show()
                    }
                }

                mBinding?.titleToolTipReward?.text =
                    data.wallet?.rewardAmount?.tooltipText.orEmpty()
                if (data.wallet?.rewardAmount?.tooltipText.isNullOrBlank()) {
                    mBinding?.ivRewardBalance?.hide()
                    mBinding?.ivRewardBalance?.setOnClickListener {
                    }
                } else {
                    mBinding?.ivRewardBalance?.show()
                    mBinding?.ivRewardBalance?.setOnClickListener {
                        mBinding?.groupToolTipCash?.hide()
                        mBinding?.groupToolTipReward?.show()
                    }
                }

                mBinding?.tvWalletBalance?.text = data.wallet?.totalAmount?.name.orEmpty()
                mBinding?.tvWalletBalanceValue?.text = data.wallet?.totalAmount?.value.orEmpty()

                mBinding?.checkBoxCashBalance?.isChecked = isWallet
                mBinding?.checkBoxCashBalance?.text = data.wallet?.cashAmount?.name.orEmpty()
                mBinding?.tvWalletCashBalanceValue?.text = data.wallet?.cashAmount?.value.orEmpty()
                mBinding?.tvAddMoney?.setVisibleState(data.wallet?.showAddMoney == true)

                mBinding?.checkBoxRewardBalance?.isChecked = isReward
                mBinding?.checkBoxRewardBalance?.text = data.wallet?.rewardAmount?.name.orEmpty()
                mBinding?.tvWalletRewardBalanceValue?.text =
                    data.wallet?.rewardAmount?.value.orEmpty()
                mBinding?.tvRewardTitle?.text = data.wallet?.rewardAmount?.title.orEmpty()
                mBinding?.tvRewardDesc?.text = data.wallet?.rewardAmount?.description.orEmpty()
            }
        }

        mBinding?.checkBoxCashBalance?.setOnCheckedChangeListener { _, isChecked ->
            isWallet = isChecked
            viewModel.getPackagePaymentInfo(
                hashMapOf(
                    PAYMENT_FOR to paymentStartBody?.paymentFor.orEmpty(),
                    VARIANT_ID to paymentStartBody?.paymentStartInfo?.variantId.orEmpty(),
                    AMOUNT to paymentStartBody?.paymentStartInfo?.amount.orEmpty(),
                    COUPON_CODE to couponCode,
                    IS_WALLET to isWallet,
                    USE_WALLET_REWARD to isReward,
                    SWITCH_ASSORTMENT to switchAssortmentId
                )
            )
        }

        mBinding?.checkBoxRewardBalance?.setOnCheckedChangeListener { _, isChecked ->
            isReward = isChecked
            viewModel.getPackagePaymentInfo(
                hashMapOf(
                    PAYMENT_FOR to paymentStartBody?.paymentFor.orEmpty(),
                    VARIANT_ID to paymentStartBody?.paymentStartInfo?.variantId.orEmpty(),
                    AMOUNT to paymentStartBody?.paymentStartInfo?.amount.orEmpty(),
                    COUPON_CODE to couponCode,
                    IS_WALLET to isWallet,
                    USE_WALLET_REWARD to isReward,
                    SWITCH_ASSORTMENT to switchAssortmentId
                )
            )
        }

        if (data.couponInfo == null || data.couponInfo?.status == null) {
            mBinding?.groupCoupon?.hide()
        } else {
            mBinding?.groupCoupon?.show()
            mBinding?.textViewTitleCouponCode?.text = data.couponInfo?.title
            mBinding?.textViewApply?.text = data.couponInfo?.ctaButton
            mBinding?.ivCoupon?.loadImage(data.couponInfo?.couponImageUrl)

            isCouponApplied = data.couponInfo?.status ?: false
            couponCode = data.couponInfo?.couponCode.toString()

            if (data.couponInfo?.status == true) {
                mBinding?.textViewReferral?.text = couponCode
            } else {
                mBinding?.textViewReferral?.text = data.couponInfo?.placeholderText.toString()
            }

            mBinding?.layoutCoupon?.setOnClickListener {

                val hashMap = hashMapOf<String, Any>(
                    PAYMENT_FOR to paymentStartBody?.paymentFor.orEmpty(),
                    VARIANT_ID to paymentStartBody?.paymentStartInfo?.variantId.orEmpty(),
                    AMOUNT to paymentStartBody?.paymentStartInfo?.amount.orEmpty(),
                    IS_WALLET to isWallet,
                    USE_WALLET_REWARD to isReward,
                    SWITCH_ASSORTMENT to switchAssortmentId
                )

                if (isCouponApplied) {
                    //remove coupon logic
                    hashMap[COUPON_CODE] = ""
                    viewModel.getPackagePaymentInfo(hashMap.apply {
                        put(REMOVE_COUPON, true)
                    })
                } else {
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.COUPON_VIEW,
                            ignoreSnowplow = true
                        )
                    )
                   CouponBottomSheetDialogFragment.newInstance(hashMap = hashMap).show(
                        parentFragmentManager,
                        CouponBottomSheetDialogFragment.TAG
                    )
                }
            }
        }

        mBinding?.textViewTitle?.text = data.title
        mBinding?.buttonPay?.text = data.actionButtonText
        mBinding?.textViewDetails?.text = data.paymentDetailsText
        val currencySymbol = "₹ "

        mBinding?.textViewDetails?.setOnClickListener {
            mBinding?.scrollView?.fullScroll(View.FOCUS_DOWN)
        }

        if (data.cartInfoList!![0].value.isNullOrEmpty()) {
            mBinding?.textViewOriginalLabel?.hide()
            mBinding?.textViewOriginalAmount?.hide()
        } else {
            mBinding?.textViewOriginalLabel?.show()
            mBinding?.textViewOriginalAmount?.show()
            mBinding?.textViewOriginalLabel?.text = data.cartInfoList!![0].name
            mBinding?.textViewOriginalAmount?.text = data.cartInfoList!![0].value
            mBinding?.textViewOriginalAmount?.setTextColor(Color.parseColor(data.cartInfoList!![0].color))
        }

        if (data.cartInfoList!![1].value.isNullOrEmpty()) {
            mBinding?.textViewDiscountLabel?.hide()
            mBinding?.textViewDiscountAmount?.hide()
        } else {
            mBinding?.textViewDiscountLabel?.show()
            mBinding?.textViewDiscountAmount?.show()
            mBinding?.textViewDiscountLabel?.text = data.cartInfoList!![1].name
            mBinding?.textViewDiscountAmount?.text = data.cartInfoList!![1].value
            mBinding?.textViewDiscountAmount?.setTextColor(Color.parseColor(data.cartInfoList!![1].color))
        }

        if (data.cartInfoList!![2].value.isNullOrEmpty()) {
            mBinding?.textViewCouponDiscountLabel?.hide()
            mBinding?.textViewCouponDiscountAmount?.hide()
        } else {
            mBinding?.textViewCouponDiscountLabel?.show()
            mBinding?.textViewCouponDiscountAmount?.show()
            mBinding?.textViewCouponDiscountLabel?.text = data.cartInfoList!![2].name
            mBinding?.textViewCouponDiscountAmount?.text = data.cartInfoList!![2].value
            mBinding?.textViewCouponDiscountAmount?.setTextColor(Color.parseColor(data.cartInfoList!![2].color))
        }

        if (data.cartInfoList!![3].value.isNullOrEmpty()) {
            mBinding?.textViewWalletDiscountLabel?.hide()
            mBinding?.textViewWalletDiscountAmount?.hide()
        } else {
            mBinding?.textViewWalletDiscountLabel?.show()
            mBinding?.textViewWalletDiscountAmount?.show()
            mBinding?.textViewWalletDiscountLabel?.text = data.cartInfoList!![3].name
            mBinding?.textViewWalletDiscountAmount?.text = data.cartInfoList!![3].value
            mBinding?.textViewWalletDiscountAmount?.setTextColor(Color.parseColor(data.cartInfoList!![3].color))
        }

        if (data.cartInfoList!![4].value.isNullOrEmpty()) {
            mBinding?.textViewAmountToPayLabel?.hide()
            mBinding?.textViewAmountToPay?.hide()
        } else {
            mBinding?.textViewAmountToPayLabel?.show()
            mBinding?.textViewAmountToPay?.show()
            mBinding?.textViewAmountToPayLabel?.text = data.cartInfoList!![4].name
            mBinding?.textViewAmountToPay?.text = currencySymbol + data.cartInfoList!![4].value
            mBinding?.textViewAmountToPay?.setTextColor(Color.parseColor(data.cartInfoList!![4].color))

            mBinding?.textViewAmount?.text = currencySymbol + data.cartInfoList!![4].value
        }

        /**
         * final amount is 0 or one of the payment methods is selected
         */
        when {
            data.cartInfoList!![4].value == "0" -> {
                isWalletSufficient = true
                mBinding?.buttonPay?.enable()
                mBinding?.buttonPay?.setBackgroundColor(resources.getColor(R.color.orange_eb532c))
            }
            isPaymentMethodSelected -> {
                isWalletSufficient = false
                mBinding?.buttonPay?.enable()
                mBinding?.buttonPay?.setBackgroundColor(resources.getColor(R.color.orange_eb532c))
            }
            else -> {
                isWalletSufficient = false
                mBinding?.buttonPay?.disable()
                mBinding?.buttonPay?.setBackgroundColor(resources.getColor(R.color.grey_cbcbcb))
            }
        }

        mBinding?.buttonPay?.setOnClickListener {
            if (paymentStartBody?.method == PAYMENT_LINK || paymentStartBody?.method == UPI_INTENT) {
                paymentStartBody?.method =
                    paymentOptionsAdapter?.paymentInfoList?.firstOrNull { it.isSelected }?.method.orEmpty()
            }
            paymentStartBody?.paymentStartInfo?.useWalletCash = isWallet
            paymentStartBody?.paymentStartInfo?.useWalletReward = isReward
            paymentStartBody?.paymentStartInfo?.couponCode = couponCode
            paymentStartBody?.paymentStartInfo?.switchAssortmentId = switchAssortmentId
            if (paymentActivityBody == null) {
                paymentActivityBody = PaymentActivityBody(
                    paymentStartBody = paymentStartBody!!,
                    cardDetails = null,
                    method = "",
                    type = "",
                    deeplink = null,
                    upi = null,
                    upiPackage = null
                )
            }
            listener?.onPayButtonClick(paymentActivityBody!!)
            val event = AnalyticsEvent(
                EventConstants.PAYMENT_PAY_NOW,
                hashMapOf<String, Any>()
                    .apply {
                        put(
                            EventConstants.VARIANT_ID,
                            checkoutData?.orderInfo?.variantId.orEmpty()
                        )
                    }
            )
            analyticsPublisher.publishEvent(event)
            val countToSendEvent: Int = Utils.getCountToSend(
                RemoteConfigUtils.getEventInfo(),
                EventConstants.PAYMENT_PAY_NOW
            )
            val eventCopy = event.copy()
            repeat((0 until countToSendEvent).count()) {
                analyticsPublisher.publishBranchIoEvent(eventCopy)
            }
        }
    }

    private fun sharePaymentLink(shareMessage: String?) {
        whatsAppSharing.shareOnWhatsApp(
            context = requireContext(),
            imageUrl = "",
            imageFilePath = null,
            sharingMessage = shareMessage
        )
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.PAYMENT_LINK_SHARED,
                ignoreSnowplow = true
            )
        )
    }

    private fun setUp() {

        val data = checkoutData ?: return
        setUpRecyclerViews()

        mBinding?.toolbarTitle?.text = data.title
        mBinding?.textViewPackageName?.text = data.orderInfo?.title
        mBinding?.textViewEmiDetails?.text = data.orderInfo?.description
        mBinding?.textViewPackageDuration?.text = data.orderInfo?.packageDuration
        mBinding?.textViewTitlePaymentOption?.text = data.paymentMethodTitle.orEmpty()

        mBinding?.btnVoiceInstructions?.text = data.checkoutAudio?.title.orEmpty()

        if (data.checkoutAudio == null) {
            mBinding?.btnVoiceInstructions?.hide()
        } else {
            mBinding?.btnVoiceInstructions?.show()
            voiceUrl = data.checkoutAudio?.audioUrl.orEmpty()
            if (mediaPlayer == null) {
                setupMediaPlayer()
            } else {
                handleMediaPlayer()
            }
        }

        if (data.paymentLink == null) {
            mBinding?.layoutPaymentLink?.hide()
        } else {
            mBinding?.layoutPaymentLink?.show()
        }

        if (data.paymentHelp != null) {
            mBinding?.tvHelp?.show()
            mBinding?.tvHelp?.text = data.paymentHelp?.pageTitle.orEmpty()
            mBinding?.tvHelp?.setOnClickListener {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.CC_HELP_CLICK,
                        ignoreSnowplow = true
                    )
                )
                startActivity(
                    PaymentHelpActivity.getStartIntent(
                        requireContext(),
                        data.paymentHelp!!
                    )
                )
            }
            if (!data.paymentHelp?.pageTitleTooltip.isNullOrBlank()) {
                mBinding?.group?.show()
                mBinding?.titleToolTip?.text = data.paymentHelp?.pageTitleTooltip.orEmpty()
            } else {
                mBinding?.group?.hide()
            }
            mBinding?.ivClose?.setOnClickListener {
                mBinding?.group?.hide()
            }
        } else {
            mBinding?.tvHelp?.hide()
            mBinding?.group?.hide()
        }

        mBinding?.apbBannerRecyclerView?.setVisibleState(!data.APBBannerList.isNullOrEmpty())
        mBinding?.textViewTitlePaymentOption?.setVisibleState(!data.paymentInfoList.isNullOrEmpty())
        mBinding?.paymentOptionRecyclerView?.setVisibleState(!data.paymentInfoList.isNullOrEmpty())

    }

    fun setListener(checkoutListener: CheckoutListener) {
        listener = checkoutListener
    }


    private fun handleMediaPlayer() {
        when (mediaPlayer?.isPlaying) {
            true -> {
                mediaPlayer?.pause()
                mBinding?.btnVoiceInstructions?.apply {
                    setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_volume_off, 0, 0, 0)
                    compoundDrawables.getOrNull(0)?.setTint(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.redTomato
                        )
                    )

                }
            }
            false -> {
                mediaPlayer?.start()
                mBinding?.btnVoiceInstructions?.apply {
                    setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_volume, 0, 0, 0)
                    compoundDrawables.getOrNull(0)?.setTint(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.redTomato
                        )
                    )
                }
            }
            else -> {
                releaseMediaPlayer()
                setupMediaPlayer()
            }
        }
    }

    private fun setupMediaPlayer() {
        if (voiceUrl.isNullOrBlank() || UserUtil.getIsAnonymousLogin())
            return

        mediaPlayer = null
        mediaPlayer = MediaPlayer().apply {
            setDataSource(voiceUrl)
            prepareAsync()
        }

        mediaPlayer?.setOnPreparedListener {
            if (mediaPlayer != null) {
                mediaPlayer?.start()
            }
        }
    }

    private fun releaseMediaPlayer() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onStop() {
        super.onStop()
        releaseMediaPlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseMediaPlayer()
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.PAYMENT_CHECOUT_BACKPRESSED, hashMapOf(
                    EventConstants.VARIANT_ID to paymentStartBody?.paymentStartInfo?.variantId.orEmpty(),
                    EventConstants.PAYMENT_COUPON_ENTERED_VALUE to paymentStartBody?.paymentStartInfo?.couponCode.orEmpty(),
                    EventConstants.PAYMENT_FOR to paymentStartBody?.paymentFor.orEmpty(),
                    EventConstants.AMOUNT to paymentStartBody?.paymentStartInfo?.amount.orEmpty()
                ), ignoreBranch = false, ignoreSnowplow = true
            )
        )
        walletObserver?.dispose()
    }

    private fun setUpRecyclerViews() {
        val data = checkoutData ?: return
        data.paymentInfoList?.map {
            if (it.method == PaymentOptionsAdapter.COD) {
                it.codInfo = codInfo
            }
        }

        data.preferredPaymentMethods?.map {
            if (it.method == PaymentOptionsAdapter.COD) {
                it.codInfo = codInfo
            }
        }

        mBinding?.apbBannerRecyclerView?.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        mBinding?.apbBannerRecyclerView?.adapter = ApbBannerAdapter(
            data.APBBannerList.orEmpty(),
            deeplinkAction,
            analyticsPublisher,
            APB_CHECKOUT_CLICKED
        )

        mBinding?.paymentOptionRecyclerView?.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        paymentOptionsAdapter =
            PaymentOptionsAdapter(
                data.paymentInfoList.orEmpty(),
                analyticsPublisher,
                deeplinkAction,
                this,
                false
            )
        mBinding?.paymentOptionRecyclerView?.adapter = paymentOptionsAdapter
        mBinding?.textViewTitlePaymentOptionPreferred?.text = data.preferredPaymentTitle.orEmpty()
        if (data.preferredPaymentMethods.isNullOrEmpty()) {
            mBinding?.textViewTitlePaymentOptionPreferred?.isVisible = false
            mBinding?.paymentOptionRecyclerViewPreferred?.isVisible = false
        } else {
            mBinding?.textViewTitlePaymentOptionPreferred?.isVisible = true
            mBinding?.paymentOptionRecyclerViewPreferred?.isVisible = true
        }

        mBinding?.paymentOptionRecyclerViewPreferred?.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        paymentOptionsAdapterPreferred =
            PaymentOptionsAdapter(
                data.preferredPaymentMethods.orEmpty(),
                analyticsPublisher,
                deeplinkAction,
                this,
                true
            )
        mBinding?.paymentOptionRecyclerViewPreferred?.adapter = paymentOptionsAdapterPreferred
    }

    override fun performAction(action: Any) {

        when (action) {
            is PaymentMethodClicked -> {
                if (action.isSelected) {
                    if (action.isPreferredPaymentOption) {
                        checkoutData?.paymentInfoList?.forEach {
                            it.isSelected = false
                            if (it.method == NETBANKING || it.method == WALLET) {
                                it.preferredMethodsList?.forEach { item ->
                                    item.isSelected = false
                                }
                            }
                        }
                    } else {
                        checkoutData?.preferredPaymentMethods?.forEach {
                            it.isSelected = false
                            if (it.method == NETBANKING || it.method == WALLET) {
                                it.preferredMethodsList?.forEach { item ->
                                    item.isSelected = false
                                }
                            }
                        }
                    }

                    val list = if (action.isPreferredPaymentOption) {
                        checkoutData?.preferredPaymentMethods.orEmpty()
                    } else {
                        checkoutData?.paymentInfoList.orEmpty()
                    }
                    for (pos in list.indices) {
                        list[pos].isSelected = (pos == action.position)
                    }
                    /**
                     * clear all previous ticks
                     **/
                    list[action.position].preferredMethodsList?.forEach {
                        it.isSelected = false
                    }

                    if (list[action.position].method == NETBANKING || list[action.position].method == WALLET
                        || list[action.position].method == CARD
                        || list[action.position].method == UPI_COLLECT
                    ) {
                        isPaymentMethodSelected = false
                    } else if (list[action.position].method == PaymentOptionsAdapter.COD) {
                        isPaymentMethodSelected = true
                        paymentActivityBody = PaymentActivityBody(
                            paymentStartBody = paymentStartBody!!,
                            cardDetails = null,
                            method = list[action.position].method.orEmpty(),
                            type = list[action.position].type.orEmpty(),
                            deeplink = list[action.position].codInfo?.deeplink.orEmpty(),
                            upi = null,
                            upiPackage = null
                        )
                    }

                } else {
                    isPaymentMethodSelected = false
                    /**
                     * reset checked state
                     **/

                    checkoutData?.preferredPaymentMethods?.forEach {
                        it.isSelected = false
                        if (it.method == NETBANKING || it.method == WALLET) {
                            it.preferredMethodsList?.forEach { item ->
                                item.isSelected = false
                            }
                        }
                    }
                    checkoutData?.paymentInfoList?.forEach {
                        it.isSelected = false
                        if (it.method == NETBANKING || it.method == WALLET) {
                            it.preferredMethodsList?.forEach { item ->
                                item.isSelected = false
                            }
                        }
                    }
                }

                /**
                 * all the payment options are selected/deselected
                 * enable wallet only if wallet balance is sufficient and wallet is ticked
                 * */
                if (isWalletSufficient || isPaymentMethodSelected) {
                    mBinding?.buttonPay?.enable()
                    mBinding?.buttonPay?.setBackgroundColor(resources.getColor(R.color.orange_eb532c))
                } else {
                    mBinding?.buttonPay?.disable()
                    mBinding?.buttonPay?.setBackgroundColor(resources.getColor(R.color.grey_cbcbcb))
                }

                paymentOptionsAdapter?.notifyDataSetChanged()
                paymentOptionsAdapterPreferred?.notifyDataSetChanged()
            }
            is CardDetailsFilled -> {
                isPaymentMethodSelected = true
                paymentStartBody?.apply {
                    method = "card"
                    paymentStartInfo?.useWalletCash = isWallet
                    paymentStartInfo?.useWalletReward = isReward
                }
                paymentActivityBody = PaymentActivityBody(
                    paymentStartBody = paymentStartBody!!,
                    cardDetails = action.cardDetails,
                    method = "card",
                    type = "",
                    deeplink = null,
                    upi = null,
                    upiPackage = null
                )

                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.PAYMENT_CHECKOUT_METHOD_CLICKED,
                        hashMapOf(
                            "method" to paymentActivityBody?.method.orEmpty(),
                            "method_type" to paymentActivityBody?.type.orEmpty(),
                            "variant_id" to paymentActivityBody?.paymentStartBody?.paymentStartInfo?.variantId.orEmpty(),
                            "payment_for" to paymentActivityBody?.paymentStartBody?.paymentFor.orEmpty()
                        ), ignoreSnowplow = true
                    )
                )
            }
            is UpiFilled -> {
                isPaymentMethodSelected = true
                paymentStartBody?.apply {
                    method = UPI_COLLECT
                    paymentStartInfo?.useWalletCash = isWallet
                    paymentStartInfo?.useWalletReward = isReward
                }
                paymentActivityBody = PaymentActivityBody(
                    paymentStartBody = paymentStartBody!!,
                    cardDetails = null,
                    method = UPI_COLLECT,
                    type = "",
                    deeplink = null,
                    upi = action.upi,
                    upiPackage = null
                )

                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.PAYMENT_CHECKOUT_COLLECT_UPI_METHOD_CLICKED,
                        hashMapOf(
                            "method" to paymentActivityBody?.method.orEmpty(),
                            "method_type" to paymentActivityBody?.type.orEmpty(),
                            "variant_id" to paymentActivityBody?.paymentStartBody?.paymentStartInfo?.variantId.orEmpty(),
                            "payment_for" to paymentActivityBody?.paymentStartBody?.paymentFor.orEmpty()
                        ), ignoreSnowplow = true
                    )
                )

            }
            is OtherMethodsFilled -> {
                isPaymentMethodSelected = true
                mBinding?.buttonPay?.enable()
                mBinding?.buttonPay?.setBackgroundColor(resources.getColor(R.color.orange_eb532c))
                paymentStartBody?.apply {
                    method = action.method
                    paymentStartInfo?.useWalletCash = isWallet
                    paymentStartInfo?.useWalletReward = isReward
                }
                paymentActivityBody = PaymentActivityBody(
                    paymentStartBody = paymentStartBody!!,
                    cardDetails = null,
                    method = action.method,
                    type = action.type,
                    deeplink = null,
                    upi = null,
                    upiPackage = null
                )

                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.PAYMENT_CHECKOUT_METHOD_CLICKED,
                        hashMapOf(
                            "method" to paymentActivityBody?.method.orEmpty(),
                            "method_type" to paymentActivityBody?.type.orEmpty(),
                            "variant_id" to paymentActivityBody?.paymentStartBody?.paymentStartInfo?.variantId.orEmpty(),
                            "payment_for" to paymentActivityBody?.paymentStartBody?.paymentFor.orEmpty()
                        ), ignoreSnowplow = true
                    )
                )

            }
            is NetBankingSelectedBankSelect -> {
                isPaymentMethodSelected = true
                mBinding?.buttonPay?.enable()
                mBinding?.buttonPay?.setBackgroundColor(resources.getColor(R.color.orange_eb532c))
                paymentStartBody?.apply {
                    method = NETBANKING
                    paymentStartInfo?.useWalletCash = isWallet
                    paymentStartInfo?.useWalletReward = isReward
                }
                paymentActivityBody = PaymentActivityBody(
                    paymentStartBody = paymentStartBody!!,
                    cardDetails = null,
                    method = NETBANKING,
                    type = action.bankCode,
                    deeplink = null,
                    upi = null,
                    upiPackage = null
                )

                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.PAYMENT_CHECKOUT_NETBANKING_SELECT_METHOD_CLICKED,
                        hashMapOf(
                            "method" to paymentActivityBody?.method.orEmpty(),
                            "method_type" to paymentActivityBody?.type.orEmpty(),
                            "variant_id" to paymentActivityBody?.paymentStartBody?.paymentStartInfo?.variantId.orEmpty(),
                            "payment_for" to paymentActivityBody?.paymentStartBody?.paymentFor.orEmpty()
                        ), ignoreSnowplow = true
                    )
                )

            }
            is PayButton -> {
                if (action.enable) {
                    isPaymentMethodSelected = true
                    mBinding?.buttonPay?.enable()
                    mBinding?.buttonPay?.setBackgroundColor(resources.getColor(R.color.orange_eb532c))
                } else {
                    isPaymentMethodSelected = false
                    if (!isWalletSufficient) {
                        mBinding?.buttonPay?.disable()
                        mBinding?.buttonPay?.setBackgroundColor(resources.getColor(R.color.grey_cbcbcb))
                    }
                }
            }
            else -> {

            }
        }
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCheckoutBinding {
        return FragmentCheckoutBinding.inflate(
            layoutInflater, container, false
        )
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): VipPlanViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        viewModel = activityViewModelProvider(viewModelFactory)
        source = activity?.intent?.getStringExtra(VipPlanActivity.INTENT_EXTRA_VIP_SOURCE)
        UXCam.tagScreenName(TAG)
        mBinding?.buttonPay?.disable()
        mBinding?.buttonPay?.setBackgroundColor(resources.getColor(R.color.grey_cbcbcb))

        paymentStartBody = arguments?.getParcelable(PAYMENT_BODY)
        switchAssortmentId = paymentStartBody?.paymentStartInfo?.switchAssortmentId.orEmpty()

        val data = paymentStartBody?.paymentStartInfo
        isWallet = data?.useWalletCash ?: false
        isReward = data?.useWalletReward ?: false
        couponCode = data?.couponCode.orEmpty()

        viewModel.requestCheckoutData(
            data?.variantId,
            data?.couponCode,
            paymentStartBody?.paymentFor,
            data?.amount
        )
        viewModel.getPackagePaymentInfo(
            hashMapOf(
                PAYMENT_FOR to paymentStartBody?.paymentFor.orEmpty(),
                VARIANT_ID to data?.variantId.orEmpty(),
                AMOUNT to data?.amount.orEmpty(),
                COUPON_CODE to couponCode,
                IS_WALLET to isWallet,
                USE_WALLET_REWARD to isReward,
                PAYMENT_FOR to paymentStartBody?.paymentFor.orEmpty(),
                SWITCH_ASSORTMENT to switchAssortmentId,
            )
        )

        setUpObserver()
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.PAYMENT_PAGE_VIEW,
                hashMapOf(
                    EventConstants.VARIANT_ID to data?.variantId.orEmpty(),
                    EventConstants.PAYMENT_COUPON_ENTERED_VALUE to data?.couponCode.orEmpty(),
                    EventConstants.PAYMENT_FOR to paymentStartBody?.paymentFor.orEmpty(),
                    EventConstants.AMOUNT to data?.amount.orEmpty()
                ), ignoreBranch = false
            )
        )

        mBinding?.ivCloseTooltipReward?.setOnClickListener {
            mBinding?.groupToolTipReward?.hide()
        }

        mBinding?.ivCloseTooltipCash?.setOnClickListener {
            mBinding?.groupToolTipCash?.hide()
        }

        mBinding?.btnVoiceInstructions?.setOnClickListener {
            if (mediaPlayer == null) {
                setupMediaPlayer()
            } else {
                handleMediaPlayer()
            }
        }
    }
}

class ApbBannerAdapter(
    val bannerItems: List<ApbBannerItemData>,
    val deeplinkAction: DeeplinkAction,
    val analyticsPublisher: AnalyticsPublisher,
    val eventType: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.apb_banner_item, parent, false)
        return ApbBannerViewHolder(view)
    }

    override fun getItemCount(): Int = bannerItems.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = bannerItems[position]
        (holder as ApbBannerViewHolder).bindData(
            data,
            deeplinkAction,
            analyticsPublisher,
            eventType
        )
    }

    class ApbBannerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindData(
            dataBanner: ApbBannerItemData,
            deeplinkAction: DeeplinkAction,
            analyticsPublisher: AnalyticsPublisher,
            eventType: String
        ) {
            itemView.imageViewAPBBanner.loadImageEtx(dataBanner.bannerImageUrl.orEmpty())

            itemView.setOnClickListener {
                analyticsPublisher.publishEvent(AnalyticsEvent(eventType))
                if (dataBanner.deeplink.isNullOrEmpty()) {
                    itemView.context.startActivity(
                        Intent(
                            itemView.context,
                            ApbCashPaymentActivity::class.java
                        )
                    )
                } else {
                    deeplinkAction.performAction(itemView.context, dataBanner.deeplink)
                }
            }
        }
    }
}

class PaymentOptionsAdapter(
    val paymentInfoList: List<PaymentInformation>,
    val analyticsPublisher: AnalyticsPublisher,
    val deeplinkAction: DeeplinkAction,
    val actionPerformer: ActionPerformer,
    val isPreferredPaymentOption: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {

        const val UPI = "upi"
        const val UPI_COLLECT = "upi_collect"
        const val NETBANKING = "netbanking"
        const val NETBANKING_SELECTED_BANK = "netbanking_selected_bank"
        const val CARD = "card"
        const val WALLET = "wallet"
        const val COD = "COD"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.payment_options_item, parent, false)
        return PaymentOptionsViewHolder(view)
    }

    override fun getItemCount(): Int = paymentInfoList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = paymentInfoList[position]
        (holder as PaymentOptionsViewHolder).bindData(
            data,
            position,
            analyticsPublisher,
            deeplinkAction,
            actionPerformer,
            isPreferredPaymentOption
        )
    }

    class PaymentOptionsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        ActionPerformer {


        var banksListAdapter: ArrayAdapter<String>? = null

        var razorpay = Razorpay(
            itemView.context as Activity,
            itemView.context.getString(R.string.RAZORPAY_API_KEY)
        )

        private var paymentData: PaymentInformation? = null

        fun bindData(
            data: PaymentInformation,
            position: Int,
            analyticsPublisher: AnalyticsPublisher,
            deeplinkAction: DeeplinkAction,
            actionPerformer: ActionPerformer,
            isPreferredPaymentOption: Boolean
        ) {

            paymentData = data
            (itemView.imageViewLogo.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio =
                data.imageRatio ?: "1:1"
            itemView.imageViewLogo.loadImage(data.imageUrl.orEmpty())
            itemView.radioButton.text = data.name.orEmpty()
            itemView.tvSelectFromOtherBanks.text = data.moreBankText.orEmpty()
            itemView.radioButton.isChecked = data.isSelected
            itemView.tvInfo.text = data.info?.title.orEmpty()
            itemView.tvInfo.setOnClickListener {
                if (data.info != null) {
                    if (!data.info?.deeplink.isNullOrBlank()) {
                        deeplinkAction.performAction(itemView.context, data.info?.deeplink)
                    } else if (!data.info?.imageUrls.isNullOrEmpty()) {
                        itemView.context.startActivity(
                            ImagesPagerActivity.getIntent(
                                itemView.context,
                                data.info?.imageUrls!!,
                                source = CheckoutFragment.TAG
                            )
                        )
                    }
                }
            }

            if (data.isSelected) {
                when (data.method) {
                    UPI_COLLECT -> {
                        showItemView()
                        itemView.tvNew.hide()
                        itemView.layout_card.setVisibleState(false)
                        itemView.layout_preferred_methods.setVisibleState(false)
                        itemView.textViewDesc.setVisibleState(!data.description.isNullOrBlank())
                        itemView.textViewDesc.text = data.description.orEmpty()
                        itemView.etUpi.show()
                        itemView.etUpi.hint = data.upiHint ?: "Upi id"
                        itemView.etUpi.addTextChangedListener(object : TextWatcher {
                            override fun beforeTextChanged(
                                s: CharSequence?,
                                start: Int,
                                count: Int,
                                after: Int
                            ) {

                            }

                            override fun onTextChanged(
                                s: CharSequence?,
                                start: Int,
                                before: Int,
                                count: Int
                            ) {
                                val text = s.toString()
                                data.enteredUpi = text.orEmpty()
                                val isValidUpi = if (text.isBlank()) {
                                    false
                                } else {
                                    text.matches(Regex("[a-zA-Z0-9.\\-_]{2,256}@[a-zA-Z]{2,64}"))
                                }
                                if (isValidUpi) {
                                    actionPerformer.performAction(
                                        UpiFilled(
                                            text,
                                            isPreferredPaymentOption
                                        )
                                    )
                                    actionPerformer.performAction(
                                        PayButton(
                                            true,
                                            isPreferredPaymentOption
                                        )
                                    )
                                } else {
                                    actionPerformer.performAction(
                                        PayButton(
                                            false,
                                            isPreferredPaymentOption
                                        )
                                    )
                                }
                            }

                            override fun afterTextChanged(s: Editable?) {

                            }
                        })
                        itemView.etUpi.setText(data.enteredUpi.orEmpty())
                    }
                    UPI -> {
                        showItemView()
                        itemView.etUpi.hide()
                        itemView.tvNew.hide()
                        itemView.layout_card.setVisibleState(false)
                        itemView.layout_preferred_methods.setVisibleState(false)
                        itemView.textViewDesc.setVisibleState(false)
                        actionPerformer.performAction(
                            OtherMethodsFilled(
                                UPI,
                                "",
                                isPreferredPaymentOption
                            )
                        )
                    }

                    NETBANKING -> {
                        showItemView()
                        itemView.etUpi.hide()
                        itemView.tvNew.hide()
                        itemView.layout_card.setVisibleState(false)
                        itemView.layout_preferred_methods.setVisibleState(true)
                        itemView.tvSelectFromOtherBanks.setVisibleState(true)
                        itemView.textViewDesc.setVisibleState(false)
                        itemView.tvSelectFromOtherBanks.text = data.moreBankText

                        itemView.tvSelectFromOtherBanks.setOnClickListener {
                            setUpDialog(data, actionPerformer, isPreferredPaymentOption)
                        }
                        setUpRecyclerView(data, actionPerformer, isPreferredPaymentOption)
                        itemView.rvPreferredMethods.adapter?.notifyDataSetChanged()
                    }

                    CARD -> {
                        showItemView()
                        itemView.etUpi.hide()
                        itemView.tvNew.hide()
                        itemView.layout_card.setVisibleState(true)
                        itemView.layout_preferred_methods.setVisibleState(false)
                        itemView.textViewDesc.setVisibleState(false)

                        //Localization for hints
                        itemView.layout_card.etCardNumber.hint =
                            data.cardLocalizationData?.cardHint.orEmpty()
                        itemView.layout_card.etExpiry.hint =
                            data.cardLocalizationData?.expiryHint.orEmpty()
                        itemView.layout_card.etCVV.hint =
                            data.cardLocalizationData?.cvvHint.orEmpty()
                        itemView.layout_card.etNameOnCard.hint =
                            data.cardLocalizationData?.nameHint.orEmpty()

                        val cardDetails = CardDetails()

                        itemView.etCardNumber.addTextChangedListener(object : TextWatcher {

                            override fun afterTextChanged(editable: Editable) {

                                if (editable.isNotEmpty() && editable.length % 5 == 0) {
                                    val c = editable[editable.length - 1]
                                    if (c == ' ') {
                                        editable.delete(editable.length - 1, editable.length)
                                    }
                                }
                                if (editable.isNotEmpty() && editable.length % 5 == 0) {
                                    val c = editable[editable.length - 1]
                                    if (Character.isDigit(c) && TextUtils.split(
                                            editable.toString(),
                                            " "
                                        ).size <= 4
                                    ) {
                                        editable.insert(editable.length - 1, " ")
                                    }
                                }


                                if (editable.toString()
                                        .isNotEmpty() && editable.toString().length >= 19
                                ) {
                                    var cardNo = ""
                                    editable.toString().forEach {
                                        if (it.isDigit())
                                            cardNo += it
                                    }
                                    val status = razorpay.isValidCardNumber(cardNo)
                                    if (status) {
                                        cardDetails.cardNo = cardNo
                                        if (cardDetails.cardNo.isNotEmpty() && cardDetails.name.isNotEmpty() && cardDetails.CVV.isNotEmpty() && cardDetails.expiry.isNotEmpty()) {
                                            actionPerformer.performAction(
                                                CardDetailsFilled(
                                                    cardDetails,
                                                    isPreferredPaymentOption
                                                )
                                            )
                                            actionPerformer.performAction(
                                                PayButton(
                                                    true,
                                                    isPreferredPaymentOption
                                                )
                                            )
                                        } else {
                                            itemView.etExpiry.requestFocus()
                                        }
                                    } else {
                                        actionPerformer.performAction(
                                            PayButton(
                                                false,
                                                isPreferredPaymentOption
                                            )
                                        )
                                        cardDetails.cardNo = ""
                                        itemView.etCardNumber.error = "Invalid"
                                    }
                                }
                            }

                            override fun beforeTextChanged(
                                s: CharSequence?,
                                start: Int,
                                count: Int,
                                after: Int
                            ) {
                            }

                            override fun onTextChanged(
                                s: CharSequence?,
                                start: Int,
                                before: Int,
                                count: Int
                            ) {
                            }

                        })

                        itemView.etExpiry.addTextChangedListener(object : TextWatcher {
                            override fun beforeTextChanged(
                                charSequence: CharSequence,
                                i: Int,
                                i1: Int,
                                i2: Int
                            ) {
                            }

                            override fun onTextChanged(
                                charSequence: CharSequence,
                                i: Int,
                                i1: Int,
                                i2: Int
                            ) {
                            }

                            override fun afterTextChanged(editable: Editable) {

                                if (editable.isNotEmpty() && editable.length % 3 == 0) {
                                    val c = editable[editable.length - 1]
                                    if (c == '/') {
                                        editable.delete(editable.length - 1, editable.length)
                                    }
                                }
                                if (editable.isNotEmpty() && editable.length % 3 == 0) {
                                    val c = editable[editable.length - 1]
                                    if (Character.isDigit(c) && TextUtils.split(
                                            editable.toString(),
                                            "/"
                                        ).size <= 2
                                    ) {
                                        editable.insert(editable.length - 1, "/")
                                    }
                                }

                                if (editable.length == 5) {
                                    val text = editable.toString()
                                    if (!text.substring(0, 2).isDigitsOnly() || !text.substring(
                                            3,
                                            5
                                        ).isDigitsOnly()
                                    ) {
                                        itemView.etExpiry.error = "Invalid"
                                        return
                                    }
                                    val mon = text.substring(0, 2).toInt()
                                    val year = text.substring(3, 5).toInt()
                                    val c = Calendar.getInstance()
                                    if (mon in 1..12 && year >= c.get(Calendar.YEAR).rem(100)) {
                                        cardDetails.expiry = text
                                        if (cardDetails.cardNo.isNotEmpty() && cardDetails.name.isNotEmpty() && cardDetails.CVV.isNotEmpty() && cardDetails.expiry.isNotEmpty()) {
                                            actionPerformer.performAction(
                                                PayButton(
                                                    true,
                                                    isPreferredPaymentOption
                                                )
                                            )
                                            actionPerformer.performAction(
                                                CardDetailsFilled(
                                                    cardDetails, isPreferredPaymentOption
                                                )
                                            )
                                        } else {
                                            itemView.etCVV.requestFocus()
                                        }
                                    } else {
                                        itemView.etExpiry.error = "Invalid"
                                    }
                                } else {
                                    actionPerformer.performAction(
                                        PayButton(
                                            false,
                                            isPreferredPaymentOption
                                        )
                                    )
                                    cardDetails.expiry = ""
                                }
                            }
                        })

                        itemView.etCVV.addTextChangedListener(object : TextWatcher {
                            override fun afterTextChanged(s: Editable?) {

                                val cardNetwork =
                                    razorpay.getCardNetwork(cardDetails.cardNo).orEmpty()
                                val cvvLength = if (cardNetwork == "amex") 4 else 3

                                if (s.toString().isNotEmpty() && s.toString().length >= cvvLength) {
                                    cardDetails.CVV = s.toString()
                                    if (cardDetails.cardNo.isNotEmpty() && cardDetails.name.isNotEmpty() && cardDetails.CVV.isNotEmpty() && cardDetails.expiry.isNotEmpty()) {
                                        actionPerformer.performAction(
                                            CardDetailsFilled(
                                                cardDetails,
                                                isPreferredPaymentOption
                                            )
                                        )
                                        actionPerformer.performAction(
                                            PayButton(
                                                true,
                                                isPreferredPaymentOption
                                            )
                                        )
                                    } else {
                                        itemView.etNameOnCard.requestFocus()
                                    }
                                } else {
                                    actionPerformer.performAction(
                                        PayButton(
                                            false,
                                            isPreferredPaymentOption
                                        )
                                    )
                                    cardDetails.CVV = ""
                                }
                            }

                            override fun beforeTextChanged(
                                s: CharSequence?,
                                start: Int,
                                count: Int,
                                after: Int
                            ) {
                            }

                            override fun onTextChanged(
                                s: CharSequence?,
                                start: Int,
                                before: Int,
                                count: Int
                            ) {
                            }

                        })

                        itemView.etNameOnCard.addTextChangedListener(object : TextWatcher {
                            override fun afterTextChanged(s: Editable?) {

                                if (s.toString().isNotEmpty()) {
                                    cardDetails.name = s.toString()
                                    if (cardDetails.cardNo.isNotEmpty() && cardDetails.name.isNotEmpty() && cardDetails.CVV.isNotEmpty() && cardDetails.expiry.isNotEmpty()) {
                                        actionPerformer.performAction(
                                            CardDetailsFilled(
                                                cardDetails,
                                                isPreferredPaymentOption
                                            )
                                        )
                                        actionPerformer.performAction(
                                            PayButton(
                                                true,
                                                isPreferredPaymentOption
                                            )
                                        )
                                    }
                                } else {
                                    actionPerformer.performAction(
                                        PayButton(
                                            false,
                                            isPreferredPaymentOption
                                        )
                                    )
                                    cardDetails.name = ""
                                }
                            }

                            override fun beforeTextChanged(
                                s: CharSequence?,
                                start: Int,
                                count: Int,
                                after: Int
                            ) {
                            }

                            override fun onTextChanged(
                                s: CharSequence?,
                                start: Int,
                                before: Int,
                                count: Int
                            ) {
                            }

                        })
                    }

                    WALLET -> {
                        itemView.tvNew.hide()
                        itemView.etUpi.hide()
                        showItemView()
                        itemView.layout_card.setVisibleState(false)
                        itemView.layout_preferred_methods.setVisibleState(true)
                        itemView.tvSelectFromOtherBanks.setVisibleState(false)
                        itemView.textViewDesc.setVisibleState(false)
                        setUpRecyclerView(data, actionPerformer, isPreferredPaymentOption)
                    }

                    COD -> {
                        itemView.etUpi.hide()
                        if (data.codInfo?.show == true) {
                            showItemView()
                            itemView.textViewDesc.setVisibleState(!data.description.isNullOrBlank())
                            itemView.textViewDesc.text = data.description.orEmpty()
                            itemView.layout_card.setVisibleState(false)
                            itemView.layout_preferred_methods.setVisibleState(false)
                            itemView.tvNew.setVisibleState(!data.hyperText.isNullOrBlank())
                            itemView.tvNew.text = data.hyperText.orEmpty()
                        } else {
                            hideItemView()
                            itemView.tvNew.hide()
                            itemView.textViewDesc.setVisibleState(false)
                        }
                    }

                    NETBANKING_SELECTED_BANK -> {
                        itemView.etUpi.hide()
                        showItemView()
                        itemView.tvNew.hide()
                        itemView.layout_card.setVisibleState(false)
                        itemView.layout_preferred_methods.setVisibleState(false)
                        itemView.textViewDesc.setVisibleState(false)
                        actionPerformer.performAction(
                            NetBankingSelectedBankSelect(
                                data.bankCode.orEmpty(), isPreferredPaymentOption
                            )
                        )
                    }

                    else -> {
                        //paytm mobiwik as siblings
                        itemView.etUpi.hide()
                        showItemView()
                        itemView.tvNew.hide()
                        itemView.layout_card.setVisibleState(false)
                        itemView.layout_preferred_methods.setVisibleState(false)
                        itemView.textViewDesc.setVisibleState(false)
                        actionPerformer.performAction(
                            OtherMethodsFilled(
                                WALLET,
                                data.type.orEmpty(), isPreferredPaymentOption
                            )
                        )
                    }
                }
                if (data.info != null) {
                    itemView.tvInfo.show()
                } else {
                    itemView.tvInfo.hide()
                }
            } else {
                itemView.layout_card.setVisibleState(false)
                itemView.layout_preferred_methods.setVisibleState(false)
                when (data.method) {
                    COD -> {
                        itemView.etUpi.hide()
                        if (data.info != null) {
                            itemView.tvInfo.show()
                        } else {
                            itemView.tvInfo.hide()
                        }
                        if (data.codInfo?.show == true) {
                            showItemView()
                            itemView.textViewDesc.setVisibleState(!data.description.isNullOrBlank())
                            itemView.textViewDesc.text = data.description.orEmpty()
                            itemView.tvNew.setVisibleState(!data.hyperText.isNullOrBlank())
                            itemView.tvNew.text = data.hyperText.orEmpty()

                        } else {
                            hideItemView()
                            itemView.tvNew.hide()
                            itemView.textViewDesc.setVisibleState(false)
                        }
                    }
                    else -> {
                        itemView.tvInfo.hide()
                        itemView.etUpi.hide()
                        showItemView()
                        itemView.tvNew.hide()
                        itemView.textViewDesc.setVisibleState(false)
                    }
                }

            }

            itemView.setOnClickListener {
                actionPerformer.performAction(
                    PaymentMethodClicked(
                        position,
                        !data.isSelected,
                        isPreferredPaymentOption
                    )
                )
            }
        }

        private fun hideItemView() {
            itemView.hide()
            itemView.layoutParams = RecyclerView.LayoutParams(
                0,
                0
            )
        }

        private fun showItemView() {
            itemView.show()
            itemView.layoutParams = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        private fun setUpDialog(
            data: PaymentInformation,
            actionPerformer: ActionPerformer,
            isPreferredPaymentOption: Boolean
        ) {

            val bankCodesList = ArrayList<String>()
            val banksList = ArrayList<String>()

            data.moreBanksData?.list?.forEach {
                banksList.add(it.name.orEmpty())
                bankCodesList.add(it.code.orEmpty())
            }

            val view = LayoutInflater.from(itemView.context).inflate(R.layout.list_dialog, null)
            banksListAdapter = ArrayAdapter(
                itemView.context,
                R.layout.support_simple_spinner_dropdown_item,
                banksList
            )
            view.listView.adapter = banksListAdapter

            val dialog = AlertDialog.Builder(itemView.context).create()
            dialog.apply {
                setCancelable(true)
                setView(view)
                show()
            }

            view.btnCross.setOnClickListener {
                dialog.dismiss()
            }

            //todo v2
            /*view.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener, android.widget.SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    if (newText.isNullOrEmpty()) {
                        view.listView.adapter = ArrayAdapter(itemView.context, R.layout.support_simple_spinner_dropdown_item, banksList)
                    } else {
                        val list = mutableListOf<String>()
                        for (item in banksList) {
                            if (item.toLowerCase().contains(newText))
                                list.add(item)
                        }
                        view.listView.adapter = ArrayAdapter(itemView.context, R.layout.support_simple_spinner_dropdown_item, list)
                    }
                    return false
                }

            })*/

            view.tvTitle.text = data.moreBanksData?.title.orEmpty()

            view.listView.setOnItemClickListener { _, _, position, _ ->

                paymentData?.preferredMethodsList?.forEach {
                    it.isSelected = false
                }

                itemView.rvPreferredMethods.adapter?.notifyDataSetChanged()
                actionPerformer.performAction(
                    OtherMethodsFilled(
                        NETBANKING,
                        bankCodesList[position], isPreferredPaymentOption
                    )
                )
                itemView.tvSelectFromOtherBanks.setBackgroundColor(Color.parseColor("#FFF7F4"))
                itemView.tvSelectFromOtherBanks.text = banksList[position]
                dialog.dismiss()
            }
        }

        private fun setUpRecyclerView(
            data: PaymentInformation,
            actionPerformer: ActionPerformer,
            isPreferredPaymentOption: Boolean
        ) {
            itemView.rvPreferredMethods.layoutManager =
                LinearLayoutManager(itemView.context, LinearLayoutManager.VERTICAL, false)
            itemView.rvPreferredMethods.adapter = PreferredMethodsAdapter(
                method = data.method.orEmpty(),
                preferredMethodsList = data.preferredMethodsList.orEmpty(),
                actionPerformer = actionPerformer,
                actionPerformer2 = this,
                isPreferredPaymentOption
            )
        }

        override fun performAction(action: Any) {
            when (action) {
                is RefreshUI -> {
                    //dialog bank clicked -> preferred bank clicked
                    itemView.tvSelectFromOtherBanks.setBackgroundColor(Color.WHITE)
                    itemView.tvSelectFromOtherBanks.text = paymentData?.moreBankText
                }
                else -> {

                }
            }
        }
    }
}

class PreferredMethodsAdapter(
    private var method: String,
    private val preferredMethodsList: List<PreferredMethodsItem>,
    val actionPerformer: ActionPerformer,
    val actionPerformer2: ActionPerformer,
    val isPreferredPaymentOption: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return PreferredMethodsViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.preferred_method_item, parent, false)
        )
    }

    override fun getItemCount(): Int = preferredMethodsList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val data = preferredMethodsList[position]

        holder.itemView.ivPreferredLogo.loadImage(data.imageUrl.orEmpty(), R.drawable.ic_paytm)
        holder.itemView.tvPreferredName.text = data.name
        holder.itemView.ivTick.setVisibleState(data.isSelected)
        if (data.isSelected && !data.isPreSelectionHandled) {
            data.isPreSelectionHandled = true
            for (pos in preferredMethodsList.indices) {
                preferredMethodsList[pos].isSelected = (pos == position)
            }
            actionPerformer.performAction(
                OtherMethodsFilled(
                    method,
                    data.code.orEmpty(),
                    isPreferredPaymentOption
                )
            )
        }
        holder.itemView.setOnClickListener {
            //for wallet and for main banks list
            data.isPreSelectionHandled = true
            val selected = !data.isSelected
            if (selected) {
                for (pos in preferredMethodsList.indices) {
                    preferredMethodsList[pos].isSelected = (pos == position)
                }
                actionPerformer.performAction(
                    OtherMethodsFilled(
                        method,
                        data.code.orEmpty(),
                        isPreferredPaymentOption
                    )
                )
            } else {
                preferredMethodsList[position].isSelected = false
                actionPerformer.performAction(PayButton(false, isPreferredPaymentOption))
            }

            notifyDataSetChanged()
            actionPerformer2.performAction(RefreshUI())
        }
    }

    class PreferredMethodsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}