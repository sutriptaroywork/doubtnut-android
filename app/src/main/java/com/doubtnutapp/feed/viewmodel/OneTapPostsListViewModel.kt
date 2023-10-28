package com.doubtnutapp.feed.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doubtnut.core.entitiy.BaseResponse
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.feed.entity.OneTapPostsResponse
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.launch
import javax.inject.Inject

class OneTapPostsListViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
) : BaseViewModel(compositeDisposable) {

    private val repository = DataHandler.INSTANCE.teslaRepository

    private val _mutableLiveDataPosts: MutableLiveData<Outcome<OneTapPostsResponse>> =
        MutableLiveData()
    val liveDataPosts get():LiveData<Outcome<OneTapPostsResponse>> = _mutableLiveDataPosts

    private val _mutableLiveDataCreatePost: MutableLiveData<Outcome<BaseResponse>> =
        MutableLiveData()
    val liveDataCreateOneTapPost get():LiveData<Outcome<BaseResponse>> = _mutableLiveDataCreatePost

    fun getOneTapPosts(page: String, carouselType: String?) {
        viewModelScope.launch {
            _mutableLiveDataPosts.postValue(Outcome.loading(true))
            try {
                val response = repository.getAllOneTapPosts(page, carouselType)
                _mutableLiveDataPosts.value = Outcome.success(response)
                _mutableLiveDataPosts.postValue(Outcome.loading(false))
            } catch (e: Exception) {
                _mutableLiveDataPosts.postValue(Outcome.loading(false))
                _mutableLiveDataPosts.postValue(Outcome.apiError(e))
            }
        }
    }

    fun createOneTapPost(id: String) {
        viewModelScope.launch {
            _mutableLiveDataCreatePost.postValue(Outcome.loading(true))
            try {
                val response = repository.createOneTapPost(id)
                _mutableLiveDataCreatePost.postValue(Outcome.success(response))
                _mutableLiveDataCreatePost.value = Outcome.loading(false)
            } catch (e: Exception) {
                e.printStackTrace()
                _mutableLiveDataCreatePost.postValue(Outcome.apiError(e))
            }
        }
    }

}