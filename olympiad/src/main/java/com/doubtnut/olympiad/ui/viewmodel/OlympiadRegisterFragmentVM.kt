package com.doubtnut.olympiad.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnut.core.base.CoreViewModel
import com.doubtnut.core.data.remote.Outcome
import com.doubtnut.core.entitiy.BaseResponse
import com.doubtnut.olympiad.data.entity.OlympiadDetailResponse
import com.doubtnut.olympiad.data.remote.OlympiadRepository
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject

class OlympiadRegisterFragmentVM @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val olympiadRepository: OlympiadRepository
) : CoreViewModel(compositeDisposable) {

    private val _olympiadDetailResponse: MutableLiveData<Outcome<OlympiadDetailResponse>> =
        MutableLiveData()
    val olympiadDetailResponse: LiveData<Outcome<OlympiadDetailResponse>>
        get() = _olympiadDetailResponse

    private val _postOlympiadRegisterResponse: MutableLiveData<Outcome<BaseResponse>> =
        MutableLiveData()
    val postOlympiadRegisterResponse: LiveData<Outcome<BaseResponse>>
        get() = _postOlympiadRegisterResponse

    fun getOlympiadDetails(
        type: String,
        id: String,
    ) {
        _olympiadDetailResponse.value = Outcome.loading(true)
        viewModelScope.launch {
            olympiadRepository.getOlympiadDetails(
                type = type,
                id = id
            )
                .catch {
                    it.printStackTrace()
                    _olympiadDetailResponse.value = Outcome.loading(false)
                    _olympiadDetailResponse.value = if (it is HttpException) {
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
                    _olympiadDetailResponse.value = Outcome.loading(false)
                    _olympiadDetailResponse.value = Outcome.success(it)
                }
        }
    }

    fun postOlympiadRegister(
        type: String,
        id: String,
        payload: Map<String, String>,
    ) {
        _postOlympiadRegisterResponse.value = Outcome.loading(true)
        viewModelScope.launch {
            olympiadRepository.postOlympiadRegister(
                type = type,
                id = id,
                payload = payload
            )
                .catch {
                    it.printStackTrace()
                    _postOlympiadRegisterResponse.value = Outcome.loading(false)
                    _postOlympiadRegisterResponse.value = if (it is HttpException) {
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
                    _postOlympiadRegisterResponse.value = Outcome.loading(false)
                    _postOlympiadRegisterResponse.value = Outcome.success(it)
                }
        }
    }

}