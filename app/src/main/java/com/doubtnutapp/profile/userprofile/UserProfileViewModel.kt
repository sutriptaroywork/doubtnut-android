package com.doubtnutapp.profile.userprofile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.onboarding.model.ApiOnBoardingStatus
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.ProfileData
import com.doubtnutapp.liveclass.viewmodel.ReferralData
import com.doubtnutapp.plus
import com.doubtnutapp.studygroup.model.SendMessageRequestData
import com.doubtnutapp.studygroup.service.StudyGroupRepository
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class UserProfileViewModel @Inject constructor(
    val analyticsPublisher: AnalyticsPublisher,
    compositeDisposable: CompositeDisposable,
    private val studyGroupRepository: StudyGroupRepository,
    private val userPreference: UserPreference,
) : BaseViewModel(compositeDisposable) {

    fun getUserProile(
        studentId: String,
        authPageOpenCount: Int
    ): RetrofitLiveData<ApiResponse<ProfileData>> {
        return DataHandler.INSTANCE.teslaRepository.getUserProfile(studentId, authPageOpenCount)
    }

    fun followUser(studentId: String) {
        DataHandler.INSTANCE.teslaRepository.followUser(studentId).subscribeOn(Schedulers.io())
            .subscribe()
    }

    fun unfollowUser(studentId: String) {
        DataHandler.INSTANCE.teslaRepository.unfollowUser(studentId).subscribeOn(Schedulers.io())
            .subscribe()
    }

    fun eventWith(eventName: String, ignoreSnowplow: Boolean = false) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                eventName,
                hashMapOf(),
                ignoreSnowplow = ignoreSnowplow
            )
        )
    }

    fun eventWith(
        eventName: String,
        params: HashMap<String, Any>,
        ignoreSnowplow: Boolean = false
    ) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                eventName,
                params,
                ignoreSnowplow = ignoreSnowplow
            )
        )
    }

    private val _referralData: MutableLiveData<Outcome<ReferralData>> = MutableLiveData()

    val referralData: LiveData<Outcome<ReferralData>>
        get() = _referralData

    private val _studyDost = MutableLiveData<ApiOnBoardingStatus.ApiStudyDost?>()

    val studyDost: LiveData<ApiOnBoardingStatus.ApiStudyDost?>
        get() = _studyDost

    private val _sendMessageRequestLiveData: MutableLiveData<Outcome<SendMessageRequestData>> =
        MutableLiveData()
    val sendMessageRequestLiveData: LiveData<Outcome<SendMessageRequestData>>
        get() = _sendMessageRequestLiveData

    fun getReferralData(type: String?, assortmentType: String?) {
        viewModelScope.launch {
            DataHandler.INSTANCE.referralRepository.getReferralData(type, assortmentType, null)
                .map { it.data }
                .catch {
                    _referralData.value = Outcome.loading(false)
                }
                .collect {
                    _referralData.value = Outcome.success(it)
                    _referralData.value = Outcome.loading(false)
                }
        }
    }

    fun requestForStudyDost() {
        compositeDisposable.add(
            DataHandler.INSTANCE.studyDostRepository
                .requestForStudyDost()
                .applyIoToMainSchedulerOnSingle()
                .subscribe(
                    {
                        userPreference.updateStudyDostData(it.data)
                        _studyDost.postValue(it.data)
                    },
                    {
                        it.printStackTrace()
                    }
                ))
    }

    fun sendMessageRequest(inviteeId: String) {
        _sendMessageRequestLiveData.value = Outcome.loading(true)
        compositeDisposable + studyGroupRepository.sendMessageRequest(inviteeId, null)
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle(
                {
                    _sendMessageRequestLiveData.value = Outcome.success(it.data)
                    _sendMessageRequestLiveData.value = Outcome.loading(false)
                },
                {
                    _sendMessageRequestLiveData.value = Outcome.loading(false)
                    _sendMessageRequestLiveData.value = Outcome.apiError(it)
                    it.printStackTrace()
                })
    }

    fun getDoubtP2pData() = userPreference.getDoubtP2pData()

    fun getKheloAurJeetoData() = userPreference.getKheloAurJeetoData()

    fun getDoubtFeed2Data() = userPreference.getDoubtFeed2Data()

    fun getRevisionCornerData() = userPreference.getRevisionCornerData()

}
