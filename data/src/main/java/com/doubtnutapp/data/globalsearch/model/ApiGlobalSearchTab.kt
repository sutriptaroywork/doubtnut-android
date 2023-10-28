package com.doubtnutapp.data.globalsearch.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiGlobalSearchTab(
    @SerializedName("description")
    val description: String,
    @SerializedName("key")
    val key: String
)
