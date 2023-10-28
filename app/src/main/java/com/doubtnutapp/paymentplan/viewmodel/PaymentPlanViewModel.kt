package com.doubtnutapp.paymentplan.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.domain.homefeed.interactor.ConfigData
import com.doubtnutapp.domain.payment.entities.PaymentLinkCreate
import com.doubtnutapp.domain.payment.entities.PaymentStartBody
import com.doubtnutapp.domain.payment.interactor.GetCheckoutDataUseCase
import com.doubtnutapp.packageInstallerCheck.CheckForPackageInstall
import com.doubtnutapp.payment.event.PaymentEventManager
import com.doubtnutapp.paymentplan.data.PaymentData
import com.doubtnutapp.paymentplan.repository.CheckoutV2Repository
import com.doubtnutapp.plus
import com.doubtnutapp.utils.Event
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject

/**
 * Created by Mehul Bisht on 07-10-2021
 */

class PaymentPlanViewModel @Inject constructor(
    private val checkoutV2Repository: CheckoutV2Repository,
    compositeDisposable: CompositeDisposable,
    private val checkForPackageInstall: CheckForPackageInstall,
    private val checkoutDataUseCase: GetCheckoutDataUseCase,
    private val paymentEventManager: PaymentEventManager
) : BaseViewModel(compositeDisposable) {

    private val _paymentData = MutableLiveData<Outcome<PaymentData>>()
    val paymentData: LiveData<Outcome<PaymentData>> get() = _paymentData

    fun getPaymentData(
        amount: String?,
        variantId: String?,
        coupon: String?,
        paymentFor: String?,
        selectedWallet: List<String>?,
        removeCoupon: Boolean,
        switchAssortmentId: String?
    ) {
        startLoading()
        viewModelScope.launch {
            checkoutV2Repository.getCheckoutData(
                amount = amount,
                variantId = variantId,
                coupon = coupon,
                paymentFor = paymentFor,
                hasUpiApp = hasAnyUpiApp(),
                selectedWallet = selectedWallet,
                removeCoupon = removeCoupon,
                switchAssortmentId = switchAssortmentId
            ).map {
                it.data
            }.catch {
                onError(it)
                stopLoading()
            }.collect {
                _paymentData.value = Outcome.success(it)
                stopLoading()
            }
        }
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

    private fun onError(error: Throwable) {
        _paymentData.value = if (error is HttpException) {
            when (error.response()?.code()) {
                HttpURLConnection.HTTP_UNAUTHORIZED -> Outcome.BadRequest(error.message ?: "")
                HttpURLConnection.HTTP_BAD_REQUEST -> Outcome.ApiError(error)
                //TODO socket time out exception msg should be "Timeout while fetching the data"
                else -> Outcome.Failure(error)
            }
        } else {
            Outcome.Failure(error)
        }
    }

    private fun startLoading() {
        _paymentData.value = Outcome.loading(true)
    }

    private fun stopLoading() {
        _paymentData.value = Outcome.loading(false)
    }

    private val _paymentLinkLiveData:
            MutableLiveData<Event<PaymentLinkCreate?>> = MutableLiveData()

    val paymentLinkLiveData: LiveData<Event<PaymentLinkCreate?>>
        get() = _paymentLinkLiveData

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

    fun publishEventWith(eventName: String, variantId: String, ignoreSnowplow: Boolean = false) {
        paymentEventManager.eventWith(eventName, variantId,ignoreSnowplow = ignoreSnowplow)
    }

    fun publishEventWith(eventName: String, variantId: String, paymentSource: String, ignoreSnowplow: Boolean = false) {
        paymentEventManager.eventWith(eventName, variantId, paymentSource, ignoreSnowplow = ignoreSnowplow)
    }

    private val _configLiveData: MutableLiveData<Outcome<ConfigData>> = MutableLiveData()

    val configLiveData: LiveData<Outcome<ConfigData>>
        get() = _configLiveData

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

}