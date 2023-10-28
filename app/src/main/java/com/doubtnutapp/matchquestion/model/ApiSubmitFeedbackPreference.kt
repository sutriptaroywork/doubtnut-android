package com.doubtnutapp.matchquestion.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiSubmitFeedbackPreference(
    @SerializedName("msg") val message: String
)