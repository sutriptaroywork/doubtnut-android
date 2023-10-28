package com.doubtnutapp.store.viewmodel

import android.nfc.FormatException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.Log
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.domain.store.entities.ConvertCoinsEntity
import com.doubtnutapp.domain.store.interactor.ConvertCoinsResult
import com.doubtnutapp.store.dto.ConvertCoinsResultDTO
import com.doubtnutapp.store.event.StoreEventManager
import com.google.gson.JsonSyntaxException
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject

class ConvertCoinsViewModel @Inject constructor(
        private val convertCoinsResult: ConvertCoinsResult,
        private val publishSubject: PublishSubject<String>,
        private val storeEventManager: StoreEventManager,
        compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {

    // This will be updated after successful convertion of coins into DN cash
    private val _convertCoinsLiveData: MutableLiveData<Outcome<ConvertCoinsResultDTO>> = MutableLiveData()
    val convertCoinsLiveData: LiveData<Outcome<ConvertCoinsResultDTO>>
        get() = _convertCoinsLiveData

    fun convertCoins() {
        convertCoinsResult.execute(
                ConvertCoinsResult.None())
                .toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onConvertCoinSuccess, this::onConvertCoinError)
                .also {
                    compositeDisposable.add(it)
                }
        storeEventManager.eventWith(EventConstants.CONVERT_DN_CASH_CLICK)

    }

    private fun onConvertCoinSuccess(convertCoinsEntity: ConvertCoinsEntity) {
        _convertCoinsLiveData.value = Outcome.loading(false)
        _convertCoinsLiveData.value = Outcome.success(
                ConvertCoinsResultDTO(
                        convertCoinsEntity.isConverted,
                        convertCoinsEntity.message,
                        convertCoinsEntity.convertedXp
                )
        )
    }

    private fun onConvertCoinError(error: Throwable) {
        _convertCoinsLiveData.value = Outcome.loading(false)
        _convertCoinsLiveData.value = if (error is HttpException) {
            when (error.response()?.code()) {
                HttpURLConnection.HTTP_UNAUTHORIZED -> Outcome.BadRequest(error.message ?: "")
                HttpURLConnection.HTTP_BAD_REQUEST -> Outcome.ApiError(error)
                //TODO socket time out exception msg should be "Timeout while fetching the data"
                else -> Outcome.Failure(error)
            }
        } else {
            Outcome.Failure(error)
        }
        logException(error)
    }

    private fun logException(error: Throwable) {
        try {
            if (error is JsonSyntaxException
                    || error is NullPointerException
                    || error is ClassCastException
                    || error is FormatException
                    || error is IllegalArgumentException) {
                Log.e(error)
            }
        } catch (e: Exception) {

        }
    }

}