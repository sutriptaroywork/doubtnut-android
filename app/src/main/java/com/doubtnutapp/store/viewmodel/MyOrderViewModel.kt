package com.doubtnutapp.store.viewmodel

import android.nfc.FormatException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.Log

import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.OpenLibraryPlayListActivity
import com.doubtnutapp.base.OpenLibraryVideoPlayListScreen
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.domain.store.entities.MyOrderEntity
import com.doubtnutapp.domain.store.interactor.GetMyOrderResult
import com.doubtnutapp.librarylisting.ui.LibraryListingActivity
import com.doubtnutapp.screennavigator.*
import com.doubtnutapp.store.dto.MyOrderResultDTO
import com.doubtnutapp.store.event.MyOrderEventManager
import com.doubtnutapp.store.mapper.MyOrderResultMapper
import com.doubtnutapp.store.model.MyOrderResult
import com.doubtnutapp.utils.Event
import com.google.gson.JsonSyntaxException
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject

class MyOrderViewModel @Inject constructor(
    private val getMyOrderResult: GetMyOrderResult,
    private val myOrderResultMapper: MyOrderResultMapper,
    private val publishSubject: PublishSubject<String>,
    compositeDisposable: CompositeDisposable,
    private val myOrderEventManager: MyOrderEventManager
) : BaseViewModel(compositeDisposable) {

    init {
        setupObserver()
    }

    fun handleItemClick(action: Any) {
        when (action) {
            is OpenLibraryPlayListActivity -> {
                myOrderEventManager.eventWith(EventConstants.MY_ORDER_ITEM_CLICK + "_" + action.playlistName + "_Open")
                openLibraryContentListScreen(action)
            }
            is OpenLibraryVideoPlayListScreen -> {
                myOrderEventManager.eventWith(EventConstants.MY_ORDER_ITEM_CLICK + "_" + action.playlistName + "_Open")
                openLibraryVideoPlaylistScreen(action)
            }
        }
    }

    private fun openLibraryVideoPlaylistScreen(action: OpenLibraryVideoPlayListScreen) {
        val arg = hashMapOf(
            SCREEN_NAV_PARAM_PLAYLIST_ID to action.playlistId,
            SCREEN_NAV_PARAM_PLAYLIST_TITLE to action.playlistName
        )
        _navigateLiveData.value = Event(NavigationModel(LibraryVideoPlayListScreen, arg))
    }

    private fun openLibraryContentListScreen(action: OpenLibraryPlayListActivity) {
        _navigateLiveData.value = Event(
            NavigationModel(
                LibraryPlayListScreen, hashMapOf(
                    LibraryListingActivity.INTENT_EXTRA_PLAYLIST_ID to action.playlistId,
                    LibraryListingActivity.INTENT_EXTRA_PACKAGE_ID to action.packageDetailsId.orEmpty(),
                    LibraryListingActivity.INTENT_EXTRA_PLAYLIST_TITLE to action.playlistName
                )
            )
        )
    }

    private val _myOrderResultLiveData: MutableLiveData<Outcome<MyOrderResultDTO>> =
        MutableLiveData()

    val myOrderResultResultLiveData: LiveData<Outcome<MyOrderResultDTO>>
        get() = _myOrderResultLiveData

    private var myOrderItemClickedId = ""

    private fun onMyOrderListSuccess(myOrderEntity: MyOrderEntity) {

        val myOrderEntities = mutableListOf<MyOrderResult>()
        for (orderEntity in myOrderEntity.orderItems) {
            myOrderEntities.add(myOrderResultMapper.map(orderEntity))
        }

        _myOrderResultLiveData.value = Outcome.success(MyOrderResultDTO(myOrderEntities))
    }

    private fun onMyOrderListError(error: Throwable) {
        _myOrderResultLiveData.value = Outcome.loading(false)
        _myOrderResultLiveData.value = if (error is HttpException) {
            val errorCode = error.response()?.code()
            when (errorCode) {
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
                || error is IllegalArgumentException
            ) {
                Log.e(error)
            }
        } catch (e: Exception) {

        }
    }

    private fun setupObserver() {
        getMyOrderResult
            .execute(GetMyOrderResult.Param(""))
            .toObservable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::onMyOrderListSuccess, this::onMyOrderListError)
            .also {
                compositeDisposable.add(it)
            }

    }
}