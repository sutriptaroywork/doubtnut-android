package com.doubtnutapp.data.globalsearch.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiTrendingSearchResult(
    @SerializedName("type") val resultType: String,
    @SerializedName("display") val title: String,
    @SerializedName("class") val resultClass: String,
    @SerializedName("course") val resultCourse: String,
    @SerializedName("chapter") val chapter: String
)
