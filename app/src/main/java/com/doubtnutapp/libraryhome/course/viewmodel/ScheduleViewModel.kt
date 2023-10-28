package com.doubtnutapp.libraryhome.course.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.ApiScheduleData
import com.doubtnutapp.plus
import io.reactivex.disposables.CompositeDisposable
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject

class ScheduleViewModel @Inject constructor(
        compositeDisposable: CompositeDisposable) : BaseViewModel(compositeDisposable) {

    var extraParams: HashMap<String, Any> = hashMapOf()

    private val _responseData: MutableLiveData<Outcome<Pair<String, ApiScheduleData>>> = MutableLiveData()

    val responseData: LiveData<Outcome<Pair<String, ApiScheduleData>>>
        get() = _responseData

    private fun startLoading() {
        _responseData.value = Outcome.loading(true)
    }

    private fun stopLoading() {
        _responseData.value = Outcome.loading(false)
    }

    fun fetchScheduleData(requestType: String, previous: String?, next: String?) {
        startLoading()
        compositeDisposable +
                DataHandler.INSTANCE.courseRepository.getScheduleData(previous, next)
                        .applyIoToMainSchedulerOnSingle()
                        .map {
                            it.data.widgets?.map { widget ->
                                if (widget != null) {
                                    if (widget.extraParams == null) {
                                        widget.extraParams = hashMapOf()
                                    }
                                    widget.extraParams?.putAll(extraParams)
                                }
                            }
                            it.data
                        }
                        .subscribeToSingle({
                            _responseData.value = Outcome.success(requestType to it)
                            stopLoading()
                        }, this::onError)
    }

    private fun onError(error: Throwable) {
        stopLoading()
        _responseData.value = if (error is HttpException) {
            when (error.response()?.code()) {
                HttpURLConnection.HTTP_UNAUTHORIZED -> Outcome.BadRequest(error.message
                        ?: "")
                HttpURLConnection.HTTP_BAD_REQUEST -> Outcome.ApiError(error)
                else -> Outcome.Failure(error)
            }
        } else {
            Outcome.Failure(error)
        }
    }


}