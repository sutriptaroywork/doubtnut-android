package com.doubtnutapp.data.newglobalsearch.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiTrendingSearchDataListItem(
    @SerializedName("header") val header: String?,
    @SerializedName("data_type") val dataType: String?,
    @SerializedName("content_type") val contentType: String?,
    @SerializedName("img_url") val imageUrl: String?,
    @SerializedName("view_type") val widgetType: String?,
    @SerializedName("howItWorks") val howItWorksData: HowItWorks?,
    @SerializedName("playlist") val playlist: List<ApiTrendingSearchPlaylistItem>?,
    @SerializedName(" item_image_url") val itemImageUrl: String?,
    @SerializedName("type") val eventType: String?,

)
