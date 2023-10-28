package com.doubtnutapp.wallet

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.EventConstants.APB_WALLET_CLICKED
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.VipStateEvent
import com.doubtnutapp.EventBus.WalletAmountUpdateEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.ActionPerformer2
import com.doubtnutapp.base.AddMoneyClicked
import com.doubtnutapp.base.OneTapBuy
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.course.widgets.VpaWidget
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.WalletData
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.domain.payment.entities.*
import com.doubtnutapp.domain.payment.interactor.PaymentLinkUseCase
import com.doubtnutapp.paymentv2.ui.PaymentActivity
import com.doubtnutapp.sharing.WhatsAppSharing
import com.doubtnutapp.topicboostergame2.ui.FaqBottomSheetDialogFragment
import com.doubtnutapp.transactionhistory.TransactionHistoryActivityV2
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.EditTextLimitUtil
import com.doubtnutapp.utils.EventObserver
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.vipplan.PaymentHelpViewItem
import com.doubtnutapp.vipplan.ui.ApbBannerAdapter
import com.doubtnutapp.vipplan.ui.CheckoutFragment
import com.doubtnutapp.wallet.adapter.WalletAmountAdapter
import com.doubtnutapp.wallet.viewmodel.WalletViewModel
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.doubtnutapp.widgetmanager.widgets.FaqWidget
import com.google.gson.annotations.SerializedName
import dagger.android.support.DaggerFragment
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_wallet.*
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 20/11/20.
 */
