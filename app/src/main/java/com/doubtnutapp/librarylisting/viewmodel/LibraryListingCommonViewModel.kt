package com.doubtnutapp.librarylisting.viewmodel

import android.nfc.FormatException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.Log
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.RecyclerViewItem
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToCompletable
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.domain.newlibrary.entities.FilterEntity
import com.doubtnutapp.domain.newlibrary.entities.HeaderEntity
import com.doubtnutapp.domain.newlibrary.entities.LibraryHistoryEntity
import com.doubtnutapp.domain.newlibrary.entities.LibraryListingEntity
import com.doubtnutapp.domain.newlibrary.interactor.GetLibraryListingDataUseCase
import com.doubtnutapp.domain.newlibrary.interactor.PutLibraryHistoryUseCase
import com.doubtnutapp.librarylisting.mapper.LibraryListingViewMapper
import com.doubtnutapp.librarylisting.model.*
import com.doubtnutapp.newlibrary.event.LibraryEventManager
import com.doubtnutapp.plus
import com.doubtnutapp.resourcelisting.model.QuestionMetaDataModel
import com.doubtnutapp.utils.Event
import com.doubtnutapp.utils.NetworkUtil
import com.google.gson.JsonSyntaxException
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-09-29.
 */
class LibraryListingCommonViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val networkUtil: NetworkUtil,
    private val libraryListingDataUseCase: GetLibraryListingDataUseCase,
    private val putLibraryHistoryUseCase: PutLibraryHistoryUseCase,
    private val libraryListingViewMapper: LibraryListingViewMapper,
    private val libraryEventManager: LibraryEventManager
) : BaseViewModel(compositeDisposable) {

    var resourceType: String = ""

    private val _headerLiveData: MutableLiveData<Event<Pair<String, MutableList<HeaderInfo>?>>> =
        MutableLiveData()

    val headerLiveData: LiveData<Event<Pair<String, MutableList<HeaderInfo>?>>>
        get() = _headerLiveData

    var filterData: MutableList<FilterInfo>? = null

    private val _messageStringIdLiveData: MutableLiveData<Event<Int>> = MutableLiveData()

    val messageStringIdLiveData: LiveData<Event<Int>>
        get() = _messageStringIdLiveData

    private var _searchTextLiveData: MutableLiveData<Event<String>> = MutableLiveData()

    val searchTextLiveData: MutableLiveData<Event<String>>
        get() = _searchTextLiveData

    val isLoading: MutableLiveData<Boolean> = MutableLiveData()

    var listingItems = listOf<RecyclerViewItem>()

    private fun startLoading() {
        isLoading.postValue(true)
    }

    private fun stopLoading() {
        isLoading.postValue(false)
    }

    fun fetchListingData(id: String, packageDetailsId: String?, source: String) {
        startLoading()
        compositeDisposable + libraryListingDataUseCase
            .execute(GetLibraryListingDataUseCase.Param(1, id, packageDetailsId, source))
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle(this::onSuccess, this::onError)
    }

    fun onSuccess(entity: LibraryListingEntity) {
        onFilterSuccess(entity.filterList)
        listingItems = entity.item.orEmpty().map {
            libraryListingViewMapper.map(it)
        }
        resourceType = getResourceType(listingItems)

        onHeaderSuccess((entity.pageTitle ?: "") to entity.headerList)
        stopLoading()
    }

    private fun getResourceType(listingItems: List<RecyclerViewItem>?): String {
        if (!listingItems.isNullOrEmpty() && listingItems!![0] != null) {
            if (!listingItems.isNullOrEmpty() &&
                (listingItems[0] is BookViewItem ||
                        listingItems[0] is ChapterViewItem ||
                        listingItems[0] is ChapterFlexViewItem ||
                        listingItems[0] is ChapterViewItem)
            ) {
                return "book"
            }
            if (listingItems[0] is PdfViewItem) {
                return "pdf"
            }
            if (listingItems[0] is NextVideoViewItem || listingItems[0] is QuestionMetaDataModel) {
                return "video"
            }
        }
        return ""
    }

    private fun onHeaderSuccess(headerEntity: Pair<String, List<HeaderEntity>?>) {
        _headerLiveData.value = Event(headerEntity.first to headerEntity.second?.map {
            HeaderInfo(
                it.id,
                it.title,
                it.isLast,
                it.packageDetailsId,
                it.announcement
            )
        }?.toMutableList())
    }

    private fun onFilterSuccess(filterEntities: List<FilterEntity>?) {
        filterData = filterEntities?.map { FilterInfo(it.id, it.title, it.isLast) }?.toMutableList()
    }

    private fun onError(error: Throwable) {
        _messageStringIdLiveData.value = Event(R.string.something_went_wrong)
        stopLoading()
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

    fun onLibraryPlaylistTabSelected(title: String, parentTitle: String) {
        libraryEventManager.onLibraryPlaylistTabSelected(title, parentTitle)
    }

    fun putLibraryHistory(libraryHistoryEntity: LibraryHistoryEntity) {
        compositeDisposable + putLibraryHistoryUseCase.execute(libraryHistoryEntity)
            .applyIoToMainSchedulerOnCompletable()
            .subscribeToCompletable({})
    }
}