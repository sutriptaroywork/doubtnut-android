package com.doubtnutapp.liveclass.viewmodel

import android.text.format.DateFormat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.*
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.newglobalsearch.model.SearchSessionModel
import com.doubtnutapp.utils.UserUtil
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import okhttp3.RequestBody
import java.util.*
import javax.inject.Inject

class CategorySearchViewModel @Inject constructor(
        compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {

    private val suggestionsPubSub: PublishSubject<String> = PublishSubject.create()
    private var searchSessionsList: MutableList<SearchSessionModel> = mutableListOf()

    private val _searchLiveData: MutableLiveData<Outcome<WidgetsResponseData>> = MutableLiveData()

    val searchLiveData: LiveData<Outcome<WidgetsResponseData>>
        get() = _searchLiveData


    fun getCategorySearchData(requestBody: RequestBody) {
        _searchLiveData.value = Outcome.loading(true)
        viewModelScope.launch {
            DataHandler.INSTANCE.courseRepository.getCategorySearchData(requestBody)
                    .map { it.data }
                    .catch {
                        _searchLiveData.value = Outcome.loading(false)
                    }
                    .collect {
                        _searchLiveData.value = Outcome.success(it)
                        _searchLiveData.value = Outcome.loading(false)
                    }
        }
    }


    private val _suggestionLiveData: MutableLiveData<Outcome<SuggestionData>> = MutableLiveData()

    val suggestionLiveData: LiveData<Outcome<SuggestionData>>
        get() = _suggestionLiveData

    fun getSuggestionData(query: String) {
        val requestBody = hashMapOf<String, Any>().apply { put("text", query) }.toRequestBody()

        _suggestionLiveData.value = Outcome.loading(true)
        viewModelScope.launch {
            DataHandler.INSTANCE.courseRepository.getSuggestionData(requestBody)
                    .map { it.data }
                    .catch {
                        _suggestionLiveData.value = Outcome.loading(false)
                    }.collect {
                        _suggestionLiveData.value = Outcome.success(it)
                        _suggestionLiveData.value = Outcome.loading(false)
                    }
        }
    }

    fun addSession(data: SearchSessionModel) {
        searchSessionsList.add(data)
    }

    fun addEventsToLastSession(eventType: String) {
        if (searchSessionsList.isNotEmpty())
            searchSessionsList.last().eventTypes.add(eventType)
    }

    fun updateIsSearched(isSearched: Boolean) {
        if (searchSessionsList.isNotEmpty()) {
            searchSessionsList.last().isSearched = isSearched
        }
    }

    fun updateIsMatched(isMatched: Boolean) {
        if (searchSessionsList.isNotEmpty()) {
            searchSessionsList.last().isMatched = isMatched
        }
    }

    fun handleLastSearchSession(lastSearchQuery: String) {
        if (searchSessionsList.isEmpty())
            return
        addEventsToLastSession(EventConstants.LCS_EMPTY_BACK)
        if (!searchSessionsList.last().isMatched)
            searchSessionsList.last().apply {
                searchedText = lastSearchQuery
                isSearched = true
            }

    }

    fun addClickedItemsToLastSession(item: String, itemId: String, position: Int, tabName: String, tabPosition: Int, toppersChoice: Boolean) {
        if (searchSessionsList.isNotEmpty())
            if (searchSessionsList.last().isMatched) {
                val session = SearchSessionModel(
                        uscId = searchSessionsList.last().uscId,
                        eventTypes = searchSessionsList.last().eventTypes,
                        isMatched = true,
                        isSearched = true,
                        timeStamp = System.currentTimeMillis(),
                        size = 0,
                        searchedText = searchSessionsList.last().searchedText,
                        clickedItem = item,
                        clickedUniqueItemId = itemId,
                        selectedItemPosition = position,
                        selectedTabName = tabName,
                        selectedTabPosition = tabPosition,
                        isToppersChoice = toppersChoice
                )
                searchSessionsList.add(session)
            } else {
                addEventsToLastSession(EventConstants.SEARCH_CLICK_EVENT)
                searchSessionsList.last().apply {
                    isMatched = true
                    isSearched = true
                    clickedItem = item
                    clickedUniqueItemId = itemId
                    selectedItemPosition = position
                    selectedTabName = tabName
                    selectedTabPosition = tabPosition
                    isToppersChoice = toppersChoice
                }
            }
    }

    fun postPreviousSessions() {
        searchSessionsList
                .filter {
                    it.isSearched != null
                }.forEach {
                    DoubtnutApp.INSTANCE.analyticsPublisher.get().publishEvent(StructuredEvent("LcsSearch", "USCLogs", null, null, null, hashMapOf<String, Any>().apply {
                        put(EventConstants.USC_ID, "${it.uscId}_${UserUtil.getStudentId()}")
                        put(EventConstants.CREATED_AT, getFormattedTime(it.timeStamp))
                        put(EventConstants.EVENT_TYPE, it.eventTypes)
                        put(EventConstants.CLICKED_ITEM, it.clickedItem)
                        put(EventConstants.CLICKED_UNIQUE_IDS, it.clickedUniqueItemId)
                        put(EventConstants.SEARCHED_TEXT, it.searchedText)
                        put(EventConstants.SIZE, it.size)
                        put(EventConstants.SOURCE, "LCS")
                        put(EventConstants.IS_SEARCHED, it.isSearched!!)
                        put(EventConstants.IS_MATCHED, it.isMatched)
                        put(EventConstants.USC_LOGGING_VERSION, Constants.LCS_LOGGING_VERSION)
                        put(EventConstants.USC_ITEM_POSITION, it.selectedItemPosition)
                    }))
                }
        searchSessionsList.clear()
    }

    private fun getFormattedTime(timeStamp: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeStamp
        return DateFormat.format("yyyy-MMM-dd HH:mm:ss", calendar).toString()
    }

}