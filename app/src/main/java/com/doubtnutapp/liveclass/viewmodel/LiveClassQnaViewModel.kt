package com.doubtnutapp.liveclass.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.LiveClassPollOptionsData
import com.doubtnutapp.data.remote.models.LiveClassPollSubmitResponse
import com.doubtnutapp.data.remote.models.LiveClassQuizSubmitResponse
import com.doubtnutapp.plus
import io.reactivex.disposables.CompositeDisposable
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-09-29.
 */
class LiveClassQnaViewModel @Inject constructor(
        compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {

    private fun startLoading() {
        _submitResponseLiveData.value = Outcome.loading(true)
    }

    private fun stopLoading() {
        _submitResponseLiveData.value = Outcome.loading(false)
    }

    private val _submitResponseLiveData:
            MutableLiveData<Outcome<Pair<LiveClassQuizSubmitResponse, List<String>>>> = MutableLiveData()

    val submitResponseLiveData: LiveData<Outcome<Pair<LiveClassQuizSubmitResponse, List<String>>>>
        get() = _submitResponseLiveData

    private val _submitPollResponseLiveData: MutableLiveData<Outcome<LiveClassPollSubmitResponse>> = MutableLiveData()

    private val _pollResultLiveData: MutableLiveData<Outcome<List<LiveClassPollOptionsData>>> = MutableLiveData()

    val submitPollResponseLiveData: LiveData<Outcome<LiveClassPollSubmitResponse>>
        get() = _submitPollResponseLiveData

    val getPollResultLiveData: LiveData<Outcome<List<LiveClassPollOptionsData>>>
        get() = _pollResultLiveData


    fun submitLiveClassQuiz(questionId: Int, liveClassId: String, optionIds: List<String>,
                            detailId: Long, resourceDetailId: Long) {
        startLoading()
        compositeDisposable +
                DataHandler.INSTANCE
                        .courseRepository
                        .submitLiveClassQuiz(hashMapOf<String, String>().apply {
                            put("quiz_question_id", questionId.toString())
                            put("liveclass_id", liveClassId)
                            put("selected_options", optionIds.joinToString("::"))
                            put("liveclass_resource_id", detailId.toString())
                            put("quiz_resource_id", resourceDetailId.toString())
                        })
                        .applyIoToMainSchedulerOnSingle()
                        .map {
                            it.data
                        }
                        .subscribeToSingle({
                            _submitResponseLiveData.value = Outcome.success(it to optionIds)
                            stopLoading()
                        }, {
                            val error = it
                            _submitResponseLiveData.value = if (error is HttpException) {
                                when (error.response()?.code()) {
                                    HttpURLConnection.HTTP_UNAUTHORIZED -> Outcome.BadRequest(error.message
                                            ?: "")
                                    HttpURLConnection.HTTP_BAD_REQUEST -> Outcome.ApiError(error)
                                    else -> Outcome.Failure(error)
                                }
                            } else {
                                Outcome.Failure(error)
                            }
                            stopLoading()
                        })
    }

    fun submitLiveClassPolls(
            pollId: String, liveClassId: String, submitOption: String,
            detailId: String
    ) {
        _submitPollResponseLiveData.value = Outcome.loading(true)
        compositeDisposable +
                DataHandler.INSTANCE
                        .courseRepository
                        .submitLiveClassPoll(hashMapOf<String, String>().apply {
                            put("poll_id", pollId)
                            put("liveclass_publish_id", liveClassId)
                            put("submit_option", submitOption)
                            put("detail_id", detailId)
                        })
                        .applyIoToMainSchedulerOnSingle()
                        .map {
                            it.data
                        }
                        .subscribeToSingle({
                            _submitPollResponseLiveData.value = Outcome.success(it)
                            _submitPollResponseLiveData.value = Outcome.loading(false)
                        }, {
                            val error = it
                            _submitPollResponseLiveData.value = if (error is HttpException) {
                                when (error.response()?.code()) {
                                    HttpURLConnection.HTTP_UNAUTHORIZED -> Outcome.BadRequest(error.message
                                            ?: "")
                                    HttpURLConnection.HTTP_BAD_REQUEST -> Outcome.ApiError(error)
                                    else -> Outcome.Failure(error)
                                }
                            } else {
                                Outcome.Failure(error)
                            }
                            _submitPollResponseLiveData.value = Outcome.loading(false)
                        })
    }

    fun getPollResult(publishId: String, pollId: String) {
        _pollResultLiveData.value = Outcome.loading(true)
        compositeDisposable +
                DataHandler.INSTANCE
                        .courseRepository
                        .getLiveClassPoll(publishId, pollId)
                        .applyIoToMainSchedulerOnSingle()
                        .map {
                            it.data
                        }
                        .subscribeToSingle({
                            _pollResultLiveData.value = Outcome.success(it)
                            _pollResultLiveData.value = Outcome.loading(false)
                        }, {
                            val error = it
                            _pollResultLiveData.value = if (error is HttpException) {
                                when (error.response()?.code()) {
                                    HttpURLConnection.HTTP_UNAUTHORIZED -> Outcome.BadRequest(error.message
                                            ?: "")
                                    HttpURLConnection.HTTP_BAD_REQUEST -> Outcome.ApiError(error)
                                    else -> Outcome.Failure(error)
                                }
                            } else {
                                Outcome.Failure(error)
                            }
                            _pollResultLiveData.value = Outcome.loading(false)
                        })
    }

}