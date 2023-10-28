package com.doubtnutapp.data.newglobalsearch.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class YoutubeSearchResultsPageInfo(
    @SerializedName("totalResults")
    val totalResults: Int?,
    @SerializedName("resultsPerPage")
    val resultsPerPage: Int?
)
