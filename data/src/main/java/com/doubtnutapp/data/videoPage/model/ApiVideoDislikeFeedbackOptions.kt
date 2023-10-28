package com.doubtnutapp.data.videoPage.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiVideoDislikeFeedbackOptions(
    @SerializedName("content") val content: String
)
