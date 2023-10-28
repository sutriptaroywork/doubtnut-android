package com.doubtnutapp.libraryhome.coursev3.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.ApiCourseDetailData
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject

class CourseScheduleViewModel @Inject constructor(
        compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {

    var extraParams: HashMap<String, Any> = hashMapOf()

    var itemsAdded = 0

    private val _widgetsLiveData: MutableLiveData<Outcome<ApiCourseDetailData>> = MutableLiveData()

    val widgetsLiveData: LiveData<Outcome<ApiCourseDetailData>>
        get() = _widgetsLiveData

    private fun startLoading() {
        _widgetsLiveData.value = Outcome.loading(true)
    }

    private fun stopLoading() {
        _widgetsLiveData.value = Outcome.loading(false)
    }

    fun getCourseScheduleDetail(page: Int, assortmentId: String, month: String?, prevMonth: String?) {
        startLoading()
        viewModelScope.launch {
            DataHandler.INSTANCE.courseRepository.getCourseScheduleDetail(page, assortmentId, month, prevMonth)
                    .map {
                        it.data.widgets?.mapIndexed { index, widget ->
                            if (widget != null) {
                                if (widget.extraParams == null) {
                                    widget.extraParams = hashMapOf()
                                }
                                widget.extraParams?.putAll(extraParams)
                                widget.extraParams?.put(EventConstants.ITEM_PARENT_POSITION, itemsAdded + index)
                            }
                        }
                        it.data
                    }
                    .catch {
                        onError(it)
                    }
                    .collect {
                        if (!it.widgets.isNullOrEmpty()) {
                            itemsAdded.plus(it.widgets.size)
                        }
                        _widgetsLiveData.value = Outcome.success(it)
                        stopLoading()
                    }
        }
    }

    private fun onError(error: Throwable) {
        stopLoading()
        _widgetsLiveData.value = if (error is HttpException) {
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