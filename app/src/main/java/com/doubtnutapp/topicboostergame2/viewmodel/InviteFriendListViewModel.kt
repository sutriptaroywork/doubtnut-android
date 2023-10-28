package com.doubtnutapp.topicboostergame2.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.topicboostergame2.Friend
import com.doubtnutapp.data.remote.models.topicboostergame2.FriendsList
import com.doubtnutapp.data.remote.repository.TopicBoosterGameRepository2
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

class InviteFriendListViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val topicBoosterGameRepository: TopicBoosterGameRepository2,
    analyticsPublisher: AnalyticsPublisher,
) : BaseViewModel(compositeDisposable) {

    private val _friendsListLiveData = MutableLiveData<Outcome<FriendsList>>()
    val friendsListLiveData: LiveData<Outcome<FriendsList>>
        get() = _friendsListLiveData

    var friendsList = emptyList<Friend>()
        private set

    fun getFriendsList(tabId: Int, source: String? = null) {
        _friendsListLiveData.value = Outcome.loading(true)
        viewModelScope.launch {
            topicBoosterGameRepository.getFriendsList(tabId, source)
                .catch {
                    _friendsListLiveData.value = Outcome.loading(false)
                }
                .collect {
                    friendsList = it.data.friendsList.orEmpty()
                    _friendsListLiveData.value = Outcome.loading(false)
                    _friendsListLiveData.postValue(Outcome.success(it.data))

                }
        }
    }
}