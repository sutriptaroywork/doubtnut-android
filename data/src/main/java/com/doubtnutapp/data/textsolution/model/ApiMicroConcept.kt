package com.doubtnutapp.data.textsolution.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by Anand Gaurav on 2019-08-28.
 */
@Keep
data class ApiMicroConcept(
    @SerializedName("mc_id") val mcId: String,
    @SerializedName("chapter") val chapter: String,
    @SerializedName("class") val mcClass: Int,
    @SerializedName("course") val mcCourse: String,
    @SerializedName("subtopic") val mcSubtopic: String?,
    @SerializedName("question_id") val mcQuestionId: String?,
    @SerializedName("answer_id") val mcAnswerId: String?,
    @SerializedName("duration") val mcVideoDuration: String?,
    @SerializedName("mc_text") var mcText: String?,
    @SerializedName("id") val mcVideoId: String?
)
