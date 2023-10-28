package com.doubtnutapp.dnr.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Tab(
    @SerializedName("title") val title: String,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("id") val id: Int,
    @SerializedName("screen_alias") var screenAlias: String = "",
)
