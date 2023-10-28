package com.doubtnutapp.topicboostergame2.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnutapp.Constants
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.topicboostergame2.Friend
import com.doubtnutapp.data.remote.models.topicboostergame2.NumberInvite
import com.doubtnutapp.data.remote.models.topicboostergame2.TbgInviteData
import com.doubtnutapp.data.remote.repository.TopicBoosterGameRepository2
import com.doubtnutapp.packageInstallerCheck.CheckForPackageInstall
import com.doubtnutapp.utils.Event
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

class InviteFriendsViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val topicBoosterGameRepository: TopicBoosterGameRepository2,
    private val analyticsPublisher: AnalyticsPublisher,
    private val checkForPackageInstall: CheckForPackageInstall,
) : BaseViewModel(compositeDisposable) {

    //region LiveData
    private val _tbgInviteLiveData = MutableLiveData<Outcome<TbgInviteData>>()
    val tbgInviteLiveData: LiveData<Outcome<TbgInviteData>>
        get() = _tbgInviteLiveData

    private val _isAnyMemberInvitedLiveData = MutableLiveData(false)
    val isAnyMemberInvitedLiveData: LiveData<Boolean>
        get() = _isAnyMemberInvitedLiveData

    private val _sendInvitationLiveData = MutableLiveData<Event<Boolean>>()
    val sendInvitationLiveData: LiveData<Event<Boolean>>
        get() = _sendInvitationLiveData

    private val _friendsSearchQueryLiveData = MutableLiveData<String>()
    val friendsSearchQueryLiveData: MutableLiveData<String>
        get() = _friendsSearchQueryLiveData

    private val _sendNumberInviteLiveData = MutableLiveData<NumberInvite>()
    val sendNumberInviteLiveData: LiveData<NumberInvite>
        get() = _sendNumberInviteLiveData


    private val _selectSingleFriendLiveData = MutableLiveData<Event<Friend>>()
    val selectSingleFriendLiveData: LiveData<Event<Friend>>
        get() = _selectSingleFriendLiveData
    //endregion

    private val inviteeSet = mutableSetOf<Long>()

    val inviteeIds: Array<String>
        get() = inviteeSet.map { it.toString() }.toTypedArray()

    var friendsListMap = hashMapOf<String, List<Friend>>()
        private set

    fun getInviteFriendData(topic: String, source: String? = null) {
        _tbgInviteLiveData.value = Outcome.loading(true)
        viewModelScope.launch {
            topicBoosterGameRepository.getTbgInviteData(topic, source)
                .catch {
                    _tbgInviteLiveData.value = Outcome.loading(false)
                    _tbgInviteLiveData.value = Outcome.failure(it)
                }
                .collect {
                    _tbgInviteLiveData.value = Outcome.loading(false)
                    _tbgInviteLiveData.value = Outcome.success(it.data)
                }
        }
    }

    fun sendTbgInvitation(gameId: String, topic: String) {
        viewModelScope.launch {
            topicBoosterGameRepository.sendTbgInvitation(inviteeSet.toList(), gameId, topic)
                .catch { }
                .collect {
                    _sendInvitationLiveData.value = Event(true)
                }
        }
    }

    fun sendNumberInvitation(gameId: String?,mobileNo : String, chapter: String?, source: String? = null){
        viewModelScope.launch {
            topicBoosterGameRepository.sendNumberInvitation(gameId.orEmpty(), mobileNo, chapter.orEmpty(), source)
                .catch {

                }
                .collect {
                    _sendNumberInviteLiveData.value = it.data
                }
        }
    }

    fun searchFriends(query: String) {
        _friendsSearchQueryLiveData.value = query
    }

    fun addInvitee(studentID: Long) {
        inviteeSet.add(studentID)
        if (_isAnyMemberInvitedLiveData.value == false) {
            _isAnyMemberInvitedLiveData.value = true
        }
    }

    fun removeInvitee(studentID: Long) {
        inviteeSet.remove(studentID)
        if (inviteeSet.isEmpty()) {
            _isAnyMemberInvitedLiveData.value = false
        }
    }

    fun isWhatsAppInstalled() = checkForPackageInstall.appInstalled(Constants.WHATSAPP_PACKAGE_NAME)

    fun setSelectedFriendLiveData(friend: Friend){
        _selectSingleFriendLiveData.value = Event(friend)
    }

    fun sendEvent(eventName: String, params: HashMap<String, Any> = hashMapOf()) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, params))
    }
}