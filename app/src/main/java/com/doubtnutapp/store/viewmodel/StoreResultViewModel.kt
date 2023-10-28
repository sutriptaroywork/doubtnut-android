package com.doubtnutapp.store.viewmodel

import android.nfc.FormatException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.Log
import com.doubtnutapp.base.*
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.domain.store.entities.RedeemStoreItemEntity
import com.doubtnutapp.domain.store.interactor.RedeemStoreItem
import com.doubtnutapp.librarylisting.ui.LibraryListingActivity
import com.doubtnutapp.screennavigator.*
import com.doubtnutapp.store.event.StoreEventManager
import com.doubtnutapp.store.ui.dialog.StoreItemBuyDialog
import com.doubtnutapp.store.ui.dialog.StoreItemDisabledDialog
import com.doubtnutapp.utils.Event
import com.google.gson.JsonSyntaxException
import io.reactivex.disposables.CompositeDisposable
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject

class StoreResultViewModel @Inject constructor(
        private val redeemStoreItem: RedeemStoreItem,
        compositeDisposable: CompositeDisposable,
        private val storeEventManager: StoreEventManager
) : BaseViewModel(compositeDisposable) {

    private val _dialogScreenLiveData: MutableLiveData<Event<NavigationModel>> = MutableLiveData()
    val dialogScreenLiveData: LiveData<Event<NavigationModel>>
        get() = _dialogScreenLiveData

    private val _availableDnCashLiveData: MutableLiveData<Int> = MutableLiveData()
    val availableDnCashLiveData: LiveData<Int>
        get() = _availableDnCashLiveData

    fun setAvailableDnCash(availableDnCash: Int? = -999) {
        _availableDnCashLiveData.postValue(availableDnCash)
    }

    private val _redeemStoreItemLiveData: MutableLiveData<Outcome<String>> = MutableLiveData()
    val redeemStoreItemLiveData: LiveData<Outcome<String>>
        get() = _redeemStoreItemLiveData

    private val _openBuyStoreItemDialogActionLiveData: MutableLiveData<OpenBuyStoreItemDialog> = MutableLiveData()
    val openBuyStoreItemDialogActionLiveData: LiveData<OpenBuyStoreItemDialog>
        get() = _openBuyStoreItemDialogActionLiveData

    private val _myOrderCountLiveData: MutableLiveData<Int> = MutableLiveData()
    val myOrderCountLiveData: LiveData<Int>
        get() = _myOrderCountLiveData


    fun handleStoreItemClick(action: Any) {
        when (action) {
            is OpenDisabledStoreItemDialog -> {
                _dialogScreenLiveData.value = Event(
                        NavigationModel(DisabledStoreItemDialogScreen, hashMapOf<String, Any>().apply {
                            put(StoreItemDisabledDialog.AVAILABLE_DN_CASH, availableDnCashLiveData.value as Int)
                            put(StoreItemDisabledDialog.ITEM_PRICE, action.itemPrice as Int)
                        }
                        )
                )
            }
            is OpenBuyStoreItemDialog -> {

                storeEventManager.eventWith(EventConstants.STORE_ITEM_BUY_CLICK + "_" + action.title + "_Click")

                _openBuyStoreItemDialogActionLiveData.postValue(action)

                executeRedeemStoreItem(action.id)
            }
            is OpenLibraryPlayListActivity -> openLibraryContentListScreen(action)
            is OpenLibraryVideoPlayListScreen -> openLibraryVideoPlaylistScreen(action)
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
        _navigateLiveData.value = Event(NavigationModel(LibraryPlayListScreen, hashMapOf(
                LibraryListingActivity.INTENT_EXTRA_PLAYLIST_ID to action.playlistId,
                LibraryListingActivity.INTENT_EXTRA_PACKAGE_ID to action.packageDetailsId.orEmpty(),
                LibraryListingActivity.INTENT_EXTRA_PLAYLIST_TITLE to action.playlistName
        )))
    }

    private fun executeRedeemStoreItem(id: Int) {
        compositeDisposable.add(redeemStoreItem
                .execute(RedeemStoreItem.Param(id))
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(this::onRedeemItemSuccess, this::onRedeemItemError))
    }

    private fun onRedeemItemSuccess(redeemStoreItemEntity: RedeemStoreItemEntity) {
        _redeemStoreItemLiveData.value = Outcome.loading(false)
        // On Success open dialog to open item
        redeemStoreItemEntity.message?.let {
            _redeemStoreItemLiveData.value = Outcome.success(it)
        }

        _openBuyStoreItemDialogActionLiveData.value?.run {
            _dialogScreenLiveData.value = Event(
                    NavigationModel(
                            BuyStoreItemDialogScreen, hashMapOf<String, Any>().apply {
                        put(StoreItemBuyDialog.RESOURCE_ID, resourceId as Int)
                        put(StoreItemBuyDialog.RESOURCE_TYPE, resourceType as String)
                        put(StoreItemBuyDialog.TITLE, title as String)
                        put(StoreItemBuyDialog.IMG_URL, imgUrl as String)
                        put(StoreItemBuyDialog.REDEEM_STATUS, redeemStatus)
                        put(StoreItemBuyDialog.ITEM_ID, id)
                        put(StoreItemBuyDialog.PRICE, price as Int)
                        put(StoreItemBuyDialog.IS_LAST, isLast)
                    }
                    )
            )
        }

        // Save counter of bought item in shared preference and clear this after user click on My Order icon.
        _myOrderCountLiveData.value = myOrderCountLiveData.value?.run {
            this + 1
        }

    }

    private fun onRedeemItemError(error: Throwable) {
        _redeemStoreItemLiveData.value = Outcome.loading(false)
        _redeemStoreItemLiveData.value = if (error is HttpException) {
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
