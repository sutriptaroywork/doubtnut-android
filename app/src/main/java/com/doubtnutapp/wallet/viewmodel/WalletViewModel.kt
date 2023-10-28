package com.doubtnutapp.wallet.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.RecommendedCourses
import com.doubtnutapp.data.remote.models.WalletData
import com.doubtnutapp.data.remote.repository.WalletRepository
import com.doubtnutapp.domain.payment.entities.PaymentLinkCreate
import com.doubtnutapp.domain.payment.interactor.PaymentLinkUseCase
import com.doubtnutapp.plus
import com.doubtnutapp.utils.Event
import com.doubtnutapp.wallet.BestSellerData
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 20/11/20.
 */
class WalletViewModel @Inject constructor(
        compositeDisposable: CompositeDisposable,
        private val walletRepository: WalletRepository,
        private val paymentLinkUseCase: PaymentLinkUseCase)
    : BaseViewModel(compositeDisposable) {

    private val _walletData
            : MutableLiveData<Outcome<WalletData>> = MutableLiveData()

    val walletData: LiveData<Outcome<WalletData>>
        get() = _walletData

    private val _recommendedCoursesLiveData = MutableLiveData<Outcome<RecommendedCourses>>()
    val recommendedCoursesLiveData: LiveData<Outcome<RecommendedCourses>>
        get() = _recommendedCoursesLiveData

    fun startLoading() {
        _walletData.value = Outcome.loading(true)
    }

    fun stopLoading() {
        _walletData.value = Outcome.loading(false)
    }

    fun fetchWalletData() {
        startLoading()
        viewModelScope.launch {
            walletRepository.fetchWalletData()
                    .map { it.data }
                    .catch {
                        stopLoading()
                    }
                    .collect {
                        _walletData.value = Outcome.success(it)
                        stopLoading()
                    }
        }
    }

    private val _paymentLinkLiveData:
            MutableLiveData<Event<PaymentLinkCreate>> = MutableLiveData()

    val paymentLinkLiveData: LiveData<Event<PaymentLinkCreate>>
        get() = _paymentLinkLiveData

    fun requestPaymentLink(paymentLinkData: PaymentLinkUseCase.Param) {
        startLoading()
        compositeDisposable + paymentLinkUseCase
                .execute(paymentLinkData)
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle({
                    stopLoading()
                    _paymentLinkLiveData.postValue(Event(it))
                }, this::onError)
    }

    private val _messageStringIdLiveData: MutableLiveData<Event<Int>> = MutableLiveData()

    val messageStringIdLiveData: LiveData<Event<Int>>
        get() = _messageStringIdLiveData

    private fun onError(error: Throwable) {
        stopLoading()
        _messageStringIdLiveData.value = Event(R.string.something_went_wrong)
    }

    private val _bestSellerLiveData: MutableLiveData<Outcome<BestSellerData>> = MutableLiveData()

    val bestSellerLiveData: LiveData<Outcome<BestSellerData>>
        get() = _bestSellerLiveData

    fun getBestSellerData() {
        _bestSellerLiveData.value = Outcome.loading(true)
        viewModelScope.launch {
            DataHandler.INSTANCE.courseRepository.getBestSellerData()
                    .map { it.data }
                    .catch {
                        _bestSellerLiveData.value = Outcome.loading(false)
                    }
                    .collect {
                        _bestSellerLiveData.value = Outcome.success(it)
                        _bestSellerLiveData.value = Outcome.loading(false)
                    }
        }
    }

    fun getRecommendedCourses() {
        _recommendedCoursesLiveData.value = Outcome.loading(true)
        viewModelScope.launch {
            DataHandler.INSTANCE.courseRepository.getRecommendedCourses()
                    .map { it.data }
                    .catch { e ->
                        _recommendedCoursesLiveData.value = Outcome.loading(false)
                        _recommendedCoursesLiveData.value = Outcome.failure(e)
                    }
                    .collect {
                        _recommendedCoursesLiveData.value = Outcome.loading(false)
                        _recommendedCoursesLiveData.value = Outcome.success(it)
                    }
        }
    }
}