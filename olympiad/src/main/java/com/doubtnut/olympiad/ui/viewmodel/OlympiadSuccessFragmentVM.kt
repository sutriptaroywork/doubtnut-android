package com.doubtnut.olympiad.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnut.core.base.CoreViewModel
import com.doubtnut.core.data.remote.Outcome
import com.doubtnut.olympiad.data.entity.OlympiadSuccessResponse
import com.doubtnut.olympiad.data.remote.OlympiadRepository
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject

class OlympiadSuccessFragmentVM @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val olympiadRepository: OlympiadRepository
) : CoreViewModel(compositeDisposable) {

    private val _olympiadSuccessResponse: MutableLiveData<Outcome<OlympiadSuccessResponse>> =
        MutableLiveData()
    val olympiadSuccessResponse: LiveData<Outcome<OlympiadSuccessResponse>>
        get() = _olympiadSuccessResponse

    fun getOlympiadDetails(
        type: String,
        id: String,
    ) {
        _olympiadSuccessResponse.value = Outcome.loading(true)
        viewModelScope.launch {
            olympiadRepository.getOlympiadSuccessData(
                type = type,
                id = id
            )
                .catch {
                    it.printStackTrace()
                    _olympiadSuccessResponse.value = Outcome.loading(false)
                    _olympiadSuccessResponse.value = if (it is HttpException) {
                        when (it.response()?.code()) {
                            HttpURLConnection.HTTP_UNAUTHORIZED -> Outcome.BadRequest(
                                it.message ?: ""
                            )
                            HttpURLConnection.HTTP_BAD_REQUEST -> Outcome.ApiError(it)
                            else -> Outcome.Failure(it)
                        }
                    } else {
                        Outcome.Failure(it)
                    }
                }
                .collect {
                    _olympiadSuccessResponse.value = Outcome.loading(false)
                    _olympiadSuccessResponse.value = Outcome.success(it)
                }
        }
    }

}