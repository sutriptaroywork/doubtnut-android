package com.doubtnutapp.data.newglobalsearch.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiTrendingSearch(
    @SerializedName("header") val header: String?,
    @SerializedName("img_url") val imageUrl: String?,
    @SerializedName("playlist") val playlist: List<ApiUserSearchPlaylist>?
)
