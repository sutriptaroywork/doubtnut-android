package com.doubtnutapp.data.globalsearch.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiGlobalSearchResult(
    @SerializedName("_source")
    val data: ApiGlobalSearchData
)
