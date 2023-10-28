package com.doubtnutapp.payment.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import com.apxor.androidsdk.core.Attributes
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.activityViewModelProvider
import com.doubtnut.core.utils.toast
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.ui.BaseFragment
import com.doubtnutapp.databinding.FragmentPaymentFailureBinding
import com.doubtnutapp.domain.payment.entities.PaymentLinkCreate
import com.doubtnutapp.domain.payment.entities.PaymentStartBody
import com.doubtnutapp.domain.payment.entities.PaymentStartInfo
import com.doubtnutapp.payment.model.PaymentLinkData
import com.doubtnutapp.sales.PrePurchaseCallingCard2
import com.doubtnutapp.sales.PrePurchaseCallingCardData2
import com.doubtnutapp.sales.PrePurchaseCallingCardModel2
import com.doubtnutapp.sharing.WhatsAppSharing
import com.doubtnutapp.transactionhistory.TransactionHistoryActivityV2
import com.doubtnutapp.utils.ApxorUtils
import com.doubtnutapp.utils.EventObserver
import com.doubtnutapp.vipplan.ui.CheckoutFragment
import com.doubtnutapp.vipplan.viewmodel.VipPlanViewModel
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-12-14.
 */
class PaymentFailureFragment : BaseFragment() {

    companion object {
        private const val TAG = "PaymentFailureFragment"
        fun newInstance(): PaymentFailureFragment {
            return PaymentFailureFragment()
        }
    }

    var orderId = ""
    var paymentLinkData: PaymentLinkData? = null

    private lateinit var viewModel: VipPlanViewModel

    @Inject
    lateinit var whatsAppSharing: WhatsAppSharing

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private lateinit var binding: FragmentPaymentFailureBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPaymentFailureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = activityViewModelProvider(viewModelFactory)
        activity?.let { currentActivity ->
            if (currentActivity.intent.hasExtra("transactionId")) {
                orderId = currentActivity.intent.getStringExtra("transactionId").orEmpty()
                if (orderId.isBlank().not()) {
                    binding.textViewTransactionId.text = "Order Id : $orderId"
                }
            }
        }

        activity?.let { currentActivity ->
            if (currentActivity.intent.hasExtra("amount")) {
                val currentAmount = currentActivity.intent.getStringExtra("amount")
                var currencySymbol =
                    currentActivity.intent.getStringExtra(PaymentStatusActivity.CURRENCY_SYMBOL)
                        .orEmpty()
                if (currencySymbol.isBlank()) {
                    currencySymbol = "₹"
                }
                if (currentAmount.isNullOrBlank().not()) {
                    binding.textViewAmount.text = currencySymbol + currentAmount
                }
            }
        }

        activity?.let { currentActivity ->
            if (currentActivity.intent.hasExtra(PaymentStatusActivity.REASON)) {
                val reason = currentActivity.intent.getStringExtra(PaymentStatusActivity.REASON)
                if (!reason.isNullOrBlank()) {
                    binding.textViewInfo.text = reason
                }
            }
        }

        binding.imageViewClose.setOnClickListener {
            activity?.onBackPressed()
        }

        activity?.let { currentActivity ->
            if (currentActivity.intent.hasExtra("hideButton")) {
                if (currentActivity.intent.getBooleanExtra("hideButton", false)) {
                    binding.buttonRetry.hide()
                }
            }
        }

        binding.buttonRetry.setOnClickListener {
            activity?.onBackPressed()
        }

        binding.layoutMail.setOnClickListener {
            try {
                if (isPackageExisted("com.google.android.gm")) {
                    val intent = Intent(android.content.Intent.ACTION_SEND)
                    intent.type = "text/plain"
                    val pm = activity?.packageManager
                    val matches = pm?.queryIntentActivities(intent, 0)
                    var best: ResolveInfo? = null
                    for (info in matches.orEmpty())
                        if (info.activityInfo.packageName.endsWith(".gm") || info.activityInfo.name.toLowerCase()
                                .contains("gmail")
                        )
                            best = info
                    if (best != null)
                        intent.setClassName(best.activityInfo.packageName, best.activityInfo.name)
                            .putExtra(Intent.EXTRA_SUBJECT, "Payment for Order ID: $orderId")
                            .data = Uri.parse("payments@doubtnut.com")
                    startActivity(intent)
                } else {
                    showToastMessage("Gmail is not installed on your device")
                }
            } catch (e: Exception) {
                showToastMessage("Gmail is not installed on your device")
            }
        }

        binding.textViewPaymentHistory.setOnClickListener {
            context?.let { currentContext ->
                ApxorUtils.logAppEvent(
                    EventConstants.EVENT_NAME_PAYMENT_HISTORY_BUTTON_CLICK,
                    Attributes().apply {
                        putAttribute(EventConstants.SOURCE, EventConstants.EVENT_NAME_FAILURE_PAGE)
                    })
                startActivity(TransactionHistoryActivityV2.getStartIntent(currentContext))
            }
        }

        if (activity?.intent?.hasExtra(PaymentStatusActivity.PAYMENT_LINK_DATA) == true) {
            paymentLinkData =
                activity?.intent?.getParcelableExtra(PaymentStatusActivity.PAYMENT_LINK_DATA)
        }

