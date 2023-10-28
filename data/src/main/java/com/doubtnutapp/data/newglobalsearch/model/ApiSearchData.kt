package com.doubtnutapp.data.newglobalsearch.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiSearchData(
    @SerializedName("recent_searches") val recentSearch: ApiTrendingSearch,
    @SerializedName("trending_playlist") val trendingSearch: ApiTrendingSearch
)
