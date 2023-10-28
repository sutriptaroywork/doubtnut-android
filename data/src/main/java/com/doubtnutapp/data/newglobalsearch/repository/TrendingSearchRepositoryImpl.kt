package com.doubtnutapp.data.newglobalsearch.repository

import android.content.SharedPreferences
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.newglobalsearch.mapper.NewUserSearchMapper
import com.doubtnutapp.data.newglobalsearch.mapper.SearchSuggestionsMapper
import com.doubtnutapp.data.newglobalsearch.mapper.TrendingSearchMapper
import com.doubtnutapp.data.newglobalsearch.model.ApiSearchRequest
import com.doubtnutapp.data.newglobalsearch.service.InAppSearchMicroApiService
import com.doubtnutapp.data.newglobalsearch.service.InAppSearchService
import com.doubtnutapp.data.toRequestBody
import com.doubtnutapp.domain.newglobalsearch.entities.NewSearchDataEntity
import com.doubtnutapp.domain.newglobalsearch.entities.SearchSuggestionsDataEntity
import com.doubtnutapp.domain.newglobalsearch.entities.TrendingSearchDataListEntity
import com.doubtnutapp.domain.newglobalsearch.repository.TrendingSearchRepository
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class TrendingSearchRepositoryImpl @Inject constructor(
    private val inAppSearchService: InAppSearchService,
    private val inAppSearchMicroApiService: InAppSearchMicroApiService,
    private val trendingSearchMapper: TrendingSearchMapper,
    private val newUserSearchMapper: NewUserSearchMapper,
    private val searchSuggestionMapper: SearchSuggestionsMapper,
    private val userPreferences: UserPreference,
    private val sharedPreferences: SharedPreferences
) : TrendingSearchRepository {

    companion object {
        const val KEY_IAS_FACETOR = "IAS_facetor"
        const val KEY_TEXT = "text"
        const val KEY_CLASS = "class"
        const val KEY_FEATURE_IDS = "featureIds"
        const val KEY_SEARCH_TEXT = "search_text"
        const val KEY_SIZE = "size"
        const val KEY_DATA = "data"
        const val KEY_SEARCH_SESSION_COUNT = "searchSessionCount"
        const val KEY_IAS_SUGGESTER = "ias_suggester"
        const val KEY_SESSION_ID = "sessionId"
        const val KEY_LIVE_NOW_TOP_TAG = "ias_LiveNow_TopTag"
        const val KEY_LIVE_ORDER = "live_order"
        const val KEY_SOURCE = "source"
        const val KEY_IS_TRENDING_CHAPTER_ENABLED = "is_trending_chapter_enabled"
        const val KEY_IS_VIDEO_QUERY_CHANGE_ENABLED = "is_video_query_change_enabled"
    }

    override fun postUserSearchData(
        searchText: String,
        size: Int,
        playlistEntity: String
    ): Completable {

        val requestBody = hashMapOf(
            KEY_SEARCH_TEXT to searchText,
            KEY_SIZE to size,
            KEY_DATA to playlistEntity
        ).toRequestBody()

        return inAppSearchService.postUserSearchData(requestBody)
    }

    override fun postMongoEvent(paramsMap: Map<String, Any>): Completable {
        return inAppSearchService.postUserSearchData(paramsMap.toRequestBody())
    }

    override fun postSuggestionClickData(paramsMap: Map<String, Any>): Completable {
        return inAppSearchService.postUserSearchSuggestionsData(paramsMap.toRequestBody())
    }

    override fun getNewGlobalSearch(
        text: String,
        selectedClass: String,
        isVoiceSearch: Boolean,
        liveOrder: Boolean,
        searchTrigger: String?,
        appliedFilterMap: java.util.HashMap<String, Any>?,
        source: String,
        ias_advanced_filter: java.util.HashMap<String, Any>?,
        advancedFilterTabType: String
    ): Single<NewSearchDataEntity> {
        val studentClass: String = if (selectedClass.isEmpty())
            userPreferences.getUserClass()
        else
            selectedClass

        val featureIdsMap = hashMapOf<String, Any>().apply {
            put(KEY_IAS_FACETOR, true)
            put(KEY_LIVE_ORDER, liveOrder)
        }
        /*  val requestBody = hashMapOf<String, Any>(
                  KEY_TEXT to text,
                  KEY_CLASS to studentClass,
                  KEY_FEATURE_IDS to featureIdsMap
          )

          if (searchFilterFacet != null)
              requestBody[KEY_FILTER_FACET] = searchFilterFacet*/

        val requestBody = ApiSearchRequest(
            text, studentClass, userPreferences.getUserSelectedExams(),userPreferences.getUserSelectedBoard(),
            featureIdsMap, isVoiceSearch,
            searchTrigger, appliedFilterMap, source, ias_advanced_filter, advancedFilterTabType
        )
        return inAppSearchMicroApiService
            .getNewGlobalSearchResult(requestBody)
            .map {
                newUserSearchMapper.map(it.data)
            }
    }

    override fun getSearchSuggestions(
        text: String,
        suggesterPayloadMap: HashMap<String, Any>?,
        source: String
    ): Single<SearchSuggestionsDataEntity> {

        val featureIdsMap = HashMap<String, Any>()

        suggesterPayloadMap?.let {
            featureIdsMap.put(KEY_IAS_SUGGESTER, suggesterPayloadMap)
        }

        val requestBodyMap = HashMap<String, Any>()
        requestBodyMap[KEY_TEXT] = text
        requestBodyMap[KEY_FEATURE_IDS] = featureIdsMap
        requestBodyMap[KEY_SOURCE] = source

        return inAppSearchService
            .getSearchSuggestions(requestBodyMap.toRequestBody())
            .map {
                searchSuggestionMapper.map(it.data)
            }
    }

    override fun getTrendingSearch(
        liveNowTopTag: Boolean,
        source: String,
        isTrendingChapterEnabled: Double,
        videoQueryChangeEnabled: Double
    ): Single<List<TrendingSearchDataListEntity>> {
        var sessionCount = sharedPreferences.getInt(KEY_SEARCH_SESSION_COUNT, 0)
        sharedPreferences.edit().putInt(KEY_SEARCH_SESSION_COUNT, ++sessionCount).apply()

        val featureIdsMap = HashMap<String, Any>()
        featureIdsMap[KEY_LIVE_NOW_TOP_TAG] = liveNowTopTag
        featureIdsMap[KEY_IS_TRENDING_CHAPTER_ENABLED] = isTrendingChapterEnabled
        featureIdsMap[KEY_IS_VIDEO_QUERY_CHANGE_ENABLED] = videoQueryChangeEnabled

        val requestBodyMap = HashMap<String, Any>()
        requestBodyMap[KEY_SESSION_ID] = sessionCount.toString()
        requestBodyMap[KEY_FEATURE_IDS] = featureIdsMap
        requestBodyMap[KEY_SOURCE] = source

        return inAppSearchService
            .getTrendingSearchResult(requestBodyMap.toRequestBody())
            .map {
                it.data.map { apiTrendingSearchDataItem ->
                    trendingSearchMapper.map(apiTrendingSearchDataItem)
                }
            }.map {
                it.filter {
                    (it.dataType == "recent") || it.playlist.isNotEmpty()
                }
            }
    }
}
