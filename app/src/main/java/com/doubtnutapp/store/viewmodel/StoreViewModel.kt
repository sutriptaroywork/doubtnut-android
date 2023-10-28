package com.doubtnutapp.store.viewmodel

import android.nfc.FormatException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.Log

import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.domain.store.entities.StoreEntity
import com.doubtnutapp.domain.store.interactor.GetStoreResult
import com.doubtnutapp.store.dto.StoreResultDTO
import com.doubtnutapp.store.event.StoreEventManager
import com.doubtnutapp.store.mapper.StoreResultMapper
import com.doubtnutapp.store.model.StoreResult
import com.google.gson.JsonSyntaxException
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject

class StoreViewModel @Inject constructor(
        private val getStoreResult: GetStoreResult,
        private val storeResultMapper: StoreResultMapper,
        private val publishSubject: PublishSubject<String>,
        compositeDisposable: CompositeDisposable,
        private val storeEventManager: StoreEventManager
) : BaseViewModel(compositeDisposable) {

    init {
//        getStoreResults()
    }

    // Total Result of store - Tabs with data
    private val _storeResultLiveData: MutableLiveData<Outcome<StoreResultDTO>> = MutableLiveData()
    val storeResultLiveData: LiveData<Outcome<StoreResultDTO>>
        get() = _storeResultLiveData

    // Filter Coins from all store data
    private val _coinsLiveData: MutableLiveData<Outcome<Int>> = MutableLiveData()
    val coinsLiveData: LiveData<Outcome<Int>>
        get() = _coinsLiveData

    // Filter FreeExp from all store data
    private val _freeExpLiveData: MutableLiveData<Outcome<Int>> = MutableLiveData()
    val freeExpLiveData: LiveData<Outcome<Int>>
        get() = _freeExpLiveData

    // My Order Count
    private val _orderCountLiveData: MutableLiveData<Outcome<Int>> = MutableLiveData()
    val orderCountLiveData: LiveData<Outcome<Int>>
        get() = _orderCountLiveData

    fun setMyOrderCount(orderCount: Int) {
        _orderCountLiveData.value = Outcome.success(orderCount)
    }

    private val _myOrderCountLiveData: MutableLiveData<Int> = MutableLiveData()
    val myOrderCountLiveData: LiveData<Int>
        get() = _myOrderCountLiveData

    private fun onStoreListSuccess(storeResult: StoreEntity) {

        _coinsLiveData.value = Outcome.success(storeResult.coins)
        _freeExpLiveData.value = Outcome.success(storeResult.freexp)

        val tabTitleList = storeResult.storeItems.keys.toMutableList()
        val sortedByStoreTab = mutableMapOf<String, List<StoreResult>>()

        for (tab in tabTitleList) {
            val mappedStoreData = mutableListOf<StoreResult>()
            storeResult.storeItems[tab]?.apply {
                for (item in this) {
                    mappedStoreData.add(storeResultMapper.map(item))
                }
            }

            sortedByStoreTab[tab] = mappedStoreData
        }

        _storeResultLiveData.value = Outcome.success(StoreResultDTO(storeResult.coins, storeResult.freexp, tabTitleList, sortedByStoreTab))
    }

    fun handleAction(action: Any) {
        when (action) {

        }
    }

    private fun onStoreListError(error: Throwable) {
        _storeResultLiveData.value = Outcome.loading(false)
        _storeResultLiveData.value = if (error is HttpException) {
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

    fun getStoreResults() {
        getStoreResult
                .execute(GetStoreResult.Param(""))
                .toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onStoreListSuccess, this::onStoreListError)
                .also {
                    compositeDisposable.add(it)
                }
    }

    fun publishEvent(event: String, ignoreSnowplow: Boolean = true) {
        storeEventManager.eventWith(event, ignoreSnowplow = ignoreSnowplow)
    }
}