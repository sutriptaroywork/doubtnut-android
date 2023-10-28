package com.doubtnutapp.dnr.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.data.DefaultDataStore
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.dnr.model.BaseDnrReward
import com.doubtnutapp.dnr.model.DnrReward
import com.doubtnutapp.dnr.model.DnrRewardType
import com.doubtnutapp.studygroup.service.DnrRepository
import com.doubtnutapp.utils.Event
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

class DnrRewardViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val analyticsPublisher: AnalyticsPublisher,
    private val dnrRepository: DnrRepository,
    private val defaultDataStore: DefaultDataStore
) : BaseViewModel(compositeDisposable) {

    enum class RewardPopupType(val type: String) {
        REWARD_BOTTOM_SHEET("bottom_sheet"),
        REWARD_DIALOG("dialog"),
        NO_POPUP("no_popup")
    }

    private val _dnrRewardPopupLiveData: MutableLiveData<Event<RewardPopupType>> = MutableLiveData()
    val dnrRewardPopupLiveData: LiveData<Event<RewardPopupType>>
        get() = _dnrRewardPopupLiveData

    private val _dnrRewardLiveData: MutableLiveData<Event<DnrReward>> = MutableLiveData()
    val dnrRewardLiveData: LiveData<Event<DnrReward>>
        get() = _dnrRewardLiveData

    fun claimReward(baseDnrReward: BaseDnrReward) {
        compositeDisposable.add(
            dnrRepository.claimReward(baseDnrReward)
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(
                    {
                        if (baseDnrReward.type == DnrRewardType.COURSE_PURCHASE.type || baseDnrReward.type == DnrRewardType.PDF_PURCHASE.type)
                            return@subscribeToSingle
                        sendEvent(
                            EventConstants.DNR_REWARD_CLAIM,
                            hashMapOf(
                                EventConstants.TYPE to baseDnrReward.type
                            ),
                            ignoreSnowplow = true
                        )
                        _dnrRewardLiveData.postValue(Event(it))
                    },
                    {
                        it.printStackTrace()
                    }
                )
        )
    }

    fun markCoursePurchased() {
        compositeDisposable.add(
            dnrRepository.markCoursePurchased()
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(
                    {
                        _dnrRewardLiveData.postValue(Event(it))
                    },
                    {
                        it.printStackTrace()
                    }
                )
        )
    }

    fun checkRewardPopupToBeShown(dnrReward: DnrReward) {
        viewModelScope.launch {
            val noOfTimesDnrRewardBottomSheetShown =
                defaultDataStore.getNoOfTimesDnrRewardBottomSheetShown(dnrReward.type).firstOrNull()
                    ?: 0
            val noOfTimesDnrRewardDialogShown =
                defaultDataStore.getNoOfTimesDnrRewardDialogShown(dnrReward.type).firstOrNull()
                    ?: 0
            val maxNoOfTimesAnyDnrPopupCanBeShown =
                dnrReward.maxNoOfAnyDnrRewardPopUpShownCount ?: Int.MAX_VALUE

            val isNoMaxLimitForBottomSheetType = dnrReward.noMaxLimitForBottomSheetType?:false

            if (maxNoOfTimesAnyDnrPopupCanBeShown <= noOfTimesDnrRewardBottomSheetShown + noOfTimesDnrRewardDialogShown) {
                _dnrRewardPopupLiveData.postValue(Event(RewardPopupType.NO_POPUP))
                return@launch
            }
            if (noOfTimesDnrRewardBottomSheetShown < dnrReward.maxNoOfRewardBottomSheetShownCount || isNoMaxLimitForBottomSheetType) {
                _dnrRewardPopupLiveData.postValue(Event(RewardPopupType.REWARD_BOTTOM_SHEET))
            } else {
                _dnrRewardPopupLiveData.postValue(Event(RewardPopupType.REWARD_DIALOG))
            }
        }
    }

    fun sendEvent(eventName: String, params: HashMap<String, Any> = hashMapOf(), ignoreSnowplow: Boolean = false) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, params, ignoreSnowplow = ignoreSnowplow))
    }
}
