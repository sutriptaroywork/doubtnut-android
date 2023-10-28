package com.doubtnutapp.newglobalsearch.viewmodel

import android.nfc.FormatException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.Log
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.domain.newglobalsearch.entities.SearchSuggestionsDataEntity
import com.doubtnutapp.domain.newglobalsearch.interactor.PostMongoEventTydUseCase
import com.doubtnutapp.domain.newglobalsearch.interactor.TypeYourDoubtSuggestionsUseCase
import com.doubtnutapp.globalsearch.event.InAppSearchEventManager
import com.doubtnutapp.newglobalsearch.mapper.TydSuggestionsItemMapper
import com.doubtnutapp.newglobalsearch.model.SearchSuggestionsDataItem
import com.doubtnutapp.plus
import com.google.gson.JsonSyntaxException
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import retrofit2.HttpException
import java.net.HttpURLConnection
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TypeYourDoubtViewModel @Inject constructor(
        private val postMongoEventTydUseCase: PostMongoEventTydUseCase,
        private val tydSuggestionItemMapper: TydSuggestionsItemMapper,
        private val inAppSearchEventManager: InAppSearchEventManager,
        private val typeYourDoubtSuggestionsUseCase: TypeYourDoubtSuggestionsUseCase,
        private val publishSubject: PublishSubject<String>,
        private val analyticsPublisher: AnalyticsPublisher,
        compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {

    init {
        setUpSearchObserver()
    }

    companion object {
        /**
         * Time delay to fetch suggestions while user is typing the search keywords.
         */
        const val TYPING_DELAY = 350L
    }

    private val _searchSuggestionsLiveData = MutableLiveData<Outcome<SearchSuggestionsDataItem>>()

    val searchSuggestionsLiveData: LiveData<Outcome<SearchSuggestionsDataItem>>
        get() = _searchSuggestionsLiveData

    private var keywordSearched = ""

    var source = ""

    fun getSearchSuggestions(text: String) {
        if (text.isNotEmpty()) {
            keywordSearched = text
            publishSubject.onNext(text)
        }
    }


    private fun onTrendingSearchError(error: Throwable) {

        _searchSuggestionsLiveData.value = if (error is HttpException) {
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
                    || error is IllegalArgumentException) {
                Log.e(error)
            }
        } catch (e: Exception) {

        }
    }

    private fun onSearchSuggestionSuccess(searchSuggestionDataEntity: SearchSuggestionsDataEntity) {

        _searchSuggestionsLiveData.value = Outcome.loading(false)
        _searchSuggestionsLiveData.value = Outcome.success(tydSuggestionItemMapper.map(searchSuggestionDataEntity))
    }


    fun postMongoEvent(paramsMap: Map<String, Any>) {
        postMongoEventTydUseCase
                .execute(PostMongoEventTydUseCase.Param(paramsMap))
                .applyIoToMainSchedulerOnCompletable()
                .subscribe()
    }

    private fun setUpSearchObserver() {
        compositeDisposable + publishSubject
                .debounce(TYPING_DELAY, TimeUnit.MILLISECONDS)
                .filter {
                    it.isNotEmpty()
                }
                .switchMap {
                    typeYourDoubtSuggestionsUseCase
                            .execute(TypeYourDoubtSuggestionsUseCase.Params(it))
                            .doOnSubscribe { _ ->
                                inAppSearchEventManager.onSearchInitiated(source, it)
                            }
                            .toObservable()
                            .subscribeOn(Schedulers.io())
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onSearchSuggestionSuccess, this::onTrendingSearchError)
                .also {
                    compositeDisposable.add(it)
                }
    }

    fun sendEvent(event: String, params: HashMap<String, Any>, ignoreSnowplow: Boolean = false) {
        inAppSearchEventManager.eventWith(event, params, ignoreSnowplow = ignoreSnowplow )
    }

    fun sendKeyboardSearchEvent(source: String, searchedItem: String) {
        inAppSearchEventManager.sendKeyboardSearchEvent(EventConstants.SEARCH_KEYBOARD_EVENT, source, searchedItem, ignoreSnowplow = true)
        postMongoEvent(hashMapOf<String, Any>().apply {
            put("eventType", "keyboard search")
            put("searched_item", searchedItem)
            put("search_text", searchedItem)
            put("size", 0)
            put(EventConstants.SOURCE, source)
        })
    }

    fun sendVoiceSearchEvent(source: String, searchedItem: String) {
        inAppSearchEventManager.sendKeyboardSearchEvent(EventConstants.SEARCH_VOICE_EVENT, source, searchedItem, ignoreSnowplow = true)
        postMongoEvent(hashMapOf<String, Any>().apply {
            put("eventType", "voice_search")
            put("searched_item", searchedItem)
            put("search_text", searchedItem)
            put("size", 0)
            put(EventConstants.SOURCE, source)
        })
    }
}