        if (paymentLinkData != null) {
            val info = paymentLinkData!!
            binding.textViewPaymentLinkInfoTitle.text = info.title
            if (!info.description.isNullOrEmpty()) {
                binding.textViewPaymentLinkDescription.text = info.description[0]
                if (info.description.size > 1) {
                    binding.textViewPaymentLinkKnowMore.show()
                } else {
                    binding.textViewPaymentLinkKnowMore.hide()
                }
            } else {
                binding.textViewPaymentLinkDescription.hide()
                binding.textViewPaymentLinkKnowMore.hide()
            }

            binding.layoutPaymentLink.show()

            binding.textViewPaymentLinkKnowMore.setOnClickListener {
                if (!info.description.isNullOrEmpty()) {
                    if (binding.textViewPaymentLinkKnowMore.text == Constants.SHOW_LESS) {
                        binding.textViewPaymentLinkDescription.text = info.description[0]
                        binding.textViewPaymentLinkKnowMore.text = Constants.KNOW_MORE
                        binding.ivDropDown.setImageResource(R.drawable.ic_arrow_down_filled)
                    } else {
                        var description = ""
                        var lineNum = 1
                        for (line in info.description) {
                            description += if (lineNum < info.description.size) {
                                line + "\n\n"
                            } else {
                                line
                            }
                            lineNum++
                        }
                        binding.textViewPaymentLinkDescription.text = description
                        binding.textViewPaymentLinkKnowMore.text = Constants.SHOW_LESS
                        binding.ivDropDown.setImageResource(R.drawable.ic_up_filled)
                        binding.scrollView.post {
                            binding.scrollView.fullScroll(View.FOCUS_DOWN)
                        }
                    }
                } else {
                    it.hide()
                }
            }

            binding.layoutSharePaymentLink.setOnClickListener {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.SHARE_PAYMENT_LINK_CLICK + EventConstants.UNDERSCORE + TAG,
                        ignoreSnowplow = true
                    )
                )

                viewModel.requestPaymentLink(
                    PaymentStartBody(
                        paymentFor = info.paymentFor,
                        method = CheckoutFragment.PAYMENT_LINK,
                        paymentStartInfo = PaymentStartInfo(
                            amount = info.amount,
                            couponCode = info.coupon,
                            variantId = info.variantId,
                            useWalletCash = info.useWallet,
                            selectedWallet = null,
                            useWalletReward = false,
                            switchAssortmentId = null
                        )
                    )
                )

                viewModel.paymentLinkLiveData.observe(viewLifecycleOwner, EventObserver {
                    val paymentLinkData: PaymentLinkCreate? = it
                    if (!paymentLinkData?.errorMessage.isNullOrEmpty()) {
                        toast(paymentLinkData?.errorMessage!!, Toast.LENGTH_SHORT)
                    }
                    if (paymentLinkData?.shareMessage.isNullOrEmpty()) {
                        toast(
                            "Error in creating payment link, Please try later",
                            Toast.LENGTH_SHORT
                        )
                    } else {
                        sharePaymentLink(paymentLinkData?.shareMessage)
                    }
                })
            }
        } else {
            binding.layoutPaymentLink.hide()
        }
        showPrePurchaseCallingCard()
    }

    private fun showPrePurchaseCallingCard() {
        if (defaultPrefs().getString(Constants.TITLE_PAYMENT_FAILURE, "").isNullOrEmpty()) {
            return
        }
        binding.prePurchaseCallingCard.show()
        binding.prePurchaseCallingCard.bindWidget(
            holder = binding.prePurchaseCallingCard.widgetViewHolder,
            model = PrePurchaseCallingCardModel2().apply {
                _data = PrePurchaseCallingCardData2(
                    title = defaultPrefs().getString(
                        Constants.TITLE_PAYMENT_FAILURE,
                        ""
                    ),
                    titleTextSize = PrePurchaseCallingCard2.titleTextSize(),
                    titleTextColor = PrePurchaseCallingCard2.titleTextColor(),
                    subtitle = defaultPrefs().getString(
                        Constants.SUBTITLE_PAYMENT_FAILURE,
                        ""
                    ),
                    subtitleTextSize = PrePurchaseCallingCard2.subtitleTextSize(),
                    subtitleTextColor = PrePurchaseCallingCard2.subtitleTextColor(),
                    actionText = PrePurchaseCallingCard2.action(),
                    actionTextSize = PrePurchaseCallingCard2.actionTextSize(),
                    actionTextColor = PrePurchaseCallingCard2.actionTextColor(),
                    actionImageUrl = PrePurchaseCallingCard2.actionImageUrl(),
                    actionDeepLink = defaultPrefs().getString(
                        Constants.CALLBACK_DEEPLINK,
                        ""
                    ),
                    imageUrl = PrePurchaseCallingCard2.imageUrl(),
                    source = Constants.SOURCE_PAYMENT_FAILURE
                )
            }
        )
    }

    private fun isPackageExisted(targetPackage: String): Boolean {
        val pm = activity?.packageManager
        try {
            val info = pm?.getPackageInfo(targetPackage, PackageManager.GET_META_DATA)
        } catch (e: PackageManager.NameNotFoundException) {
            return false
        }
        return true
    }

    private fun showToast(@StringRes resId: Int) {
        showToastMessage(getString(resId))
    }

    private fun showToastMessage(message: String) {
        toast(message)
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
                EventConstants.PAYMENT_LINK_SHARED + EventConstants.UNDERSCORE + TAG,
                ignoreSnowplow = true
            )
        )
    }

}