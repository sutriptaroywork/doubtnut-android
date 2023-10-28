@file:Suppress("PropertyName")

package com.doubtnutapp.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnut.core.base.CoreViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.screennavigator.NavigationModel
import com.doubtnutapp.utils.Event
import io.reactivex.disposables.CompositeDisposable
import retrofit2.HttpException
import java.net.HttpURLConnection

open class BaseViewModel(compositeDisposable: CompositeDisposable) :
    CoreViewModel(compositeDisposable) {

    protected val _navigateLiveData = MutableLiveData<Event<NavigationModel>>()

    val navigateLiveData: LiveData<Event<NavigationModel>>
        get() = _navigateLiveData

    protected fun <T> getOutComeError(error: Throwable): Outcome<T> {
        return if (error is HttpException) {
            when (error.response()?.code()) {
                HttpURLConnection.HTTP_UNAUTHORIZED -> Outcome.BadRequest(error.message ?: "")
                HttpURLConnection.HTTP_BAD_REQUEST -> Outcome.ApiError(error)
                // TODO socket time out exception msg should be "Timeout while fetching the data"
                else -> Outcome.Failure(error)
            }
        } else {
            Outcome.Failure(error)
        }
    }
}