class WalletFragment : DaggerFragment(), ActionPerformer2 {
    companion object {
        private const val TAG = "WalletFragment"
        private const val MAX_AMOUNT = 10000
        const val PAYMENT_REQUEST_CODE = 101
        fun newInstance() = WalletFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: WalletViewModel

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var whatsAppSharing: WhatsAppSharing

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    private var observer: Disposable? = null

    private var isWalletUpdated = false

    var paymentHelpViewItem = mutableListOf<PaymentHelpViewItem>()

    var paymentHelpTitle = ""

    var actionPerformer: ActionPerformer? = null

    var updateEventLiveData: MutableLiveData<PaymentActivityBody> = MutableLiveData()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_wallet, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = viewModelProvider(viewModelFactory)
        setUpObserver()
        viewModel.fetchWalletData()
        viewModel.getBestSellerData()
        getRecommendedCourses()
        etAmount.filters = arrayOf<InputFilter>(EditTextLimitUtil(1, 10000))
        setClickListeners()
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.WALLET_PAGE_OPEN, ignoreSnowplow = true))
    }

    fun setActionListener(actionPerformer: ActionPerformer) {
        this.actionPerformer = actionPerformer
    }

    override fun performAction(action: Any) {
        when (action) {
            is OneTapBuy -> {
                startActivityForResult(
                    PaymentActivity.getPaymentIntent(
                        requireContext(), PaymentActivityBody(
                            paymentStartBody = PaymentStartBody(
                                paymentFor = CheckoutFragment.COURSE_PACKAGE,
                                method = CheckoutFragment.WALLET,
                                paymentStartInfo = PaymentStartInfo(
                                    amount = null,
                                    couponCode = null,
                                    variantId = action.variantId,
                                    useWalletCash = true,
                                    selectedWallet = null,
                                    useWalletReward = true,
                                    switchAssortmentId = null
                                )
                            ),
                            cardDetails = null,
                            method = "",
                            type = "",
                            deeplink = null,
                            upi = null,
                            upiPackage = null
                        )
                    ), PAYMENT_REQUEST_CODE
                )
                analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.WALLET_RECOMMENDED_COURSE_BUY_NOW_CLICK, ignoreSnowplow = true))
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setClickListeners() {

        buttonBack.setOnClickListener {
            activity?.onBackPressed()
        }

        tvAddFifty.setOnClickListener {
            var amount = currentEnteredAmount() + 50
            if (amount > MAX_AMOUNT) {
                amount = MAX_AMOUNT
            }
            updateEditTextAmount(amount.toString())
        }

        tvAddHundred.setOnClickListener {
            var amount = currentEnteredAmount() + 100
            if (amount > MAX_AMOUNT) {
                amount = MAX_AMOUNT
            }
            updateEditTextAmount(amount.toString())
        }

        tvAddFiveHundred.setOnClickListener {
            var amount = currentEnteredAmount() + 500
            if (amount > MAX_AMOUNT) {
                amount = MAX_AMOUNT
            }
            updateEditTextAmount(amount.toString())
        }

        buttonPay.setOnClickListener {
            val amount = currentEnteredAmount()
            if (amount <= 0) {
                showSnackBarMessage("Enter Valid Amount")
                return@setOnClickListener
            }

            actionPerformer?.performAction(AddMoneyClicked(amount))
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.BUY_NOW_CLICKED,
                    hashMapOf(EventConstants.AMOUNT to amount),
                    ignoreSnowplow = true
                )
            )
        }

        buttonViewPaymentHistory.setOnClickListener {
            startActivity(TransactionHistoryActivityV2.getStartIntent(requireContext()))
        }

        etAmount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty() || s.toString().toIntOrNull() == 0) {
                    buttonPay.disable()
                    buttonPay.setBackgroundColor(resources.getColor(R.color.grey_cbcbcb))
                } else {
                    buttonPay.enable()
                    buttonPay.setBackgroundColor(resources.getColor(R.color.orange_eb532c))
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })
    }

    private fun currentEnteredAmount() = etAmount.text.toString().toIntOrNull() ?: 0

    private fun updateEditTextAmount(amount: String) {
        etAmount?.setText(amount)
        etAmount?.setSelection(etAmount?.text?.length ?: 0)
    }

    private fun setUpObserver() {

        viewModel.walletData.observeK(
            viewLifecycleOwner,
            this::onWalletData,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgress
        )

        viewModel.messageStringIdLiveData.observe(viewLifecycleOwner, EventObserver {
            showSnackBarMessage(it)
        })

        viewModel.bestSellerLiveData.observeK(
            viewLifecycleOwner,
            this::onBestSellerDataFetched,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgress
        )

        viewModel.recommendedCoursesLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is Outcome.Success -> {
                    val courses = it.data.courses
                    if (courses != null) {
                        setupRecommendedCoursesUi(courses)
                    } else {
                        tvCourseCarouselsTitle.hide()
                        rvCourseCarousels.hide()
                    }
                }
                is Outcome.Progress -> {
                    pbCourseCarousels.isVisible = it.loading
                }
            }
        }

        observer = DoubtnutApp.INSTANCE.bus()?.toObservable()?.subscribe { event ->
            if (event is VipStateEvent && event.state) {
                onPaymentSuccess()
            }
        }
    }

    fun onPaymentSuccess() {
        isWalletUpdated = true
        showSnackBarMessage("Payment Done")
        viewModel.fetchWalletData()
        getRecommendedCourses()
        etAmount.setText("")
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.WALLET_PAYMENT_SUCCESS, ignoreSnowplow = true))
    }


    private fun onBestSellerDataFetched(data: BestSellerData) {

        layoutBestSeller.show()

        tvBestSeller.text = data.header
        tvTitleBestSeller.text = data.title
        tvPriceBestSeller.text = data.amountToPay
        buttonBuyNow.text = data.buyText
        ivBestSeller.loadImageEtx(data.iconUrl.orEmpty())

        buttonBuyNow.setOnClickListener {
            deeplinkAction.performAction(requireContext(), data.buyDeeplink)
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.BUY_NOW_CLICKED,
                    hashMapOf(EventConstants.ASSORTMENT_ID to data.assortmentId.toString()),
                    ignoreSnowplow = true
                )
            )
        }

        ivBestSeller.setOnClickListener {
            deeplinkAction.performAction(requireContext(), data.deeplink)
        }
    }

    private fun onWalletData(walletData: WalletData) {
        tvTotalWalletBalance.text = walletData.walletInfo?.totalAmount?.name.orEmpty()
        tvTotalWalletBalanceValue.text = walletData.walletInfo?.totalAmount?.value.orEmpty()

        if (walletData.vpaObj != null) {
            viewVpa.show()
            val model = VpaWidget.Model().apply {
                _data = walletData.vpaObj
                layoutConfig = WidgetLayoutConfig(0, 0, 0, 0)
            }
            viewVpa.bindWidget(viewVpa.widgetViewHolder, model)
        } else {
            viewVpa.hide()
        }

        rvWalletAmounts.adapter = WalletAmountAdapter().apply {
            updateList(walletData.walletInfo?.walletAmounts)
        }

        updatePaymentLinkInfoView(walletData.paymentLinkInfo)

        paymentHelpTitle = walletData.faq?.title.orEmpty()
        if (paymentHelpTitle.isBlank().not()) {
            textViewPaymentHelp.text = paymentHelpTitle
            textViewPaymentHelp.setOnClickListener {
                analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.WALLET_HELP_CLICK, ignoreSnowplow = true))
                FaqBottomSheetDialogFragment.newInstance(
                    walletData.faq?.title.orEmpty(),
                    (walletData.faq?.items ?: arrayListOf()) as ArrayList<FaqWidget.FaqItem>
                ).show(childFragmentManager, FaqBottomSheetDialogFragment.TAG)
            }
        }

        if (walletData.walletUse == null) {
            layoutUseWallet.hide()
        } else {
            layoutUseWallet.show()
            tvWhyWalletTitle.text = walletData.walletUse.title.orEmpty()
            tvWalletOne.text = walletData.walletUse.list?.getOrNull(0)?.name.orEmpty()
            tvWalletTwo.text = walletData.walletUse.list?.getOrNull(1)?.name.orEmpty()
            tvWalletThree.text = walletData.walletUse.list?.getOrNull(2)?.name.orEmpty()
            imageViewOne.loadImageEtx(walletData.walletUse.list?.getOrNull(0)?.image.orEmpty())
            imageViewTwo.loadImageEtx(walletData.walletUse.list?.getOrNull(1)?.image.orEmpty())
            imageViewThree.loadImageEtx(walletData.walletUse.list?.getOrNull(2)?.image.orEmpty())
        }

        if (walletData.APBBannerList.isNullOrEmpty()) {
            apbBannerRecyclerView.visibility = View.GONE
        } else {
            setUpRecyclerView(walletData.APBBannerList.orEmpty())
            apbBannerRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun updatePaymentLinkInfoView(paymentLinkData: PaymentLinkInfo?) {
        if (paymentLinkData?.payLink != null) {
            textViewPaymentLinkInfoTitle.text = paymentLinkData.payLink?.title
            if (!paymentLinkData.payLink?.description.isNullOrEmpty()) {
                textViewPaymentLinkDescription.text = paymentLinkData.payLink?.description!![0]
                if (paymentLinkData.payLink?.description!!.size > 1) {
                    textViewPaymentLinkKnowMore.show()
                } else {
                    textViewPaymentLinkKnowMore.hide()
                }
            } else {
                textViewPaymentLinkDescription.hide()
                textViewPaymentLinkKnowMore.hide()
            }

            layoutPaymentLink.show()
            textViewTitlePaymentLink.show()
            textViewTitlePaymentLink.text = paymentLinkData.payLink?.header.orEmpty()

            textViewPaymentLinkKnowMore.setOnClickListener {
                if (!paymentLinkData.payLink?.description.isNullOrEmpty()) {
                    if (textViewPaymentLinkKnowMore.text == Constants.SHOW_LESS) {
                        textViewPaymentLinkDescription.text =
                            paymentLinkData.payLink?.description!![0]
                        textViewPaymentLinkKnowMore.text = Constants.KNOW_MORE
                        ivDropDown.setImageResource(R.drawable.ic_arrow_down_filled)
                    } else {
                        var description = ""
                        var lineNum = 1
                        for (line in paymentLinkData.payLink?.description!!) {
                            description += if (lineNum < paymentLinkData.payLink?.description!!.size) {
                                line + "\n\n"
                            } else {
                                line
                            }
                            lineNum++
                        }
                        textViewPaymentLinkDescription.text = description
                        textViewPaymentLinkKnowMore.text = Constants.SHOW_LESS
                        ivDropDown.setImageResource(R.drawable.ic_up_filled)
                        scrollView.post {
                            scrollView.fullScroll(View.FOCUS_DOWN)
                        }
                    }
                } else {
                    it.hide()
                }
            }

            layoutSharePaymentLink.setOnClickListener {
                val amount = currentEnteredAmount()
                if (amount <= 0) {
                    showSnackBarMessage("Enter Valid Amount")
                    return@setOnClickListener
                }
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(EventConstants.SHARE_PAYMENT_LINK_CLICK + EventConstants.UNDERSCORE + TAG, ignoreSnowplow = true)
                )
                viewModel.requestPaymentLink(
                    PaymentLinkUseCase.Param(
                        amount.toString(),
                        "", "", "WALLET", "",
                        "", "", "", null,
                        null, null
                    )
                )
                viewModel.paymentLinkLiveData.observe(viewLifecycleOwner, EventObserver {
                    if (!it.errorMessage.isNullOrEmpty()) {
                        toast(it.errorMessage!!, Toast.LENGTH_SHORT)
                    }
                    if (it.shareMessage.isNullOrEmpty()) {
                        toast(
                            "Error in creating payment link, Please try later",
                            Toast.LENGTH_SHORT
                        )
                    } else {
                        sharePaymentLink(it.shareMessage)
                    }
                })
            }
        } else {
            layoutPaymentLink.hide()
            textViewTitlePaymentLink.hide()
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
            AnalyticsEvent(EventConstants.PAYMENT_LINK_SHARED + EventConstants.UNDERSCORE + TAG, ignoreSnowplow = true)
        )
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(parentFragmentManager, "BadRequestDialog")
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun ioExceptionHandler() {
        val currentContext = context ?: return
        if (NetworkUtils.isConnected(currentContext)) {
            toast(getString(R.string.somethingWentWrong))
        } else {
            toast(getString(R.string.string_noInternetConnection))
        }
    }

    private fun updateProgress(state: Boolean) {
        progressBar.setVisibleState(state)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isWalletUpdated) {
            DoubtnutApp.INSTANCE.bus()?.send(WalletAmountUpdateEvent(true))
        }
        observer?.dispose()
    }

    private fun setUpRecyclerView(bannerItems: List<ApbBannerItemData>) {
        apbBannerRecyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        apbBannerRecyclerView.adapter =
            ApbBannerAdapter(
                bannerItems,
                deeplinkAction,
                analyticsPublisher,
                APB_WALLET_CLICKED
            )
    }

    private fun setupRecommendedCoursesUi(carousels: List<WidgetEntityModel<WidgetData, WidgetAction>>) {
        tvCourseCarouselsTitle.show()
        val adapter = WidgetLayoutAdapter(requireContext(), this)
        rvCourseCarousels.adapter = adapter
        adapter.setWidgets(carousels)
    }

    private fun getRecommendedCourses() {
        viewModel.getRecommendedCourses()
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.WALLET_RECOMMENDED_COURSE_VISIBLE, ignoreSnowplow = true))
    }
}

data class BestSellerData(
    @SerializedName("header") val header: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("buy_deeplink") val buyDeeplink: String?,
    @SerializedName("image_bg") val imageBg: String?,
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("icon_url") val iconUrl: String?,
    @SerializedName("amount_to_pay") val amountToPay: String?,
    @SerializedName("buy_text") val buyText: String?,
    @SerializedName("id") val assortmentId: Int?
)