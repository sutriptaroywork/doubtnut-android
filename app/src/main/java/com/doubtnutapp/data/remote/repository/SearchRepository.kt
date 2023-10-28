package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.data.remote.api.services.SearchService
import okhttp3.RequestBody

class SearchRepository(private val searchService: SearchService) {

    fun getHintAnimationStrings() =
        searchService.getHintAnimationStrings()

    fun getSearchHints() =
        searchService.getSearchHints()

    fun getLastPlayedVideo() =
        searchService.getLastPlayedVideoInfo()

    fun getAdvanceSearchFilters(requestBody: RequestBody) =
        searchService.getAdvanceSearchFilters(requestBody)
}
