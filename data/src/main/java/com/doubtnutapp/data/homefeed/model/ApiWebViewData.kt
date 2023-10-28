package com.doubtnutapp.data.homefeed.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiWebViewData(
    @SerializedName("url") val url: String?,
    @SerializedName("show_count") val count: Int?

)
