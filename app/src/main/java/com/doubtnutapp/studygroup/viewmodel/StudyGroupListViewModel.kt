package com.doubtnutapp.studygroup.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToCompletable
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.socket.mapper.SocketMessageMapper
import com.doubtnutapp.studygroup.model.InvitedToStudyGroup
import com.doubtnutapp.studygroup.model.StudyGroupList
import com.doubtnutapp.studygroup.service.StudyGroupRepository
import com.doubtnutapp.widgetmanager.widgets.StudyGroupParentWidget
import com.google.gson.Gson
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class StudyGroupListViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val analyticsPublisher: AnalyticsPublisher,
    private val userPreference: UserPreference,
    private val studyGroupRepository: StudyGroupRepository,
    private val gson: Gson,
    private val socketMessageMapper: SocketMessageMapper
) : BaseViewModel(compositeDisposable) {

    private val _groupListLiveData: MutableLiveData<StudyGroupList> = MutableLiveData()
    val groupListLiveData: LiveData<StudyGroupList>
        get() = _groupListLiveData

    private val _progressLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val progressLiveData: LiveData<Boolean>
        get() = _progressLiveData

    private val _invitedLiveData: MutableLiveData<InvitedToStudyGroup> = MutableLiveData()
    val invitedLiveData: LiveData<InvitedToStudyGroup>
        get() = _invitedLiveData

    private val _messageToMultipleGroups: MutableLiveData<Boolean> = MutableLiveData()
    val messageToMultipleGroups: LiveData<Boolean>
        get() = _messageToMultipleGroups

    private val _enableSendBtnStatus: MutableLiveData<Boolean> = MutableLiveData()
    val enableSendBtnStatus: LiveData<Boolean>
        get() = _enableSendBtnStatus

    var selectedGroupList = mutableSetOf<String>()
    var isButtonEnable = false

    fun getGroupList() {
        compositeDisposable.add(
            studyGroupRepository.getGroupList()
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(
                    {
                        _groupListLiveData.postValue(it)
                    },
                    {
                        it.printStackTrace()
                    }
                )
        )
    }

    fun getInvitationStatus(invitee: String) {
        compositeDisposable.add(
            studyGroupRepository.getInvitationStatus(invitee)
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(
                    {
                        _groupListLiveData.postValue(it)
                    },
                    {
                        it.printStackTrace()
                    }
                )
        )
    }

    fun inviteToStudyGroup(groupId: String, invitee: String) {
        compositeDisposable.add(
            studyGroupRepository.inviteToStudyGroup(groupId, invitee)
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(
                    {
                        _invitedLiveData.postValue(it)
                    },
                    {
                        it.printStackTrace()
                    }
                )
        )
    }

    fun addIdIntoSelectedGroupList(groupId: String) {
        val isGroupAdded = selectedGroupList.contains(groupId)
        if (isGroupAdded) {
            selectedGroupList.remove(groupId)
        } else {
            selectedGroupList.add(groupId)
        }
        if (selectedGroupList.size > 0 && isButtonEnable.not()) {
            isButtonEnable = true
            _enableSendBtnStatus.postValue(true)
        } else if (selectedGroupList.size == 0) {
            isButtonEnable = false
            _enableSendBtnStatus.postValue(false)
        }
    }

    fun postMessageToMultipleGroups(message: WidgetEntityModel<*, *>?) {
        _progressLiveData.postValue(true)
        compositeDisposable.add(
            studyGroupRepository.postMessageToMultipleGroups(
                message = message,
                roomList = selectedGroupList.toList()
            ).applyIoToMainSchedulerOnCompletable()
                .subscribeToCompletable(
                    {
                        _messageToMultipleGroups.postValue(true)
                    },
                    {
                        _progressLiveData.postValue(false)
                        it.printStackTrace()
                    }
                )
        )

    }

    fun sendEvent(
        eventName: String,
        params: HashMap<String, Any> = hashMapOf(),
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

    fun getVideoThumbnailParentWidget(
        imageUrl: String, ocrText: String?, questionId: String, page: String, roomId: String?
    ): StudyGroupParentWidget.Model =
        socketMessageMapper.getVideoThumbnailParentWidget(
            imageUrl = imageUrl,
            ocrText = ocrText,
            questionId = questionId,
            page = page,
            roomId = roomId
        )

    fun getParentTextWidget(message:String, roomId: String?,studentId:String):StudyGroupParentWidget.Model{
       return socketMessageMapper.getTextParentWidget(message=message,roomId=roomId, studentId = studentId)
    }

    fun publishTimeSpentEvent(
        category: String,
        action: String,
        params: java.util.HashMap<String, Any>
    ) {
        analyticsPublisher.publishEvent(
            StructuredEvent(
                category = category,
                action = action,
                eventParams = params
            )
        )
    }
}