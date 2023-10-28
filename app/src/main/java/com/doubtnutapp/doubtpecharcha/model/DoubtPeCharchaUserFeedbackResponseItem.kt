package com.doubtnutapp.doubtpecharcha.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class DoubtPeCharchaUserFeedbackResponseItem(
    @SerializedName("user_id_selected") val userIdSelected: String?,
    @SerializedName("smiley_id_selected") val smileyIdSelected: Int,
    @SerializedName("feedback_selected") var feedbackOptionSelected: String?
)