package com.doubtnutapp.matchquestion.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by Anand Gaurav on 2019-10-18.
 */
@Keep
data class ApiMatchQuestionBanner(
    @SerializedName("content") val content: String,
    @SerializedName("dn_cash") val dnCash: Int?
)