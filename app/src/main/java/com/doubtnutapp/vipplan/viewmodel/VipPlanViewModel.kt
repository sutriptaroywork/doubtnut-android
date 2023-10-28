package com.doubtnutapp.vipplan.viewmodel

import android.nfc.FormatException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.Log
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.domain.homefeed.interactor.ConfigData
import com.doubtnutapp.domain.payment.entities.*
import com.doubtnutapp.domain.payment.interactor.GetCheckoutDataUseCase
import com.doubtnutapp.domain.payment.interactor.GetPlanDetailUseCase
import com.doubtnutapp.domain.payment.interactor.TrialVipUseCase
import com.doubtnutapp.packageInstallerCheck.CheckForPackageInstall
import com.doubtnutapp.payment.event.PaymentEventManager
import com.doubtnutapp.plus
import com.doubtnutapp.utils.Event
import com.google.gson.JsonSyntaxException
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-12-13.
 */

class VipPlanViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val getPlanDetailUseCase: GetPlanDetailUseCase,
    private val paymentEventManager: PaymentEventManager,
    private val trialVipUseCase: TrialVipUseCase,
    private val checkoutDataUseCase: GetCheckoutDataUseCase,
    private val analyticsPublisher: AnalyticsPublisher,
    private val checkForPackageInstall: CheckForPackageInstall
) : BaseViewModel(compositeDisposable) {

    private val _messageStringIdLiveData: MutableLiveData<Event<Int>> = MutableLiveData()

    val messageStringIdLiveData: LiveData<Event<Int>>
        get() = _messageStringIdLiveData

    private val _messageStringLiveData: MutableLiveData<Event<String>> = MutableLiveData()

    val messageStringLiveData: LiveData<Event<String>>
        get() = _messageStringLiveData

    private val _enableVipLiveData: MutableLiveData<Event<String>> = MutableLiveData()

    val enableVipLiveData: LiveData<Event<String>>
        get() = _enableVipLiveData

    private val _checkoutLiveData:
            MutableLiveData<Event<CheckoutData>> = MutableLiveData()

    val checkoutLiveData: LiveData<Event<CheckoutData>>
        get() = _checkoutLiveData

    private val _paymentLinkInfoLiveData:
            MutableLiveData<Event<PaymentLinkInfo?>> = MutableLiveData()

    private val _paymentLinkLiveData:
            MutableLiveData<Event<PaymentLinkCreate?>> = MutableLiveData()

    private val _planDetail: MutableLiveData<Event<PlanDetail>> = MutableLiveData()

    val planDetail: LiveData<Event<PlanDetail>>
        get() = _planDetail

    val paymentLinkInoLiveData: LiveData<Event<PaymentLinkInfo?>>
        get() = _paymentLinkInfoLiveData

    val paymentLinkLiveData: LiveData<Event<PaymentLinkCreate?>>
        get() = _paymentLinkLiveData

    private val _isLoading: MutableLiveData<Boolean> = MutableLiveData()

    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _configLiveData: MutableLiveData<Outcome<ConfigData>> = MutableLiveData()

    val configLiveData: LiveData<Outcome<ConfigData>>
        get() = _configLiveData

    var assortmentType = ""

    fun startLoading() {
        _isLoading.postValue(true)
    }

    fun stopLoading() {
        _isLoading.postValue(false)
    }

    fun getConfigData(sessionCount: Int, postPurchaseSessionCount: Int) {
        _configLiveData.value = Outcome.loading(true)
        compositeDisposable +
                DataHandler.INSTANCE.appConfigRepository.getConfigData(
                    sessionCount,
                    postPurchaseSessionCount
                )
                    .applyIoToMainSchedulerOnSingle()
                    .map {
                        it.data
                    }
                    .subscribeToSingle({
                        _configLiveData.value = Outcome.success(it)
                        _configLiveData.value = Outcome.loading(false)
                    }, {
                        _configLiveData.value = Outcome.loading(false)
                    })
    }

    fun fetchPlanDetail(
        assortmentId: String,
        bottomSheet: Boolean = false,
        source: String? = null
    ) {
        startLoading()
        compositeDisposable + getPlanDetailUseCase
            .execute(GetPlanDetailUseCase.Param(assortmentId, bottomSheet, source))
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle(this::onPlanDetailSuccess, this::onError)
    }

    fun requestVipTrial(id: String) {
        startLoading()
        compositeDisposable + trialVipUseCase
            .execute(id)
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle({
                stopLoading()
                if (it.status == "SUCCESS") {
                    _enableVipLiveData.value = Event(it.message.orEmpty())
                }
                _messageStringLiveData.value = Event(it.status.orEmpty())
            }, this::onError)
    }

    fun requestCheckoutData(
        variantId: String?,
        coupon: String?,
        paymentFor: String? = null,
        amount: String? = null
    ) {
        val requestData =
            GetCheckoutDataUseCase.Param(
                variantId,
                coupon,
                paymentFor.orEmpty(),
                amount.orEmpty(),
                hasAnyUpiApp()
            )
        startLoading()
        compositeDisposable + checkoutDataUseCase
            .execute(requestData)
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle({
                stopLoading()
                assortmentType = it.assortmentType.orEmpty()
                _paymentLinkInfoLiveData.value = Event(it.paymentLink)
                _checkoutLiveData.value = Event(it)
            }, this::onError)
    }

    private fun hasAnyUpiApp(): Boolean {
        val upiAppsPackages = listOf(
            "net.one97.paytm",
            "com.phonepe.app",
            "com.google.android.apps.nbu.paisa.user",
            "in.org.npci.upiapp",
            "in.amazon.mShop.android.shopping"
        )
        upiAppsPackages.forEach {
            if (checkForPackageInstall.appInstalled(it)) {
                return true
            }
        }
        return false
    }

    private fun onPlanDetailSuccess(planDetail: PlanDetail) {
        stopLoading()
        _planDetail.postValue(Event(planDetail))
    }

    private fun onError(error: Throwable) {
        stopLoading()
        _messageStringIdLiveData.value = Event(R.string.something_went_wrong)
        logException(error)
    }

    private fun logException(error: Throwable) {
        try {
            if (error is JsonSyntaxException
                || error is NullPointerException
                || error is ClassCastException
                || error is FormatException
                || error is IllegalArgumentException
            ) {
                Log.e(error)
            }
        } catch (e: Exception) {

        }
    }

    fun publishEventWith(eventName: String, variantId: String, ignoreSnowplow: Boolean = false) {
        paymentEventManager.eventWith(eventName, variantId, ignoreSnowplow = ignoreSnowplow)
    }

    fun publishEventWith(eventName: String, variantId: String, paymentSource: String) {
        paymentEventManager.eventWith(eventName, variantId, paymentSource)
    }

    fun publishEventWithSnowPlow(structuredEvent: StructuredEvent) {
        analyticsPublisher.publishEvent(structuredEvent)
    }

    fun requestPaymentLink(paymentStartBody: PaymentStartBody?) {

        startLoading()
        compositeDisposable + checkoutDataUseCase
            .getPaymentInitiationInfo(paymentStartBody)
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle(
                {
                    stopLoading()
                    _paymentLinkLiveData.postValue(Event(it.paymentLinkCreate))
                },
                this::onError
            )
    }


    val packagePaymentInfo: LiveData<PackagePaymentInfo>
        get() = _packagePaymentInfo

    val _packagePaymentInfo: MutableLiveData<PackagePaymentInfo> = MutableLiveData()

    fun getPackagePaymentInfo(params: HashMap<String, Any>) {
        startLoading()
        compositeDisposable + checkoutDataUseCase.getPackagePaymentInfo(params)
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle({
                stopLoading()
                _packagePaymentInfo.postValue(it)
            }, this::onError)
    }

    val couponLiveData: LiveData<CouponData>
        get() = _couponLiveData

    val _couponLiveData: MutableLiveData<CouponData> = MutableLiveData()

    fun getCouponData(hashMap: HashMap<String, Any>) {
        startLoading()
        compositeDisposable + checkoutDataUseCase.getCouponData(hashMap)
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle({
                stopLoading()
                _couponLiveData.value = it
            }, this::onError)
    }

}