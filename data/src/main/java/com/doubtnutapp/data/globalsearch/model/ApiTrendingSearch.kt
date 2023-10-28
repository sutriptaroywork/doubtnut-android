package com.doubtnutapp.data.globalsearch.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiTrendingSearch(
    @SerializedName("tabs") val searchTabs: List<ApiGlobalSearchTab>,
    @SerializedName("list") val searchResultList: List<ApiTrendingSearchResult>
)
