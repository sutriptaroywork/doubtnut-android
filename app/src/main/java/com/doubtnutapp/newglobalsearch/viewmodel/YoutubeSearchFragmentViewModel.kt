package com.doubtnutapp.newglobalsearch.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.Constants
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.domain.newglobalsearch.entities.SearchFilter
import com.doubtnutapp.domain.newglobalsearch.entities.SearchFilterItem
import com.doubtnutapp.domain.newglobalsearch.entities.YTSearchDataEntity
import com.doubtnutapp.domain.newglobalsearch.interactor.YoutubeSearchUseCase
import com.doubtnutapp.newglobalsearch.mapper.YoutubeSearchViewItemMapper
import com.doubtnutapp.newglobalsearch.model.SearchListViewItem
import com.doubtnutapp.plus
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class YoutubeSearchFragmentViewModel @Inject constructor(
        private val youtubeSearchUseCase: YoutubeSearchUseCase,
        private val youtubeSearchViewItemMapper: YoutubeSearchViewItemMapper,
        compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {

    private val _youtubeSearchResultsLiveData = MutableLiveData<Outcome<List<SearchListViewItem>>>()

    val youtubeSearchResultsLiveData: LiveData<Outcome<List<SearchListViewItem>>>
        get() = _youtubeSearchResultsLiveData

    fun fetchDataFromYoutube(text: String) {
        if (!cachedDataList.isNullOrEmpty() && lastSearchedQuery == text)
            deliverCachedYoutubeData(cachedDataList!!)
        else
            performNetworkCallToYoutube(text)
    }

    private fun deliverCachedYoutubeData(cachedDataList: List<SearchListViewItem>) {
        _youtubeSearchResultsLiveData.value = Outcome.loading(false)
        _youtubeSearchResultsLiveData.value = Outcome.success(cachedDataList)
    }

    private fun performNetworkCallToYoutube(text: String) {
        lastSearchedQuery = text
        cachedDataList = null
        compositeDisposable + youtubeSearchUseCase
                .execute(YoutubeSearchUseCase.Params(text + Constants.ADD_DOUBTNUT_STRING + Constants.EXCLUDE_BYJU_VEDANTU_RESULTS_STRING,
                        Constants.YOUTUBE_API_KEY))
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(this::onUserYoutubeSearchSuccess, this::onYoutubeSearchError)
    }

    private fun onUserYoutubeSearchSuccess(ytSearchDataEntity: YTSearchDataEntity) {
        val youtubeApiData = ytSearchDataEntity.youtubeSearchItems
                .map { youtubeSearchViewItemMapper.map(it) }

        _youtubeSearchResultsLiveData.value = Outcome.loading(false)
        _youtubeSearchResultsLiveData.value = Outcome.success(youtubeApiData)
        cachedDataList = youtubeApiData
    }

    private fun onYoutubeSearchError(error: Throwable) {
        _youtubeSearchResultsLiveData.value = Outcome.loading(false)
        _youtubeSearchResultsLiveData.value = Outcome.apiError(error)
    }


    //Class: 6 to 12 in dropdown

    fun getFilterList(): ArrayList<SearchFilter> {

        var filterList: ArrayList<SearchFilter> = arrayListOf()

        //Class: 6 to 12
        var classFilterItemList: ArrayList<SearchFilterItem> = ArrayList<SearchFilterItem>()
        classFilterItemList.add(SearchFilterItem("6", null, "6", null, false))
        classFilterItemList.add(SearchFilterItem("7", null, "7", null, false))
        classFilterItemList.add(SearchFilterItem("8", null, "8", null, false))
        classFilterItemList.add(SearchFilterItem("9",  null,"9", null, false))
        classFilterItemList.add(SearchFilterItem("10",  null,"10", null, false))
        classFilterItemList.add(SearchFilterItem("11", null, "11", null, false))
        classFilterItemList.add(SearchFilterItem("12",  null,"12", null, false))

        val classFilter = SearchFilter("class", "Class", classFilterItemList)
        filterList.add(classFilter)

        //Subject: Maths, Science, English, Physics, Chemistry, Biology, Social Science
        var subjectFilterItemList: ArrayList<SearchFilterItem> = ArrayList<SearchFilterItem>()
        subjectFilterItemList.add(SearchFilterItem("Maths",  null,"Maths", null, false))
        subjectFilterItemList.add(SearchFilterItem("Science", null, "Science", null, false))
        subjectFilterItemList.add(SearchFilterItem("English",  null,"English", null, false))
        subjectFilterItemList.add(SearchFilterItem("Physics",  null,"Physics", null, false))
        subjectFilterItemList.add(SearchFilterItem("Chemistry", null, "Chemistry", null, false))
        subjectFilterItemList.add(SearchFilterItem("Biology",  null,"Biology", null, false))
        subjectFilterItemList.add(SearchFilterItem("Social Science",  null,"Social Science", null, false))

        val subjectFilter = SearchFilter("subject", "Subject", subjectFilterItemList)
        filterList.add(subjectFilter)

        //Language: English, Hindi
        var languageFilterItemList: ArrayList<SearchFilterItem> = ArrayList<SearchFilterItem>()
        languageFilterItemList.add(SearchFilterItem("English",  null,"English", null, false))
        languageFilterItemList.add(SearchFilterItem("Hindi",  null,"Hindi", null, false))

        val languageFilter = SearchFilter("language", "Language", languageFilterItemList)
        filterList.add(languageFilter)

        return filterList

    }

    /**
     * Creating singleton to persist the searched data from api
     * in order to avoid the api call again.
     */
    companion object {
        var lastSearchedQuery = ""

        var cachedDataList: List<SearchListViewItem>? = null
    }
}
