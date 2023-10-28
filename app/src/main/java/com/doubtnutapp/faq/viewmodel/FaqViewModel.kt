package com.doubtnutapp.faq.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.repository.FaqRepository
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.plus
import com.doubtnutapp.utils.Event
import io.reactivex.disposables.CompositeDisposable
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject

class FaqViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val faqRepository: FaqRepository
) : BaseViewModel(compositeDisposable) {

    private val _widgetsLiveData: MutableLiveData<Outcome<List<WidgetEntityModel<*, *>>>> =
        MutableLiveData()
    private val _header: MutableLiveData<Event<String?>> = MutableLiveData()

    val widgetsLiveData: LiveData<Outcome<List<WidgetEntityModel<*, *>>>>
        get() = _widgetsLiveData

    val header: LiveData<Event<String?>> get() = _header
    private fun startLoading() {
        _widgetsLiveData.value = Outcome.loading(true)
    }

    private fun stopLoading() {
        _widgetsLiveData.value = Outcome.loading(false)
    }

    fun fetchFaqData(bucket: String?, priority: String?) {
        startLoading()
        compositeDisposable +
                faqRepository.getFaqData(bucket, priority)
                    .applyIoToMainSchedulerOnSingle()
                    .map {
                        it.data
                    }
                    .subscribeToSingle({
                        _widgetsLiveData.value = Outcome.success(it.widgets!!)
                        _header.value = Event(it.header)
                        stopLoading()
                    }, this::onError)
    }

    private fun onError(error: Throwable) {
        stopLoading()
        _widgetsLiveData.value = if (error is HttpException) {
            when (error.response()?.code()) {
                HttpURLConnection.HTTP_UNAUTHORIZED -> Outcome.BadRequest(
                    error.message
                        ?: ""
                )
                HttpURLConnection.HTTP_BAD_REQUEST -> Outcome.ApiError(error)
                else -> Outcome.Failure(error)
            }
        } else {
            Outcome.Failure(error)
        }
    }

}