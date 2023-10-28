package com.doubtnutapp.paymentplan.ui

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.ThreadUtils
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.*
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.databinding.ActivityPaymentPlanBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.domain.homefeed.interactor.ConfigData
import com.doubtnutapp.domain.payment.entities.CardDetails
import com.doubtnutapp.domain.payment.entities.PaymentActivityBody
import com.doubtnutapp.domain.payment.entities.PaymentStartBody
import com.doubtnutapp.domain.payment.entities.PaymentStartInfo
import com.doubtnutapp.moengage.MoEngageUtils
import com.doubtnutapp.payment.ui.PaymentStatusActivity
import com.doubtnutapp.paymentplan.data.PaymentData
import com.doubtnutapp.paymentplan.viewmodel.PaymentPlanViewModel
import com.doubtnutapp.paymentplan.widgets.*
import com.doubtnutapp.paymentv2.ui.CouponBottomSheetDialogFragment
import com.doubtnutapp.paymentv2.ui.CouponSuccessDialogFragment
import com.doubtnutapp.paymentv2.ui.PaymentActivity
import com.doubtnutapp.qrpayment.QrPaymentActivity
import com.doubtnutapp.sharing.WhatsAppSharing
import com.doubtnutapp.studygroup.viewmodel.SgCreateViewModel
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.*
import com.doubtnutapp.utils.extension.observeEvent
import com.doubtnutapp.vipplan.ui.CheckoutFragment
import com.doubtnutapp.vipplan.ui.PaymentHelpActivity
import com.doubtnutapp.vipplan.ui.VipPlanActivity
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import io.branch.indexing.BranchUniversalObject
import io.branch.referral.Defines
import io.branch.referral.util.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class PaymentPlanActivity : BaseBindingActivity<PaymentPlanViewModel, ActivityPaymentPlanBinding>(),
    ActionPerformer {

    companion object {
        const val TAG = "PaymentPlanActivity"
        const val PAYMENT_FOR_COURSE = "course_package"
        const val PAYMENT_FOR = "payment_for"
        const val PAYMENT_REQUEST_CODE = 101
        const val VARIANT_ID = "variant_id"
        const val COUPON_CODE = "coupon_code"
        const val AMOUNT = "amount"
        const val PAYMENT_FOR_ID = "payment_for_id"
        const val SWITCH_ASSORTMENT_ID = "switch_assortment_id"

        fun getStartIntent(
            context: Context,
            source: String,
            variantId: String,
            couponCode: String,
            amount: String? = null,
            paymentFor: String = PAYMENT_FOR_COURSE,
            paymentForId: String? = null,
            switchAssortmentId: String = "",
        ) = Intent(context, PaymentPlanActivity::class.java)
            .apply {
                putExtra(Constants.SOURCE, source)
                putExtra(VARIANT_ID, variantId)
                putExtra(COUPON_CODE, couponCode)
                putExtra(AMOUNT, amount)
                putExtra(PAYMENT_FOR, paymentFor)
                putExtra(PAYMENT_FOR_ID, paymentForId)
                putExtra(SWITCH_ASSORTMENT_ID, switchAssortmentId)
            }
    }

    private lateinit var adapter: WidgetLayoutAdapter
    private var selectedWallet: HashSet<String>? = null
    private val variantId: String
        get() = intent?.getStringExtra(VARIANT_ID)!!
    private val paymentFor: String
        get() = intent?.getStringExtra(PAYMENT_FOR)!!
    private val paymentForId: String?
        get() = intent?.getStringExtra(PAYMENT_FOR_ID)
    private var couponCode: String? = null
    private var removeCoupon: Boolean = false
    private val amount: String?
        get() = intent?.getStringExtra(AMOUNT)
    private val switchAssortmentId: String
        get() = intent?.getStringExtra(SWITCH_ASSORTMENT_ID).orEmpty()
    private var isAudioPlayedOnce = false
    private var mediaPlayer: MediaPlayer? = null
    private var voiceUrl: String? = null
    private var paymentSource: String? = ""
    private var paymentMethod = "RazorPay"
    private var paymentDone: Boolean = false
    private var eventInfo: PaymentData.EventInfo? = null
    private val studentId: String by lazy {
        UserUtil.getStudentId()
    }

    private var currencySymbol: String = ""

    private val couponAppliedDialog by lazy { CouponSuccessDialogFragment.newInstance() }

    @Inject
    lateinit var disposable: CompositeDisposable

    @Inject
    lateinit var whatsAppSharing: WhatsAppSharing

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private lateinit var createStudyGroupViewModel: SgCreateViewModel

    private var observer: Disposable? = null

    override fun providePageName(): String = TAG

    override fun provideViewBinding(): ActivityPaymentPlanBinding =
        ActivityPaymentPlanBinding.inflate(layoutInflater)

    override fun provideViewModel(): PaymentPlanViewModel {
        createStudyGroupViewModel = viewModelProvider(viewModelFactory)
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        couponCode = intent?.getStringExtra(COUPON_CODE)
        binding.ivBack.setOnClickListener {
            goBack()
        }
        setupRecyclerView()
        analyticsPublisher.publishEvent(AnalyticsEvent(
            EventConstants.CHECKOUT_PAGE_OPEN,
            hashMapOf<String, Any>()
                .apply {
                    put(
                        EventConstants.VARIANT_ID,
                        variantId
                    )
                }
        ))
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.paymentData.observeK(
            this,
            ::onSuccess,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgress
        )

        viewModel.paymentLinkLiveData.observe(this, EventObserver {
            val paymentLinkData = it
            if (paymentLinkData != null) {
                if (!paymentLinkData.errorMessage.isNullOrEmpty()) {
                    toast(paymentLinkData.errorMessage!!)
                }
                if (paymentLinkData.shareMessage.isNullOrEmpty()) {
                    toast("Error in creating payment link, Please try later")
                } else {
                    sharePaymentLink(paymentLinkData.shareMessage)
                }
            }
        })

        viewModel.configLiveData.observeK(this,
            ::onConfigDataSuccess, ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            {})

        observer = DoubtnutApp.INSTANCE.bus()?.toObservable()?.subscribe { event ->
            if (event is WalletAmountUpdateEvent) {
                initiateRecyclerListenerAndFetchInitialData()
            } else if (event is CouponApplied) {
                showCouponAppliedSuccessDialog()
                couponCode = event.couponCode
                removeCoupon = false
                initiateRecyclerListenerAndFetchInitialData()
            } else if (event is PaymentCompletedEvent && event.type is PaymentSuccessEventType) {
                if (event.type == QrPaymentSuccess) {
                    paymentDone = true
                    DoubtnutApp.INSTANCE.bus()?.send(VipStateEvent(true))
                    finish()
                }
            }
        }

        createStudyGroupViewModel.groupCreatedLiveData.observeEvent(this) {
            val deeplink = it.groupChatDeeplink ?: return@observeEvent
            deeplinkAction.performAction(this, deeplink)
            val eventMap = hashMapOf<String, Any>()
            eventMap[EventConstants.SOURCE] = EventConstants.PAYMENT
            eventMap[EventConstants.FEATURE] = EventConstants.FEATURE_STUDY_GROUP
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.CHAT_STARTED,
                    eventMap
                )
            )
        }
    }

    private fun showCouponAppliedSuccessDialog() {
        couponAppliedDialog.show(supportFragmentManager, CouponSuccessDialogFragment.TAG)
    }

    private fun onConfigDataSuccess(data: ConfigData) {
        ConfigUtils.saveToPref(data)
        finish()
    }

    override fun performAction(action: Any) {
        disposable.clear()
        when (action) {
            is OnWalletToggle -> {
                if (selectedWallet.isNullOrEmpty()) {
                    selectedWallet = HashSet()
                }
                if (action.isSelected) {
                    selectedWallet?.add(action.key)
                } else {
                    selectedWallet?.remove(action.key)
                }
                initiateRecyclerListenerAndFetchInitialData()
            }
            is ShowCheckoutV2Dialog -> {
                showDialog(action.data)
            }
            is OnNetBankingClick -> {
                updateSelectionView(action.parentPosition, action.childPosition)
                if (!action.ignoreAction) {
                    val delay = action.delay
                    if (delay != 0L) {
                        action.delay = 0
                        handleTimer(delay = delay, action = action)
                    } else {
                        sendPaymentMethodClick("netbanking", "")
                        showNetBankingBottomSheet(action.data)
                    }
                }
            }
            is OnSelectedNetBankingClick -> {
                updateSelectionView(action.parentPosition, action.childPosition)
                if (!action.ignoreAction) {
                    val delay = action.delay
                    if (delay != 0L) {
                        action.delay = 0
                        handleTimer(delay = delay, action = action)
                    } else {
                        sendPaymentMethodClick(action.method, action.code)
                        openPaymentScreen(action.method, action.code, null, null)
                    }
                }
            }
            is OnDnWalletPaymentClick -> {
                openPaymentScreen(action.method, "", null, null)
            }
            is OnWalletClick -> {
                updateSelectionView(action.parentPosition, action.childPosition)
                if (!action.ignoreAction) {
                    val delay = action.delay
                    if (delay != 0L) {
                        action.delay = 0
                        handleTimer(delay = delay, action = action)
                    } else {
                        sendPaymentMethodClick("wallet", "")
                        showWalletBottomSheet(action.data)
                    }
                }
            }
            is OnCardMethodClick -> {
                updateSelectionView(action.parentPosition, action.childPosition)
                if (!action.ignoreAction) {
                    val delay = action.delay
                    if (delay != 0L) {
                        action.delay = 0
                        handleTimer(delay = delay, action = action)
                    } else {
                        sendPaymentMethodClick("card", "")
                        showCardBottomSheet(action.data)
                    }
                }
            }
            is OnUpiCollectClick -> {
                updateSelectionView(action.parentPosition, action.childPosition)
                if (!action.ignoreAction) {
                    val delay = action.delay
                    if (delay != 0L) {
                        action.delay = 0
                        handleTimer(delay = delay, action = action)
                    } else {
                        sendPaymentMethodClick("upi_collect", "")
                        showUpiBottomSheet(action.data)
                    }
                }
            }
            is OnUpiPaymentClick -> {
                updateSelectionView(action.parentPosition, action.childPosition)
                if (!action.ignoreAction) {
                    val delay = action.delay
                    if (delay != 0L) {
                        action.delay = 0
                        handleTimer(delay = delay, action = action)
                    } else {
                        sendPaymentMethodClick(action.method, "")
                        openPaymentScreen(action.method, "", null, null)
                    }
                }
            }
            is OnPaytmWalletPaymentClick -> {
                updateSelectionView(action.parentPosition, action.childPosition)
                if (!action.ignoreAction) {
                    val delay = action.delay
                    if (delay != 0L) {
                        action.delay = 0
                        handleTimer(delay = delay, action = action)
                    } else {
                        sendPaymentMethodClick(action.method, action.type)
                        openPaymentScreen(action.method, action.type, null, null)
                    }
                }
            }
            is OnWalletPaymentClick -> {
                openPaymentScreen(action.method, action.code, null, null)
            }
            is OnNetBankingPaymentClick -> {
                openPaymentScreen(action.method, action.code, null, null)
            }
            is OnUpiCollectPaymentClick -> {
                openPaymentScreen(action.method, "", null, action.upiId)
            }
            is OnDeeplinkPaymentClick -> {
                updateSelectionView(action.parentPosition, action.childPosition)
                if (!action.ignoreAction) {
                    val delay = action.delay
                    if (delay != 0L) {
                        action.delay = 0
                        handleTimer(delay = delay, action = action)
                    } else {
                        sendPaymentMethodClick("deeplink", "")
                        deeplinkAction.performAction(this, action.deeplink)
                    }
                }
            }
            is OnPaymentLinkShareClick -> {
                updateSelectionView(action.parentPosition, action.childPosition)
                if (!action.ignoreAction) {
                    val delay = action.delay
                    if (delay != 0L) {
                        action.delay = 0
                        handleTimer(delay = delay, action = action)
                    } else {
                        sendPaymentMethodClick(action.method, "")
                        onPaymentLinkShare(action.method)
                    }
                }
            }
            is OnQrPaymentClick -> {
                updateSelectionView(action.parentPosition, action.childPosition)
                if (!action.ignoreAction) {
                    val delay = action.delay
                    if (delay != 0L) {
                        action.delay = 0
                        handleTimer(delay = delay, action = action)
                    } else {
                        sendPaymentMethodClick(action.method, "")
                        QrPaymentActivity.getStartIntent(this, getPaymentStartData(action.method))
                            .apply {
                                startActivity(this)
                            }
                    }
                }
            }
            is OnBankSelectorClicked -> {
                showBankSelectorBottomSheet(action.data)
            }
            is OnRemoveCouponClick -> {
                couponCode = ""
                removeCoupon = true
                initiateRecyclerListenerAndFetchInitialData()
            }
            is OnAddCouponClick -> {
                val hashMap = hashMapOf<String, Any>(
                    CheckoutFragment.PAYMENT_FOR to paymentFor,
                    CheckoutFragment.VARIANT_ID to variantId,
                    CheckoutFragment.AMOUNT to amount.orEmpty(),
                    CheckoutFragment.IS_WALLET to false,
                    CheckoutFragment.USE_WALLET_REWARD to false,
                    CheckoutFragment.SWITCH_ASSORTMENT to switchAssortmentId
                )
                CouponBottomSheetDialogFragment.newInstance(hashMap)
                    .show(supportFragmentManager, CouponBottomSheetDialogFragment.TAG)
            }
            is OnCardPaymentClick -> {
                openPaymentScreen("card", "", action.cardDetails, null)
            }
            is OnAudioToggle -> {
                if (action.state) {
                    playAudio(action.audioUrl)
                } else {
                    releaseMediaPlayer()
                }
            }
            else -> {

            }
        }
    }

    private fun sendPaymentMethodClick(method: String, methodType: String) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.PAYMENT_CHECKOUT_METHOD_CLICKED,
                hashMapOf(
                    "method" to method,
                    "method_type" to methodType,
                    "variant_id" to variantId,
                    "payment_for" to paymentFor
                ), ignoreSnowplow = true
            )
        )
    }

    private fun sendPaymentPayButtonClick() {
        val event = AnalyticsEvent(
            EventConstants.PAYMENT_PAY_NOW,
            hashMapOf<String, Any>()
                .apply {
                    put(
                        EventConstants.VARIANT_ID,
                        variantId
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

    override fun onAttachFragment(fragment: Fragment) {
        super.onAttachFragment(fragment)
        when (fragment) {
            is CardBottomSheet -> fragment.setActionListener(this)
            is WalletSelectorBottomSheet -> fragment.setActionListener(this)
            is NetBankingBottomSheet -> fragment.setActionListener(this)
            is UPIBottomSheet -> fragment.setActionListener(this)
        }
    }

    private fun handleMediaPlayer() {
        when (mediaPlayer?.isPlaying) {
            true -> {
                mediaPlayer?.pause()
            }
            false -> {
                mediaPlayer?.start()
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
        (applicationContext as DoubtnutApp).bus()?.send(VipStateEvent(paymentDone))
        disposable.dispose()
        observer?.dispose()
    }

    private fun updateSelectionView(parentPosition: Int, childPosition: Int) {
        adapter.widgets.forEachIndexed { index, widgetEntityModel ->
            if (widgetEntityModel is CheckoutV2ParentWidgetModel) {
                widgetEntityModel.data.items.forEachIndexed { childIndex, childWidgetEntityModel ->
                    if (childWidgetEntityModel is CheckoutV2ChildWidgetModel) {
                        childWidgetEntityModel.data.isSelected =
                            index == parentPosition && childIndex == childPosition
                    }
                }
            }
        }
        adapter.notifyDataSetChanged()
    }

    private fun handleTimer(delay: Long, action: Any) {
        disposable.clear()
        disposable.add(
            Observable.timer(delay, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<Long>() {
                    override fun onComplete() {
                        performAction(action)
                        disposable.clear()
                    }

                    override fun onNext(t: Long) {}

                    override fun onError(e: Throwable) {
                        disposable.clear()
                    }
                })
        )
    }

    private fun setupRecyclerView() {
        adapter = WidgetLayoutAdapter(this, this)
        binding.rvWidgets.layoutManager = LinearLayoutManager(this)
        binding.rvWidgets.adapter = adapter
        initiateRecyclerListenerAndFetchInitialData()
    }

    private fun initiateRecyclerListenerAndFetchInitialData() {
        releaseMediaPlayer()
        adapter.clearData()
        viewModel.getPaymentData(
            amount = amount,
            variantId = variantId,
            coupon = couponCode,
            paymentFor = paymentFor,
            selectedWallet = selectedWallet?.toList(),
            removeCoupon = removeCoupon,
            switchAssortmentId = switchAssortmentId
        )
    }

    private fun onSuccess(data: PaymentData) {
        currencySymbol = data.currencySymbol.orEmpty()
        couponCode = data.couponInfo?.couponCode
        removeCoupon = data.couponInfo?.removeCoupon ?: false
        //setting data in coupon applied dialog
        if (couponAppliedDialog.isVisible) {
            couponAppliedDialog.setData(
                data.couponInfo?.dialogTitle.orEmpty(),
                data.couponInfo?.dialogSubTitle.orEmpty()
            )
        }

        eventInfo = data.eventInfo
        binding.pageTitle.text = data.pageTitle.orEmpty()
        if (!data.widgets.isNullOrEmpty()) {
            selectedWallet = HashSet()
            data.widgets.find { it is CheckoutV2WalletWidgetModel }?.apply {
                val model = (this as CheckoutV2WalletWidgetModel)
                model.data.items?.forEach {
                    if (it.isSelected) {
                        selectedWallet?.add(it.key)
                    }
                }
            }

            data.widgets.find { it is CheckoutV2ParentWidgetModel }?.apply {
                val model = (this as CheckoutV2ParentWidgetModel)
                model.data.items.forEach {
                    if (it is CheckoutV2ChildWidgetModel && it.data.isSelected) {
                        it.data.progress = it.data.autoApplyTimer?.toLongOrNull() ?: 0
                    }
                }
            }

            if (!isAudioPlayedOnce) {
                data.widgets.find { it is CheckoutV2HeaderWidgetModel }?.apply {
                    val headerData = (this as CheckoutV2HeaderWidgetModel).data
                    if (!headerData.audioUrl.isNullOrBlank()) {
                        headerData.isAudioPlaying = true
                        playAudio(headerData.audioUrl)
                    }
                }
            }
            adapter.setWidgets(data.widgets)
        }

        if (data.paymentHelp != null) {
            binding.tvHelp.show()
            binding.tvHelp.text = data.paymentHelp.pageTitle.orEmpty()
            binding.tvHelp.setOnClickListener {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.CC_HELP_CLICK,
                        ignoreSnowplow = true
                    )
                )
                startActivity(
                    PaymentHelpActivity.getStartIntent(
                        this,
                        data.paymentHelp
                    )
                )
            }
            if (!data.paymentHelp.pageTitleTooltip.isNullOrBlank()) {
                binding.group.show()
                binding.titleToolTip.text = data.paymentHelp.pageTitleTooltip.orEmpty()
            } else {
                binding.group.hide()
            }
            binding.ivClose.setOnClickListener {
                binding.group.hide()
            }
        } else {
            binding.tvHelp.hide()
            binding.group.hide()
        }

    }

    private fun playAudio(audioUrl: String?) {
        voiceUrl = audioUrl
        isAudioPlayedOnce = true
        if (mediaPlayer == null) {
            setupMediaPlayer()
        } else {
            handleMediaPlayer()
        }
    }

    private fun showDialog(checkoutV2HeaderDialogData: CheckoutV2HeaderDialogData) {
        val dialog = CheckoutV2Dialog.newInstance(checkoutV2HeaderDialogData)
        dialog.show(supportFragmentManager, CheckoutV2Dialog.TAG)
    }

    private fun showNetBankingBottomSheet(data: CheckoutV2DialogData?) {
        NetBankingBottomSheet.newInstance(data, object : NetBankingBottomSheet.BankSelector {
            override fun onBankSelectorClicked(data: NetBankingDialogData?) {
                performAction(OnBankSelectorClicked(data))
            }
        })
            .show(supportFragmentManager, NetBankingBottomSheet.TAG)
    }

    private fun showBankSelectorBottomSheet(data: NetBankingDialogData?) {
        BankSelectorBottomSheet.newInstance(data)
            .show(supportFragmentManager, BankSelectorBottomSheet.TAG)
    }

    private fun showWalletBottomSheet(data: CheckoutV2DialogData?) {
        WalletSelectorBottomSheet.newInstance(data)
            .show(supportFragmentManager, WalletSelectorBottomSheet.TAG)
    }

    private fun showCardBottomSheet(data: CheckoutV2DialogData?) {
        CardBottomSheet.newInstance(data)
            .show(supportFragmentManager, CardBottomSheet.TAG)
    }

    private fun showUpiBottomSheet(data: CheckoutV2DialogData?) {
        UPIBottomSheet.newInstance(data)
            .show(supportFragmentManager, UPIBottomSheet.TAG)
    }

    private fun openPaymentScreen(
        method: String,
        type: String,
        cardDetails: CardDetails?,
        upiId: String?
    ) {
        sendPaymentPayButtonClick()
        startActivityForResult(
            PaymentActivity.getPaymentIntent(
                context = this,
                paymentActivityData = getPaymentBody(method, type, cardDetails, upiId)
            ),
            PAYMENT_REQUEST_CODE
        )
    }

    private fun getPaymentBody(
        method: String,
        type: String,
        cardDetails: CardDetails?,
        upiId: String?
    ) =
        PaymentActivityBody(
            paymentStartBody = getPaymentStartData(method),
            cardDetails = cardDetails,
            method = method,
            type = type,
            deeplink = null,
            upi = upiId,
            upiPackage = null
        )

    private fun getPaymentStartData(method: String) = PaymentStartBody(
        paymentFor = paymentFor,
        method = method,
        paymentStartInfo = PaymentStartInfo(
            amount = amount,
            couponCode = if (removeCoupon) {
                ""
            } else {
                couponCode
            },
            variantId = variantId,
            paymentForId = paymentForId,
            useWalletCash = null,
            selectedWallet = selectedWallet?.toList(),
            useWalletReward = null,
            switchAssortmentId = switchAssortmentId
        )
    )

    private fun onPaymentLinkShare(method: String) {
        viewModel.requestPaymentLink(getPaymentStartData(method))
    }

    private fun sharePaymentLink(shareMessage: String?) {
        whatsAppSharing.shareOnWhatsApp(
            context = this,
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

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(supportFragmentManager, "BadRequestDialog")
    }

    private fun ioExceptionHandler() {
        if (NetworkUtils.isConnected(this).not()) {
            toast(getString(R.string.string_noInternetConnection))
        } else {
            toast(getString(R.string.somethingWentWrong))
        }
    }

    private fun updateProgress(state: Boolean) {
        binding.progressBar.setVisibleState(state)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PAYMENT_REQUEST_CODE && resultCode == PaymentActivity.PAYMENT_SUCCESS_RETURN_CODE) {
            getConfigData()
            paymentDone = true
            (applicationContext as DoubtnutApp).bus()?.send(VipStateEvent(true))

            val udid = Utils.getUDID()

            ThreadUtils.runOnAnalyticsThread {
                val purchaseEvent = BranchUniversalObject()
                    .setContentMetadata(
                        ContentMetadata()
                            .setPrice(
                                eventInfo?.amount?.toDoubleOrNull() ?: 0.0, CurrencyType.INR
                            )
                            .setProductName(eventInfo?.packageDescription.orEmpty())
                            .setProductVariant(variantId)
                            .setSku(eventInfo?.packageId)
                            .setQuantity(1.0)
                            .setProductBrand("etoos")
                            .setProductCategory(ProductCategory.SOFTWARE)
                    )

                BranchEvent(BRANCH_STANDARD_EVENT.PURCHASE)
                    .setCoupon("")
                    .setAffiliation("")
                    .setTax(0.0)
                    .setShipping(0.0)
                    .setRevenue(eventInfo?.amount?.toDoubleOrNull() ?: 0.0)
                    .setTransactionID(studentId)
                    .setCurrency(CurrencyType.INR)
                    .addContentItems(purchaseEvent)
                    .addCustomDataProperty("OS", "Android")
                    .addCustomDataProperty(
                        "OS_version",
                        android.os.Build.VERSION.SDK_INT.toString()
                    )
                    .addCustomDataProperty("android_id", udid)
                    .logEvent(baseContext)
            }

            MoEngageUtils.setUserAttribute(
                applicationContext,
                "paid_course_id",
                eventInfo?.courseId.orEmpty()
            )
            MoEngageUtils.setUserAttribute(
                applicationContext,
                "paid_course_variant_id",
                variantId
            )

            sendPurchaseCategoryBranchEvent()
            sendPaymentResultMoEngageEvent(true, eventInfo?.courseId.orEmpty(), variantId)

            data?.let { transactionData ->
                if (!transactionData.getStringExtra(Constants.SOURCE).isNullOrEmpty()) {
                    paymentSource = transactionData.getStringExtra(Constants.SOURCE)
                }
            }


            analyticsPublisher.publishBranchIoEvent(AnalyticsEvent(EventConstants.VIP_PAYMENT_SUCCESS))
            analyticsPublisher.publishMoEngageEvent(AnalyticsEvent(EventConstants.VIP_PAYMENT_SUCCESS))
            // finish()
            if (paymentSource.isNullOrEmpty()) {
                viewModel.publishEventWith(
                    EventConstants.VIP_PAYMENT_SUCCESS,
                    variantId,
                    ignoreSnowplow = true
                )
            } else {
                viewModel.publishEventWith(
                    EventConstants.VIP_PAYMENT_SUCCESS,
                    variantId,
                    paymentSource.toString(),
                    ignoreSnowplow = true
                )
            }

        } else {
            sendPaymentResultMoEngageEvent(false, eventInfo?.courseId.orEmpty(), variantId)
            viewModel.publishEventWith(
                EventConstants.VIP_PAYMENT_FAILURE,
                variantId,
                ignoreSnowplow = true
            )
            analyticsPublisher.publishBranchIoEvent(
                AnalyticsEvent(
                    EventConstants.VIP_PAYMENT_FAILURE,
                    ignoreSnowplow = true
                )
            )
            data?.let { transactionData ->
                if (transactionData.hasExtra(PaymentActivity.ORDER_ID)) {

                    val responseOrderId =
                        transactionData.getStringExtra(PaymentActivity.ORDER_ID).orEmpty()
                    val responseAmount =
                        transactionData.getStringExtra(PaymentActivity.AMOUNT).orEmpty()

                    startActivity(
                        PaymentStatusActivity.getStartIntent(
                            this,
                            false,
                            responseOrderId,
                            responseAmount,
                            false,
                            "vip",
                            "",
                            null,
                            transactionData.getStringExtra(PaymentActivity.REASON).orEmpty(),
                            currencySymbol
                        )
                    )
                } else {
                    showToast(R.string.somethingWentWrong)
                }
            } ?: run {
                showToast(R.string.somethingWentWrong)
            }
        }
    }

    private fun showToast(@StringRes resId: Int) {
        showToastMessage(getString(resId))
    }

    private fun showToastMessage(message: String) {
        toast(message)
    }

    private fun sendPaymentResultMoEngageEvent(
        isSuccess: Boolean,
        courseId: String,
        variantId: String
    ) {
        analyticsPublisher.publishMoEngageEvent(
            AnalyticsEvent(
                EventConstants.EVENT_PAYMENT,
                hashMapOf<String, Any>().apply {

                    put(
                        EventConstants.PAYMENT_STATUS, if (isSuccess) EventConstants.PAYMENT_SUCCESS
                        else
                            EventConstants.PAYMENT_FAILED
                    )
                    put(EventConstants.COURSE_ID, courseId)
                    put(EventConstants.VARIANT_ID, variantId)
                    put(
                        EventConstants.PAYMENT_METHOD, when (paymentMethod) {
                            "Paytm" -> EventConstants.PAYMENT_METHOD_IS_PAYTM
                            "RazorPay" -> EventConstants.PAYMENT_METHOD_IS_RAZERPAY
                            else -> ""
                        }
                    )
                })
        )
    }

    private fun sendPurchaseCategoryBranchEvent() {
        val revenue = eventInfo?.amount?.toDoubleOrNull() ?: 0.0
        val userInfo: HashMap<String, Any> = hashMapOf(
            "Payment Method" to paymentMethod,
            Defines.Jsonkey.Revenue.key to revenue,
            "Duration" to eventInfo?.packageDescription.orEmpty(),
            "Page Path" to intent.getStringExtra(VipPlanActivity.INTENT_EXTRA_SOURCE).orEmpty(),
            "transaction id" to studentId
        )

        analyticsPublisher.publishBranchIoEvent(
            AnalyticsEvent(
                EventConstants.PURCHASE_CATEGORY_TYPE + eventInfo?.assortmentType.orEmpty(),
                userInfo
            )
        )

        when (eventInfo?.packageDescription.orEmpty()) {
            "30 Days" -> {
                analyticsPublisher.publishBranchIoEvent(
                    AnalyticsEvent(
                        EventConstants.PURCHASE_P1,
                        userInfo
                    )
                )
            }

            "1 Year" -> {
                analyticsPublisher.publishBranchIoEvent(
                    AnalyticsEvent(
                        EventConstants.PURCHASE_P3,
                        userInfo
                    )
                )
            }

            else -> {
            }
        }
    }

    private fun getConfigData() {
        val postPurchaseSessionCount =
            defaultPrefs().getInt(Constants.POST_PURCHASE_SESSION_COUNT, 0)
        val sessionCount = defaultPrefs().getInt(Constants.SESSION_COUNT, 1)
        viewModel.getConfigData(sessionCount, postPurchaseSessionCount)
    }

    override fun onBackPressed() {
        if (ChatUtil.isChatEnabled(this)) {
            showAlertDialogForUserConfirmation(
                this,
                eventInfo?.assortmentId.orEmpty(),
                eventInfo?.packageDescription.orEmpty(),
                eventInfo?.duration.orEmpty()
            )
        } else {
            super.onBackPressed()
        }
    }

    private fun showAlertDialogForUserConfirmation(
        context: Context,
        assortmentId: String,
        title: String,
        duration: String
    ) {
        var alertDialog: AlertDialog? = null
        alertDialog = this.let {
            val builder = AlertDialog.Builder(context)
            builder.apply {
                setPositiveButton(context.getString(R.string.freshchat_yes)) { _, _ ->
                    if (FeaturesManager.isFeatureEnabled(
                            this@PaymentPlanActivity,
                            Features.STUDY_GROUP_AS_FRESH_CHAT
                        )
                    ) {
                        createStudyGroupViewModel.createGroup(
                            groupName = null,
                            groupImage = null,
                            isSupport = true
                        )
                    } else {
                        ChatUtil.setUser(
                            applicationContext,
                            assortmentId,
                            variantId,
                            title,
                            duration,
                            EventConstants.PAYMENT
                        )
                        ChatUtil.startConversation(applicationContext)
                        alertDialog?.setOnDismissListener {
                            finish()
                        }
                        defaultPrefs().edit()
                            .putLong(Constants.PREF_CHAT_START_TIME, System.currentTimeMillis())
                            .apply()
                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                EventConstants.CHAT_STARTED,
                                hashMapOf<String, Any>(
                                    EventConstants.SOURCE to EventConstants.PAYMENT,
                                    EventConstants.FEATURE to EventConstants.FEATURE_FRESHWORKS
                                ), ignoreSnowplow = true
                            )
                        )
                    }
                }.setNegativeButton(context.getString(R.string.freshchat_no)) { _, _ ->
                    finish()
                }
            }.setTitle(context.getString(R.string.chat_with_us))
                .setMessage(R.string.chat_message)

            builder.create()
        }
        alertDialog.show()
    }

}