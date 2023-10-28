package com.doubtnutapp.data.common.service

import com.doubtnutapp.data.newglobalsearch.model.YoutubeSearchApiData
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface ThirdPartyApisService {
    @GET("https://www.googleapis.com/youtube/v3/search")
    fun getYoutubeSearchResult(
        @Query("q") text: String,
        @Query("part") part: String,
        @Query("maxResults") maxResults: String,
        @Query("type") type: String,
        @Query("safeSearch") safeSearch: String,
        @Query("key") apiKey: String
    ):
        Single<YoutubeSearchApiData>
}
