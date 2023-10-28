package com.doubtnutapp.quiztfs.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.quiztfs.MyRewardsData
import com.doubtnutapp.quiztfs.repository.MyRewardsRepository
import com.doubtnutapp.quiztfs.widgets.DialogData
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject

/**
 * Created by Mehul Bisht on 07-09-2021
 */
class MyRewardsViewModel @Inject constructor(
    private val myRewardsRepository: MyRewardsRepository,
    compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {

    private val _myRewards: MutableLiveData<Outcome<MyRewardsData>> = MutableLiveData()
    val myRewards: LiveData<Outcome<MyRewardsData>> get() = _myRewards

    private val _submittedRewardData: MutableLiveData<Outcome<DialogData>> =
        MutableLiveData()
    val submittedRewardData: LiveData<Outcome<DialogData>> get() = _submittedRewardData

    fun fetchRewards(page: Int, type: String) {
        startLoading()
        viewModelScope.launch {
            myRewardsRepository.fetchRewards(page, type)
                .map { it.data }
                .catch {
                    stopLoading()
                    onError(it)
                }
                .collect {
                    stopLoading()
                    _myRewards.value = Outcome.success(it)
                }
        }
    }

    fun submitScratchStatus(couponCode: Int?) {
        startLoadingSubmitted()
        viewModelScope.launch {
            myRewardsRepository.submitScratchStatus(couponCode)
                .map { it.data }
                .catch {
                    onSubmitError(it)
                    stopLoadingSubmitted()
                }.collect {
                    _submittedRewardData.value = Outcome.success(it)
                    stopLoadingSubmitted()
                }
        }
    }

    private fun onSubmitError(error: Throwable) {
        _submittedRewardData.value = if (error is HttpException) {
            when (error.response()?.code()) {
                HttpURLConnection.HTTP_UNAUTHORIZED -> Outcome.BadRequest(error.message ?: "")
                HttpURLConnection.HTTP_BAD_REQUEST -> Outcome.ApiError(error)
                //TODO socket time out exception msg should be "Timeout while fetching the data"
                else -> Outcome.Failure(error)
            }
        } else {
            Outcome.Failure(error)
        }
    }

    private fun onError(error: Throwable) {
        _myRewards.value = if (error is HttpException) {
            when (error.response()?.code()) {
                HttpURLConnection.HTTP_UNAUTHORIZED -> Outcome.BadRequest(error.message ?: "")
                HttpURLConnection.HTTP_BAD_REQUEST -> Outcome.ApiError(error)
                //TODO socket time out exception msg should be "Timeout while fetching the data"
                else -> Outcome.Failure(error)
            }
        } else {
            Outcome.Failure(error)
        }
    }

    fun startLoadingSubmitted() {
        _submittedRewardData.value = Outcome.loading(true)
    }

    fun stopLoadingSubmitted() {
        _submittedRewardData.value = Outcome.loading(false)
    }

    fun startLoading() {
        _myRewards.value = Outcome.loading(true)
    }

    fun stopLoading() {
        _myRewards.value = Outcome.loading(false)
    }
}