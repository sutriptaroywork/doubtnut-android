package com.doubtnutapp.freeTrialCourse.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnutapp.Log
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.freeTrialCourse.model.FreeTrialCourseResponse
import com.doubtnutapp.freeTrialCourse.model.FreeTrialCourseActivationResponse
import com.doubtnutapp.freeTrialCourse.repository.FreeTrialCourseRepository
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject

class FreeTrialCourseViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val freeTrialCourseRepository: FreeTrialCourseRepository
) : BaseViewModel(compositeDisposable) {

    private var _mutableLiveDataFreeCourseReponse: MutableLiveData<Outcome<FreeTrialCourseResponse>> =
        MutableLiveData()
    val freeCoursesLiveData: LiveData<Outcome<FreeTrialCourseResponse>> =
        _mutableLiveDataFreeCourseReponse

    private var _mutableLiveDataSubscribeCourseReponse: MutableLiveData<Outcome<FreeTrialCourseActivationResponse>> =
        MutableLiveData()
    val subscribeToCourseLiveData: LiveData<Outcome<FreeTrialCourseActivationResponse>> =
        _mutableLiveDataSubscribeCourseReponse

    var courseIdSelected=-1;
    var deeplinkForCardSelected:String=""

    var freeTrialCourseListResponse:FreeTrialCourseResponse?=null


    fun fetchFreeLiveCourses() {
        _mutableLiveDataFreeCourseReponse.value = Outcome.loading(true)
        viewModelScope.launch {
            try {
                val response = freeTrialCourseRepository.fetchFreeTrialCourses()
                _mutableLiveDataFreeCourseReponse.postValue(Outcome.success(response))
                freeTrialCourseListResponse = response
                withContext(Dispatchers.Main) {
                    _mutableLiveDataFreeCourseReponse.value = Outcome.loading(false)
                }

            } catch (e: Exception) {
                onResponseError(e)
                _mutableLiveDataFreeCourseReponse.postValue(Outcome.loading(false))

            }
        }
    }

    fun activateCourse(courseId:Int) {
        _mutableLiveDataSubscribeCourseReponse.value = Outcome.loading(true)
        viewModelScope.launch {
            try {
                val response = freeTrialCourseRepository.subscribeToCourse(courseId)
                _mutableLiveDataSubscribeCourseReponse.postValue(Outcome.success(response))
                withContext(Dispatchers.Main) {
                    _mutableLiveDataSubscribeCourseReponse.value = Outcome.loading(false)
                }

            } catch (e: Exception) {
                onResponseErrorSubscriptionActivate(e)
                _mutableLiveDataSubscribeCourseReponse.postValue(Outcome.loading(false))
            }
        }
    }

    private fun onResponseError(error: Throwable) {
        _mutableLiveDataFreeCourseReponse.postValue(
            if (error is HttpException) {
                when (error.response()?.code()) {
                    HttpURLConnection.HTTP_BAD_REQUEST -> Outcome.apiError(error)
                    HttpURLConnection.HTTP_UNAUTHORIZED -> Outcome.badRequest(error.message())
                    else -> Outcome.failure(error)
                }
            } else {
                Outcome.failure(error)
            }
        )
    }

    private fun onResponseErrorSubscriptionActivate(error: Throwable){
        _mutableLiveDataSubscribeCourseReponse.postValue(
            if (error is HttpException) {
                when (error.response()?.code()) {
                    HttpURLConnection.HTTP_BAD_REQUEST -> Outcome.apiError(error)
                    HttpURLConnection.HTTP_UNAUTHORIZED -> Outcome.badRequest(error.message())
                    else -> Outcome.failure(error)
                }
            } else {
                Outcome.failure(error)
            }
        )
    }
}