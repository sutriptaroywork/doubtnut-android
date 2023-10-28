package com.doubtnutapp.liveclass.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.LiveClassFeedbackResponse
import com.doubtnutapp.data.remote.models.LiveClassPollSubmitResponse
import com.doubtnutapp.plus
import io.reactivex.disposables.CompositeDisposable
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject

class LiveClassFeedbackViewModel @Inject constructor(
        compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {

    private val _submitFeedbackLiveData: MutableLiveData<Outcome<LiveClassFeedbackResponse>> = MutableLiveData()

    val submitFeedbackLiveData: LiveData<Outcome<LiveClassFeedbackResponse>>
        get() = _submitFeedbackLiveData


    private val _viewedFeedbackLiveData: MutableLiveData<Outcome<LiveClassFeedbackResponse>> = MutableLiveData()

    val viewedFeedbackLiveData: LiveData<Outcome<LiveClassFeedbackResponse>>
        get() = _viewedFeedbackLiveData

    fun submitFeedback(selectedTagsList: List<String>, detailId: String, rating: String, review: String, engageTime: String) {
        _submitFeedbackLiveData.value = Outcome.loading(true)
        compositeDisposable +
                DataHandler.INSTANCE
                        .courseRepository
                        .submitLiveClassFeedback(hashMapOf<String, Any>().apply {
                            put("detail_id", detailId)
                            put("review", review)
                            put("star_rating", rating)
                            put("engage_time", engageTime)
                            put("options", selectedTagsList.joinToString(","))
                        })
                        .applyIoToMainSchedulerOnSingle()
                        .map {
                            it.data
                        }
                        .subscribeToSingle({
                            _submitFeedbackLiveData.value = Outcome.success(it)
                            _submitFeedbackLiveData.value = Outcome.loading(false)
                        }, {
                            val error = it
                            _submitFeedbackLiveData.value = if (error is HttpException) {
                                when (error.response()?.code()) {
                                    HttpURLConnection.HTTP_UNAUTHORIZED -> Outcome.BadRequest(error.message
                                            ?: "")
                                    HttpURLConnection.HTTP_BAD_REQUEST -> Outcome.ApiError(error)
                                    else -> Outcome.Failure(error)
                                }
                            } else {
                                Outcome.Failure(error)
                            }
                            _submitFeedbackLiveData.value = Outcome.loading(false)
                        })
    }

    fun viewedFeedbackDialog(detailId: String) {
        compositeDisposable +
                DataHandler.INSTANCE
                        .courseRepository
                        .viewedFeedbackDialog(hashMapOf<String, Any>().apply {
                            put("detail_id", detailId)
                        })
                        .applyIoToMainSchedulerOnSingle()
                        .map {
                            it.data
                        }
                        .subscribeToSingle({
                            _viewedFeedbackLiveData.value = Outcome.success(it)
                        }, {
                            val error = it
                            _viewedFeedbackLiveData.value = if (error is HttpException) {
                                when (error.response()?.code()) {
                                    HttpURLConnection.HTTP_UNAUTHORIZED -> Outcome.BadRequest(error.message
                                            ?: "")
                                    HttpURLConnection.HTTP_BAD_REQUEST -> Outcome.ApiError(error)
                                    else -> Outcome.Failure(error)
                                }
                            } else {
                                Outcome.Failure(error)
                            }
                        })
    }
}