package com.doubtnutapp.course.viewmodel

import android.nfc.FormatException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.Log

import com.doubtnutapp.base.BaseViewModel
import com.doubtnut.core.sharing.entities.ShareOnWhatApp
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.course.event.CourseEventManager
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.domain.course.entities.CourseDataEntity
import com.doubtnutapp.domain.course.interactor.FetchCourseDataUseCase
import com.doubtnutapp.plus
import com.doubtnutapp.sharing.WhatsAppSharing
import com.doubtnutapp.utils.Event
import com.google.gson.JsonSyntaxException
import io.reactivex.disposables.CompositeDisposable
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject

class CourseViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val whatsAppSharing: WhatsAppSharing,
    private val fetchCourseDataUseCase: FetchCourseDataUseCase,
    private val courseEventManager: CourseEventManager
) : BaseViewModel(compositeDisposable) {

    private val _courseData: MutableLiveData<Outcome<CourseDataEntity>> = MutableLiveData()

    val courseData: LiveData<Outcome<CourseDataEntity>>
        get() = _courseData

    val isLoading: MutableLiveData<Boolean> = MutableLiveData()

    private val _messageStringIdLiveData: MutableLiveData<Event<Int>> = MutableLiveData()

    val messageStringIdLiveData: LiveData<Event<Int>>
        get() = _messageStringIdLiveData

    private fun startLoading() {
        _courseData.value = Outcome.loading(true)
    }

    private fun stopLoading() {
        _courseData.value = Outcome.loading(false)
    }

    fun fetchViewData(id: String) {
        startLoading()
        compositeDisposable + fetchCourseDataUseCase
            .execute(id)
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle(this::onSuccess, this::onError)
    }

    fun onSuccess(entity: CourseDataEntity) {
        _courseData.value = Outcome.success(entity)
        stopLoading()
    }

    private fun onError(error: Throwable) {
        stopLoading()
        _courseData.value = if (error is HttpException) {
            val errorCode = error.response()?.code()
            when (errorCode) {
                HttpURLConnection.HTTP_UNAUTHORIZED -> Outcome.BadRequest(error.message ?: "")
                HttpURLConnection.HTTP_BAD_REQUEST -> Outcome.ApiError(error)
                // TODO socket time out exception msg should be "Timeout while fetching the data"
                else -> Outcome.Failure(error)
            }
        } else {
            Outcome.Failure(error)
        }
        logException(error)
    }

    private fun logException(error: Throwable) {
        try {
            if (error is JsonSyntaxException ||
                error is NullPointerException ||
                error is ClassCastException ||
                error is FormatException ||
                error is IllegalArgumentException
            ) {
                Log.e(error)
            }
        } catch (e: Exception) {
        }
    }

    fun sendEvent(eventName: String, params: HashMap<String, Any>, sendToBranch: Boolean) {
        courseEventManager.publishEvent(eventName, params, sendToBranch)
    }

    fun handleAction(action: Any) {
        when (action) {
            is ShareOnWhatApp -> shareOnWhatsApp(action)
        }
    }

    private fun shareOnWhatsApp(action: ShareOnWhatApp) {
        whatsAppSharing.shareOnWhatsApp(action)
    }

    val showWhatsAppShareProgressBar = whatsAppSharing.showWhatsAppProgressLiveData

    val whatsAppShareableData: LiveData<Event<Triple<String?, String?, String?>>>
        get() = whatsAppSharing.whatsAppShareableData

    override fun onCleared() {
        super.onCleared()
        whatsAppSharing.dispose()
    }
}
