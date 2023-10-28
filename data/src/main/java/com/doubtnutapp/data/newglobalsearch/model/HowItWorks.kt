package com.doubtnutapp.data.newglobalsearch.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class HowItWorks(
    @SerializedName("title")
    val title: String?,
    @SerializedName("text1")
    val text1: String?,
    @SerializedName("text2")
    val text2: String?,
    @SerializedName("text3")
    val text3: String?,
    @SerializedName("bg_image")
    val bgImage: String?
)
