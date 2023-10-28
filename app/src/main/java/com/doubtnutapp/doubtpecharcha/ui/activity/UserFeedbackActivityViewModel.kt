package com.doubtnutapp.doubtpecharcha.ui.activity

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnut.core.data.remote.Outcome
import com.doubtnut.core.entitiy.BaseResponse
import com.doubtnut.core.utils.NetworkApiUtils
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.doubtpecharcha.model.DoubtPeCharchaUserFeedbackResponseItem
import com.doubtnutapp.doubtpecharcha.model.FeedbackUserListResponse
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class UserFeedbackActivityViewModel @Inject constructor(compositeDisposable: CompositeDisposable) :
    BaseViewModel(compositeDisposable) {

    private val doubtPeCharchaRepository = DataHandler.INSTANCE.doubtPeCharchaRepository

    private var _mutableLiveDataFeedbackResponse =
        MutableLiveData<Outcome<FeedbackUserListResponse>>()

    val liveDataFeedbackResponse: LiveData<Outcome<FeedbackUserListResponse>> =
        _mutableLiveDataFeedbackResponse

    private var _mutableLiveDataFeedbackForUser =
        MutableLiveData<Outcome<BaseResponse>>()

    val liveDataFeedbackRatingForUser: LiveData<Outcome<BaseResponse>> =
        _mutableLiveDataFeedbackForUser

    var moveDirectlyToRatingFragment = false

    fun getUserFeedbackData(roomId: String) {
        _mutableLiveDataFeedbackResponse.postValue(Outcome.loading(true))
        viewModelScope.launch {
            try {
                val response = doubtPeCharchaRepository.getDoubtPeCharchaFeedbackData(roomId)
                _mutableLiveDataFeedbackResponse.value = Outcome.success(response)
                _mutableLiveDataFeedbackResponse.postValue(Outcome.loading(false))
            } catch (e: Exception) {
                e.printStackTrace()
                NetworkApiUtils.onApiError(_mutableLiveDataFeedbackResponse, e)
            }
        }
    }

    fun sendFeedbackRatingForSelectedUsers(
        roomId: String, studentId: String, rating: Int,
        feedbackOption: String, writtenFeedback: String = ""
    ) {
        viewModelScope.launch {
            try {
                val response = doubtPeCharchaRepository.selectUsersForFeedback(
                    roomId, rating,
                    feedbackOption,
                    writtenFeedback,
                    studentId
                )
                _mutableLiveDataFeedbackForUser.postValue(Outcome.success(response))
            } catch (e: Exception) {
                NetworkApiUtils.onApiError(_mutableLiveDataFeedbackForUser, e)
                e.printStackTrace()
            }
        }
    }


}

