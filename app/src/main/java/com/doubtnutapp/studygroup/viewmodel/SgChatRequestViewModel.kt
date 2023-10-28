package com.doubtnutapp.studygroup.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.plus
import com.doubtnutapp.studygroup.model.AcceptMessageRequestData
import com.doubtnutapp.studygroup.service.StudyGroupRepository
import io.reactivex.disposables.CompositeDisposable
import java.util.*
import javax.inject.Inject

class SgChatRequestViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val analyticsPublisher: AnalyticsPublisher,
    private val studyGroupRepository: StudyGroupRepository,
) : BaseViewModel(compositeDisposable) {

    private val _acceptRequestLiveData: MutableLiveData<AcceptMessageRequestData> =
        MutableLiveData()
    val acceptRequestLiveData: LiveData<AcceptMessageRequestData>
        get() = _acceptRequestLiveData

    private val _rejectRequestLiveData: MutableLiveData<AcceptMessageRequestData> =
        MutableLiveData()
    val rejectRequestLiveData: LiveData<AcceptMessageRequestData>
        get() = _rejectRequestLiveData

    fun acceptMessageRequest(chatId: String) {
        compositeDisposable +
                studyGroupRepository.acceptMessageRequest(chatId)
                    .applyIoToMainSchedulerOnSingle()
                    .subscribeToSingle(
                        {
                            _acceptRequestLiveData.postValue(it.data)
                        }, {
                            it.printStackTrace()
                        }
                    )
    }

    fun rejectMessageRequest(chatId: String) {
        compositeDisposable +
                studyGroupRepository.rejectMessageRequest(chatId)
                    .applyIoToMainSchedulerOnSingle()
                    .subscribeToSingle(
                        {
                            _rejectRequestLiveData.postValue(it.data)
                        }, {
                            it.printStackTrace()
                        }
                    )
    }

    fun sendEvent(eventName: String, params: HashMap<String, Any> = hashMapOf(), ignoreSnowplow: Boolean = false) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, params, ignoreSnowplow = ignoreSnowplow))
    }
}