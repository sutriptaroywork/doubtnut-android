package com.doubtnutapp.quicksearch.viewmodel

import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.plus
import com.doubtnutapp.studygroup.service.StudyGroupRepository
import com.doubtnutapp.studygroup.ui.activity.StudyGroupActivity
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class NotificationSettingViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val userPreference: UserPreference,
    private val analyticsPublisher: AnalyticsPublisher,
    private val studyGroupRepository: StudyGroupRepository
) : BaseViewModel(compositeDisposable) {

    val messageLiveData: MutableLiveData<String> = MutableLiveData()

    fun publishEvent(eventName: String, params: HashMap<String, Any>, ignoreSnowplow: Boolean = false) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, params, ignoreSnowplow = ignoreSnowplow))
    }

    fun muteNotification(type: Int) {
        compositeDisposable +
                studyGroupRepository.muteNotification(
                    type = type,
                    action = StudyGroupActivity.ActionSource.GROUP_CHAT
                )
                    .applyIoToMainSchedulerOnSingle()
                    .subscribeToSingle(
                        {
                            messageLiveData.postValue(it.message)
                            setStudyGroupNotificationMuteStatus(type == 0)
                        },
                        {
                            it.printStackTrace()
                        }
                    )
    }

    fun setDoubtP2pNotificationEnabled(enable: Boolean) =
        userPreference.setDoubtP2pNotificationEnabled(enable)

    fun isDoubtP2pNotificationEnabled() = userPreference.isDoubtP2pNotificationEnabled()

    private fun setStudyGroupNotificationMuteStatus(isMute: Boolean) =
        userPreference.setStudyGroupNotificationMuteStatus(isMute)

    fun isStudyGroupNotificationMuted() = userPreference.isStudyGroupNotificationMuted()
}