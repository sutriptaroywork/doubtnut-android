package com.doubtnutapp.icons.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.icons.data.entity.IconsDetailResponse
import com.doubtnutapp.icons.data.remote.IconsRepository
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject

class IconsHomeFragmentVM @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val iconRepository: IconsRepository
) : BaseViewModel(compositeDisposable) {

    private val _iconsDetailResponse: MutableLiveData<Outcome<IconsDetailResponse>> =
        MutableLiveData()
    val iconsDetailResponse: LiveData<Outcome<IconsDetailResponse>>
        get() = _iconsDetailResponse

    fun getCategories(
        type: String,
        id: String,
        page: Int,
    ) {
        _iconsDetailResponse.value = Outcome.loading(true)
        viewModelScope.launch {
            iconRepository.getCategories(
                type = type,
                id = id,
                page = page
            )
                .catch {
                    it.printStackTrace()
                    _iconsDetailResponse.value = Outcome.loading(false)
                    _iconsDetailResponse.value = if (it is HttpException) {
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
                    _iconsDetailResponse.value = Outcome.loading(false)
                    _iconsDetailResponse.value = Outcome.success(it)
                }
        }
    }
}