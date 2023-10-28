package com.doubtnutapp.data.videoPage.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiResourceData(
    @SerializedName("id") val Id: String,
    @SerializedName("question_id") val questionId: String,
    @SerializedName("type") val type: String,
    @SerializedName("page_no") val pageNo: Int,
    @SerializedName("sub_obj") val subObj: String,
    @SerializedName("opt_1") val opt1: String,
    @SerializedName("opt_2") val opt2: String,
    @SerializedName("opt_3") val opt3: String,
    @SerializedName("opt_4") val opt4: String,
    @SerializedName("answer") val answer: String,
    @SerializedName("subtopic") val subtopic: String,
    @SerializedName("tag1") val tag1: String,
    @SerializedName("tag2") val tag2: String,
    @SerializedName("solutions") val solutions: String,
    @SerializedName("created_at") val createdAt: String
)
