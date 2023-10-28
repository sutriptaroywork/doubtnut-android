package com.doubtnutapp.ui.questionAskedHistory

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class QuestionAskedHistoryDetails(@SerializedName("list") val questionAskedList: List<QuestionAskedDetails>,
                                       @SerializedName("next_url") val nextUrl: String) {
    @Keep
    data class QuestionAskedDetails(@SerializedName("timestamp_formatted") val timestamp: String,
                                    @SerializedName("question_image") val quesImageUrl: String?,
                                    @SerializedName("is_solution_present") val isSolutionPresent: Boolean,
                                    @SerializedName("question_id") val questionId: Long?,
                                    @SerializedName("subject") val subject: String,
                                    @SerializedName("ocr_text") val questionText: String,
                                    @SerializedName("resource_type") val resourceType: String)
}

