package com.doubtnutapp.domain.newglobalsearch.repository

import com.doubtnutapp.domain.newglobalsearch.entities.NewSearchDataEntity
import com.doubtnutapp.domain.newglobalsearch.entities.SearchSuggestionsDataEntity
import com.doubtnutapp.domain.newglobalsearch.entities.TrendingSearchDataListEntity
import io.reactivex.Completable
import io.reactivex.Single

interface TrendingSearchRepository {

    fun getTrendingSearch(
        liveNowTopTag: Boolean,
        source: String,
        isTrendingChapterEnabled: Double,
        videoQueryChangeEnabled: Double
    ): Single<List<TrendingSearchDataListEntity>>

    fun getNewGlobalSearch(
        text: String,
        selectedClass: String,
        isVoiceSearch: Boolean,
        liveOrder: Boolean,
        searchTrigger: String?,
        appliedFilterMap: java.util.HashMap<String, Any>?,
        source: String,
        ias_advanced_filter: java.util.HashMap<String, Any>?,
        advancedFilterTabType: String
    ): Single<NewSearchDataEntity>

    fun getSearchSuggestions(
        text: String,
        suggesterPayloadMap: HashMap<String, Any>?,
        source: String
    ): Single<SearchSuggestionsDataEntity>

    fun postUserSearchData(searchText: String, size: Int, playlistEntity: String): Completable

    fun postMongoEvent(paramsMap: Map<String, Any>): Completable

    fun postSuggestionClickData(paramsMap: Map<String, Any>): Completable
}
