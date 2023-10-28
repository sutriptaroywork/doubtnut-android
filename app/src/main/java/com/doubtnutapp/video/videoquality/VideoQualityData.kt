package com.doubtnutapp.video.videoquality

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class VideoQualityData(

    @SerializedName("title")
    val title: String?,
    @SerializedName("title_color")
    val titleColor: String?,
    @SerializedName("title_size")
    val titleSize: String?,
    @SerializedName("subtitle")
    val subtitle: String?,
    @SerializedName("subtitle_color")
    val subtitleColor: String?,
    @SerializedName("subtitle_size")
    val subtitleSize: String?,
    @SerializedName("icon_url")
    val iconUrl: String?
)
