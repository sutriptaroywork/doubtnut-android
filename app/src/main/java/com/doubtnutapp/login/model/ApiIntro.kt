package com.doubtnutapp.login.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by Anand Gaurav on 2019-08-13.
 */
@Keep
data class ApiIntro(
    @SerializedName("type") val type: String,
    @SerializedName("video") val video: String,
    @SerializedName("question_id") val questionId: String
)
