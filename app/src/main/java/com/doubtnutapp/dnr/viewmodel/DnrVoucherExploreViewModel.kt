package com.doubtnutapp.dnr.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.dnr.model.*
import com.doubtnutapp.studygroup.service.DnrRepository
import com.doubtnutapp.utils.Event
import io.reactivex.disposables.CompositeDisposable
import java.util.*
import javax.inject.Inject

class DnrVoucherExploreViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val analyticsPublisher: AnalyticsPublisher,
    private val dnrRepository: DnrRepository,
) : BaseViewModel(compositeDisposable) {

    private val _voucherStateLiveData: MutableLiveData<Event<VoucherState>> = MutableLiveData()
    val voucherStateLiveData: LiveData<Event<VoucherState>>
        get() = _voucherStateLiveData

    private val _initialInfoLiveData: MutableLiveData<Event<VoucherInitialData>> = MutableLiveData()
    val initialInfoLiveData: LiveData<Event<VoucherInitialData>>
        get() = _initialInfoLiveData

    private val _mysteryBoxLiveData: MutableLiveData<Event<DnrMysteryBoxInitialData>> =
        MutableLiveData()
    val mysteryBoxLiveData: LiveData<Event<DnrMysteryBoxInitialData>>
        get() = _mysteryBoxLiveData

    private val _spinTheWheelData: MutableLiveData<Event<DnrSpinTheWheelInitialData>> = MutableLiveData()
    val spinTheWheelData: LiveData<Event<DnrSpinTheWheelInitialData>>
        get() = _spinTheWheelData

    private val _navigateToDeeplinkLiveData: MutableLiveData<Event<Pair<String?, Int>>> = MutableLiveData()
    val navigateToDeeplinkLiveData: LiveData<Event<Pair<String?, Int>>>
        get() = _navigateToDeeplinkLiveData

    private var loadingData: VoucherLoadingData? = null
    private var errorData: VoucherErrorData? = null
    private var betterLuckNextTime: VoucherErrorData? = null
    var isVoucherRedeemed: Boolean = false

    private fun setLoadingData(data: VoucherLoadingData?) {
        loadingData = data
    }

    private fun setErrorData(data: VoucherErrorData?) {
        errorData = data
    }

    private fun setBetterLuckNextTimeData(data: VoucherErrorData?) {
        betterLuckNextTime = data
    }

    fun changeLoadingState(isLoading: Boolean) {

        loadingData?.apply {
            this.isLoading = isLoading
            voucherStateLayoutVisibility = if (isLoading) {
                VoucherStateLayoutVisibility(
                    isDetailLayoutVisible = false,
                    isErrorLayoutVisible = false,
                    isLoadingLayoutVisible = true
                )
            } else {
                null
            }
            _voucherStateLiveData.value = Event(VoucherState.loading(this))
        }
    }

    fun showBetterLuckNextTime() {
        betterLuckNextTime?.apply {
            voucherStateLayoutVisibility = VoucherStateLayoutVisibility(
                isDetailLayoutVisible = false,
                isErrorLayoutVisible = true,
                isLoadingLayoutVisible = false
            )
            _voucherStateLiveData.value = Event(VoucherState.error(this))
        }
    }

    fun getVoucherStateData(voucherId: String, redeemId: String, source: String? = null) {
        compositeDisposable.add(
            dnrRepository.getVoucherStateData(voucherId, redeemId, source.orEmpty())
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(
                    {
                        _initialInfoLiveData.value = Event(it)
                        it.lockedStateData?.voucherStateLayoutVisibility =
                            VoucherStateLayoutVisibility(
                                isDetailLayoutVisible = true,
                                isErrorLayoutVisible = false,
                                isLoadingLayoutVisible = false
                            )

                        setLoadingData(it.loadingStateData)
                        setErrorData(it.errorStateData)

                        if (it.lockedStateData != null) {
                            _voucherStateLiveData.value = Event(VoucherState.lock(it.lockedStateData))
                        } else {
                            changeLoadingState(true)
                        }

                        if (it.warningContainer != null) {
                            it.warningContainer.deeplink = it.loadingStateData?.deeplink
                            _voucherStateLiveData.value = Event(VoucherState.notEnoughDnr(it.warningContainer))
                        } else if (it.unlockedStateData != null) {
                            _voucherStateLiveData.value = Event(VoucherState.Unlocked(it.unlockedStateData))
                        }
                    },
                    {
                        it.printStackTrace()
                    }
                )
        )
    }

    fun redeemVoucher(voucherId: String, source: String? = null) {
        changeLoadingState(true)
        sendEvent(
            EventConstants.DNR_VOUCHER_UNLOCK_CLICKED,
            hashMapOf(
                EventConstants.SOURCE to source.orEmpty(),
                EventConstants.VOUCHER_ID to voucherId
            ),
            ignoreSnowplow = true
        )
        val startTime = System.currentTimeMillis()
        compositeDisposable.add(
            dnrRepository.redeemVoucher(voucherId, source.orEmpty())
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(
                    {
                        if (source == DnrVoucherSource.BETTER_LUCK_NEXT_TIME.type) return@subscribeToSingle
                        changeLoadingState(false)
                        when (source) {
                            DnrVoucherSource.SPIN_THE_WHEEL.type, DnrVoucherSource.MYSTERY_BOX.type -> {
                                _initialInfoLiveData.value = Event(it)
                                it.lockedStateData?.voucherStateLayoutVisibility =
                                    VoucherStateLayoutVisibility(
                                        isDetailLayoutVisible = true,
                                        isErrorLayoutVisible = false,
                                        isLoadingLayoutVisible = false
                                    )
                                if (it.lockedStateData != null) {
                                    _voucherStateLiveData.value = Event(VoucherState.lock(it.lockedStateData))
                                }
                            }
                        }
                        it.unlockedStateData?.voucherStateLayoutVisibility =
                            VoucherStateLayoutVisibility(
                                isDetailLayoutVisible = true,
                                isErrorLayoutVisible = false,
                                isLoadingLayoutVisible = false
                            )
                        if (it.unlockedStateData != null) {
                            _voucherStateLiveData.value = Event(VoucherState.Unlocked(it.unlockedStateData))
                        }
                    },
                    {
                        // Show error state layout only when error occur before 55 sec
                        if (source == DnrVoucherSource.BETTER_LUCK_NEXT_TIME.type) return@subscribeToSingle
                        if (System.currentTimeMillis() - startTime < (loadingData?.duration ?: 5000L)) {
                            it.printStackTrace()
                            errorData?.voucherStateLayoutVisibility = VoucherStateLayoutVisibility(
                                isDetailLayoutVisible = false,
                                isErrorLayoutVisible = true,
                                isLoadingLayoutVisible = false
                            )
                            if (errorData != null) {
                                _voucherStateLiveData.postValue(Event(VoucherState.error(errorData!!)))
                            }
                        }
                    }
                )
        )
    }

    fun getMysteryBoxData() {
        compositeDisposable.add(
            dnrRepository.getMysteryBoxData()
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(
                    {
                        setLoadingData(it.loadingStateData)
                        setErrorData(it.errorStateData)
                        _mysteryBoxLiveData.postValue(Event(it))
                    },
                    {
                        it.printStackTrace()
                    }
                )
        )
    }

    fun getSpinTheWheelData() {
        compositeDisposable.add(
            dnrRepository.getSpinTheWheelData()
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(
                    {
                        setLoadingData(it.loadingStateData)
                        setErrorData(it.errorStateData)
                        setBetterLuckNextTimeData(it.betterLuckNextTimeData)
                        _spinTheWheelData.postValue(Event(it))
                    },
                    {
                        it.printStackTrace()
                    }
                )
        )
    }

    fun navigateToDeeplink(deeplink: String?, popupToFragment: Int) {
        _navigateToDeeplinkLiveData.value = Event(Pair(deeplink, popupToFragment))
    }

    fun sendEvent(eventName: String, params: HashMap<String, Any> = hashMapOf(), ignoreSnowplow: Boolean = false) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, params, ignoreSnowplow = ignoreSnowplow))
    }
}
