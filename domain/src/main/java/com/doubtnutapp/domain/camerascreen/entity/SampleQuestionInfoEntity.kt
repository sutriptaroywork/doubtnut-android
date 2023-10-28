package com.doubtnutapp.domain.camerascreen.entity

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by Anand Gaurav on 2020-01-07.
 */
@Keep
data class SampleQuestionInfoEntity(
    @SerializedName("title") val title: String?,
    @SerializedName("content") val content: String?,
    @SerializedName("imageUrl") val imageUrl: String?,
    @SerializedName("showTimes") val showTimes: Int?
)
