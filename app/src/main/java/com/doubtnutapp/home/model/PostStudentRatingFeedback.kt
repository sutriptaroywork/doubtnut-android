package com.doubtnutapp.home.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class PostStudentRatingFeedback (
        @SerializedName("rating")
        val rating: String,
        @SerializedName("feedback")
        val optionsList: List<String>
)