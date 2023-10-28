package com.doubtnutapp.data.globalsearch.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiGlobalSearchData(
    @SerializedName("type") val resultType: String,
    @SerializedName("question_id") val questionId: String?,
    @SerializedName("display") val title: String?,
    @SerializedName("class") val resultClass: String?,
    @SerializedName("course") val resultCourse: String?,
    @SerializedName("chapter") val chapter: String?,
    @SerializedName("mc_id") val microConceptId: String?,
    @SerializedName("subtopic") val subtopic: String?,
    @SerializedName("page") val page: String?
)
