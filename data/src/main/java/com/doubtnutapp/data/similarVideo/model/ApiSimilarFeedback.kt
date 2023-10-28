package com.doubtnutapp.data.similarVideo.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiSimilarFeedback(
    @SerializedName("feedback_text") val feedbackText: String,
    @SerializedName("is_show") val isShow: Int,
    @SerializedName("bg_color") val bgColor: String
)
